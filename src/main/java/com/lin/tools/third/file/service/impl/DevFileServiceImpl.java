package com.lin.tools.third.file.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.entity.CommonProperties;
import com.lin.common.exception.CommonException;
import com.lin.common.utils.CommonDownloadUtil;
import com.lin.tools.third.file.entity.DevFile;
import com.lin.tools.third.file.enums.DevFileEngineTypeEnum;
import com.lin.tools.third.file.param.DevFileIdParam;
import com.lin.tools.third.file.param.DevFileListParam;
import com.lin.tools.third.file.param.DevFilePageParam;
import com.lin.tools.third.file.service.DevFileService;
import com.lin.tools.third.file.mapper.DevFileMapper;
import com.lin.tools.third.file.strategy.EngineContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
* @author linsz
* @description 针对表【DEV_FILE(文件)】的数据库操作Service实现
* @createDate 2023-09-19 11:18:12
*/
@Service
public class DevFileServiceImpl extends ServiceImpl<DevFileMapper, DevFile>
        implements DevFileService{

    @Resource
    private CommonProperties properties;



    /**
     * MultipartFile文件上传，返回文件id
     *
     * @param engine
     * @param file
     */
    @Override
    public String uploadReturnId(String engine, MultipartFile file) {
        return this.storageFile(engine,file,true);
    }

    /**
     * MultipartFile文件上传，返回文件Url
     *
     * @param engine
     * @param file
     */
    @Override
    public String uploadReturnUrl(String engine, MultipartFile file) {
        return this.storageFile(engine,file,false);
    }

    /**
     * 存储文件
     * @param engine
     * @param file
     * @param returnFileId
     * @return
     */
    private String storageFile(String engine,MultipartFile file, boolean returnFileId) {
        String fileId = IdWorker.getIdStr();
        // get the engine context by engine  eg: LocalEngineContext/ AliyunEngineContext
        EngineContext engineContext = DevFileEngineTypeEnum.getEngineContext(engine);
        // get the storage url (may local or remote)
        String storageUrl = engineContext.storageFileAndGetUrlByEngine(engineContext.getDefaultBucketName(),
                getFileKey(fileId, file), file);
        return this.saveFile(fileId, engine, file, engineContext, storageUrl,returnFileId);
    }

    /**
     *   save the file properties to db
     * @param fileId
     * @param file
     * @param context
     */
    private String saveFile(String fileId,String engine,MultipartFile file,EngineContext context,String storageUrl, boolean returnFileId) {
        DevFile devFile = new DevFile();
        // file ori name  eg:  hahaha.doc
        String fileName = file.getOriginalFilename();
        // get suffix    eg: doc
        String suffix = ObjectUtil.isNotEmpty(fileName) ? StrUtil.subAfter(fileName, StrUtil.DOT,true) : null;
        // get obj name   eg:  123490043.doc
        String objName = fileId + StrUtil.DOT + suffix;
        // get file size kb
        BigDecimal fileSizeKb = NumberUtil.div(new BigDecimal(file.getSize()),
                BigDecimal.valueOf(1024),
                0, RoundingMode.HALF_UP);
        // get download path or url
        String downloadPath = "";
        if (engine.equals(DevFileEngineTypeEnum.LOCAL.getValue())){
            String backendUrl = properties.getBackendUrl();
            if (ObjectUtil.isNotEmpty(backendUrl)){
                downloadPath = backendUrl + "/dev/file/download?id="+fileId;
            }else {
                throw new CommonException("后端地址配置不正确,请查验");
            }
        }else {
            downloadPath = storageUrl;
        }
        // if file is a picture
        String thumbnail = "";
        try {
            thumbnail = isPicture(suffix)?(ImgUtil.toBase64DataUri(ImgUtil.scale(ImgUtil.toImage(file.getBytes()),100,100,null),suffix)):null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        devFile.setId(fileId)
                .setName(fileName)
                .setBucket(context.getDefaultBucketName())
                .setEngine(engine)
                .setSuffix(suffix)
                .setObjName(objName)
                // 未格式化
                .setSizeKb(Convert.toStr(fileSizeKb))
                // 格式化后
                .setSizeInfo(FileUtil.readableFileSize(file.getSize()))
                .setStoragePath(storageUrl)
                .setDownloadPath(downloadPath)
                .setThumbnail(thumbnail);

        this.save(devFile);
        return returnFileId ? fileId : downloadPath;

    }

    /**
     * 得到规定的文件名称  eg:  2023/8/15/1234577888.doc
     * @param fileId
     * @param file
     * @return
     */
    private String getFileKey(String fileId,MultipartFile file) {
        // get original file name
        String originalFilename = file.getOriginalFilename();
        // get suffix eg:  doc  扩展名不带“.”
        String fileSuffix = FileUtil.getSuffix(originalFilename);
        //set file name  eg: 123332323.doc
        String fileName = fileId + fileSuffix +StrUtil.DOT + fileSuffix;
        //set file key contains other folders.   eg: 2023/8/15/
        String fileKey = DateUtil.thisYear() + StrUtil.SLASH +
                // 获得月份，从0开始计数
                (DateUtil.thisMonth() + 1) + StrUtil.SLASH +
                DateUtil.thisDayOfMonth() + StrUtil.SLASH + fileName;

        return fileKey;
    }

    /**
     * 根据传入的文件后缀判断是否为图片
     * image Type, u can check the type of ImgUtil of Hutools
     * @param suffix
     * @return
     */
    private boolean isPicture(String suffix) {
        if (ObjectUtil.isEmpty(suffix)) return false;
        List<String> imgTypeList = Arrays.asList(ImgUtil.IMAGE_TYPE_GIF,
                ImgUtil.IMAGE_TYPE_JPG,
                ImgUtil.IMAGE_TYPE_JPEG,
                ImgUtil.IMAGE_TYPE_BMP,
                ImgUtil.IMAGE_TYPE_PNG,
                ImgUtil.IMAGE_TYPE_PSD);
        return imgTypeList.contains(suffix.toLowerCase());
    }



    /**
     * 文件分页列表接口
     *
     * @param devFilePageParam
     */
    @Override
    public Page<DevFile> page(DevFilePageParam devFilePageParam) {
        QueryWrapper<DevFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ObjectUtil.isNotEmpty(devFilePageParam.getEngine()),
                        DevFile::getEngine,devFilePageParam.getEngine())
                .like(ObjectUtil.isNotEmpty(devFilePageParam.getSearchKey()
                ), DevFile::getName, devFilePageParam.getSearchKey());
        Page<DevFile> devFilePage = new Page<>(1,20);
        return this.page(devFilePage, queryWrapper);
    }

    /**
     * 文件列表接口
     *
     * @param devFileListParam
     */
    @Override
    public List<DevFile> list(DevFileListParam devFileListParam) {
        QueryWrapper<DevFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ObjectUtil.isNotEmpty(devFileListParam.getEngine()),
                        DevFile::getEngine,devFileListParam.getEngine())
                .like(ObjectUtil.isNotEmpty(devFileListParam.getSearchKey()
                ), DevFile::getName, devFileListParam.getSearchKey());
        return this.list(queryWrapper);
    }

    /**
     * 下载文件 只能下载本地文件
     *
     * @param devFileIdParam
     * @param response
     */
    @Override
    public void download(DevFileIdParam devFileIdParam, HttpServletResponse response) throws IOException {
        DevFile devFile;
        try {
            devFile = this.queryEntity(devFileIdParam.getId());

        }catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
        if (!devFile.getEngine().equals(DevFileEngineTypeEnum.LOCAL.getValue())){
            throw new CommonException("非本地文件不知此方式下载,id:{}"+ devFile.getId());
        }
        File file = FileUtil.file(devFile.getStoragePath());
        if (!file.exists()){
            throw new CommonException("文件不存在,id:{}"+ devFile.getId());
        }
        CommonDownloadUtil.download(file,response);
    }

    /**
     * 删除文件
     *
     * @param devFileIdParamList
     */
    @Override
    public void delete(List<DevFileIdParam> devFileIdParamList) {
        this.removeByIds(CollStreamUtil.toList(devFileIdParamList,DevFileIdParam::getId));

    }

    /**
     * 获取文件详情 内调 queryEntity
     *
     * @param devFileIdParam
     */
    @Override
    public DevFile detail(DevFileIdParam devFileIdParam) {
        return this.queryEntity(devFileIdParam.getId());
    }

    /**
     * 获取文件详情
     *
     * @param id
     */
    @Override
    public DevFile queryEntity(String id) {
        DevFile devFile = this.getById(id);
        if (ObjectUtil.isEmpty(devFile)){
            throw new CommonException("所查询的文件id不存在");
        }
        return devFile;
    }
}



