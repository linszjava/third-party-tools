package com.lin.tools.third.file.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lin.tools.third.file.entity.DevFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.tools.third.file.param.DevFileIdParam;
import com.lin.tools.third.file.param.DevFileListParam;
import com.lin.tools.third.file.param.DevFilePageParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
* @author linsz
* @description 针对表【DEV_FILE(文件)】的数据库操作Service
* @createDate 2023-09-19 11:18:12
*/
public interface DevFileService extends IService<DevFile> {

    /**
     * MultipartFile文件上传，返回文件id  null :Local   aliyun :Aliyun
     **/
    String uploadReturnId(String engine, MultipartFile file);

    /**
     * MultipartFile文件上传，返回文件Url
     **/
    String uploadReturnUrl(String engine, MultipartFile file);

    /**
     * 文件分页列表接口
     **/
    Page<DevFile> page(DevFilePageParam devFilePageParam);

    /**
     * 文件列表接口
     **/
    List<DevFile> list(DevFileListParam devFileListParam);

    /**
     * 下载文件
     **/
    void download(DevFileIdParam devFileIdParam, HttpServletResponse response) throws IOException;

    /**
     * 删除文件
     **/
    void delete(List<DevFileIdParam> devFileIdParamList);

    /**
     * 获取文件详情
     */
    DevFile detail(DevFileIdParam devFileIdParam);

    /**
     * 获取文件详情
     */
    DevFile queryEntity(String id);



}
