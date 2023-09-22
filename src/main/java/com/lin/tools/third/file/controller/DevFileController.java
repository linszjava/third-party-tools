package com.lin.tools.third.file.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.lin.common.result.CommonResult;
import com.lin.tools.third.config.provider.DevConfigProvider;
import com.lin.tools.third.file.constant.DevFileConst;
import com.lin.tools.third.file.entity.DevFile;
import com.lin.tools.third.file.enums.DevFileEngineTypeEnum;
import com.lin.tools.third.file.param.DevFileIdParam;
import com.lin.tools.third.file.param.DevFileListParam;
import com.lin.tools.third.file.param.DevFilePageParam;
import com.lin.tools.third.file.service.DevFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/4 20:42
 */
@Api(tags = "文件管理控制器")
@ApiSupport(author = "linsz",order = 4)
@RestController
@Validated
@RequestMapping("/dev/file")
public class DevFileController {
    

    @Autowired
    private DevFileService devFileService;

    @Autowired
    private DevConfigProvider devConfigProvider;


    /**
     * 动态上传文件返回id  动态：引擎可以随时更改，则上传位置根据引擎不同存储在不同的位置，故称为 动态；
     */
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "动态上传文件返回id", notes = "动态上传文件返回id")
    @PostMapping("/uploadDynamicReturnId")
    public CommonResult<String> uploadDynamicReturnId(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnId(devConfigProvider.getValueByKey(DevFileConst.SYS_DEFAULT_FILE_ENGINE_KEY),file));
    }

    /**
     * 动态上传文件返回url  动态：引擎可以随时更改，则上传位置根据引擎不同存储在不同的位置，故称为 动态；
     */
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "动态上传文件返回url", notes = "动态上传文件返回url")
    @PostMapping("/uploadDynamicReturnUrl")
    public CommonResult<String> uploadDynamicReturnUrl(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnUrl(devConfigProvider.getValueByKey(DevFileConst.SYS_DEFAULT_FILE_ENGINE_KEY),file));
    }

    /**
     * 上传本地文件返回id   引擎：本地
     */
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "上传本地文件返回id", notes = "上传本地文件返回id")
    @PostMapping("/uploadLocalReturnId")
    public CommonResult<String> uploadLocalReturnId(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnId(DevFileEngineTypeEnum.LOCAL.getValue(), file));
    }

    /**
     * 上传本地文件返回url   引擎：本地
     */
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "上传本地文件返回url", notes = "上传本地文件返回url")
    @PostMapping("/uploadLocalReturnUrl")
    public CommonResult<String> uploadLocalReturnUrl(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnUrl(DevFileEngineTypeEnum.LOCAL.getValue(), file));
    }

    /**
     * 上传阿里云文件返回id   引擎：阿里云
     */
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "上传阿里云文件返回id", notes = "上传阿里云文件返回id")
    @PostMapping("/uploadAliyunReturnId")
    public CommonResult<String> uploadAliyunReturnId(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnId(DevFileEngineTypeEnum.ALIYUN.getValue(), file));
    }

    /**
     * 上传阿里云文件返回url   引擎：阿里云
     */
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "上传阿里云文件返回url", notes = "上传阿里云文件返回url")
    @PostMapping("/uploadAliyunReturnUrl")
    public CommonResult<String> uploadAliyunReturnUrl(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnUrl(DevFileEngineTypeEnum.ALIYUN.getValue(), file));
    }

    /**
     * 上传腾讯云文件返回id   引擎：腾讯云
     */
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "上传腾讯云文件返回id", notes = "上传腾讯云文件返回id")
    @PostMapping("/uploadTencentReturnId")
    public CommonResult<String> uploadTencentReturnId(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnId(DevFileEngineTypeEnum.TENCENT.getValue(), file));
    }

    /**
     * 上传腾讯云文件返回url   引擎：腾讯云
     */
    @ApiOperationSupport(order = 8)
    @ApiOperation(value = "上传腾讯云文件返回url", notes = "上传腾讯云文件返回url")
    @PostMapping("/uploadTencentReturnUrl")
    public CommonResult<String> uploadTencentReturnUrl(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnUrl(DevFileEngineTypeEnum.TENCENT.getValue(), file));
    }

    /**
     * 上传minio文件返回id   引擎：minio
     */
    @ApiOperationSupport(order = 9)
    @ApiOperation(value = "上传minio文件返回id", notes = "上传minio文件返回id")
    @PostMapping("/uploadMinioReturnId")
    public CommonResult<String> uploadMinioReturnId(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnId(DevFileEngineTypeEnum.MINIO.getValue(), file));
    }

    /**
     * 上传minio文件返回url   引擎：minio
     */
    @ApiOperationSupport(order = 10)
    @ApiOperation(value = "上传minio文件返回url", notes = "上传minio文件返回url")
    @PostMapping("/uploadMinioReturnUrl")
    public CommonResult<String> uploadMinioReturnUrl(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(devFileService.uploadReturnUrl(DevFileEngineTypeEnum.MINIO.getValue(), file));
    }

    /**
     * 获取文件分页
     */
    @ApiOperationSupport(order = 11)
    @ApiOperation(value = "获取文件分页", notes = "获取文件分页")
    @GetMapping("/page")
    public CommonResult<Page<DevFile>> page(DevFilePageParam devFilePageParam) {
        return CommonResult.data(devFileService.page(devFilePageParam));
    }

    /**
     * 获取文件列表
     */
    @ApiOperationSupport(order = 12)
    @ApiOperation(value = "获取文件列表", notes = "获取文件列表")
    @GetMapping("/list")
    public CommonResult<List<DevFile>> list(DevFileListParam devFileListParam) {
        return CommonResult.data(devFileService.list(devFileListParam));
    }

    /**
     * 下载文件  本地
     */
    @ApiOperationSupport(order = 13)
    @ApiOperation(value = "下载文件", notes = "下载文件")
    @GetMapping(value = "/download" ,produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void download(@Valid DevFileIdParam devFileIdParam, HttpServletResponse response) throws IOException {
        devFileService.download(devFileIdParam,response);
    }

    /**
     * 删除文件
     */
    @ApiOperationSupport(order = 14)
    @ApiOperation(value = "删除文件", notes = "删除文件")
    @PostMapping("/delete")
    public CommonResult<String> delete(@Valid @RequestBody @NotEmpty(message = "id不能为空") List<DevFileIdParam> idList){
        devFileService.delete(idList);
        return CommonResult.ok();
    }

    /**
     * 获取文件详情
     */
    @ApiOperationSupport(order = 15)
    @ApiOperation(value = "获取文件详情", notes = "获取文件详情")
    @GetMapping("/detail")
    public CommonResult<DevFile> detail(@Valid DevFileIdParam devFileIdParam) {
        return CommonResult.data(devFileService.detail(devFileIdParam));
    }







}
