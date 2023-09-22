package com.lin.tools.third.file.controller;

import com.lin.tools.third.config.provider.DevConfigProvider;
import com.lin.tools.third.file.constant.DevFileConst;
import com.lin.tools.third.file.utils.AliyunEngineUtil;
import com.lin.tools.third.file.utils.MinioEngineUtil;
import com.lin.tools.third.file.utils.TencentEngineUtil;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/20 13:53
 */
@RestController
@RequestMapping("/test")
public class FileTestController {

    @Autowired
    private DevConfigProvider DevConfigProvider;

    @GetMapping("/getClient")
    public String getClient(){
        return AliyunEngineUtil.getOSsClient().toString();
    }

    @GetMapping("/isBucketExist")
    public boolean isBucketExist(){
        return AliyunEngineUtil.isBucketExist(DevConfigProvider.getValueByKey(DevFileConst.FILE_ALIYUN_DEFAULT_BUCKET_NAME));
    }
}
