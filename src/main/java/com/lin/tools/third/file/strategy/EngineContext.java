package com.lin.tools.third.file.strategy;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/14 23:16
 */
@AllArgsConstructor
@NoArgsConstructor
@Component
public class EngineContext {

    private FileEngine fileEngine;


//    public EngineContext(FileEngine fileEngine) {
//        this.fileEngine = fileEngine;
//    }
//
//    public EngineContext(){}


    public void storageFileByEngine(String bucketName, String key, InputStream inputStream) {
        fileEngine.storageFile(bucketName,key,inputStream);
    }
    public void storageFileByEngine(String bucketName, String key, File file) {
        fileEngine.storageFile(bucketName,key,file);
    }
    public void storageFileByEngine(String bucketName, String key, MultipartFile multipartFile) {
        fileEngine.storageFile(bucketName,key,multipartFile);
    }
    public void storageFileByEngine(String bucketName, String key, byte[] bytes) {
        fileEngine.storageFile(bucketName,key,bytes);
    }

    /**
     * 策略模式 存储文件并返回文件的地址
     * @param bucketName
     * @param key
     * @param inputStream
     * @return
     */
    public String storageFileAndGetUrlByEngine(String bucketName, String key, InputStream inputStream) {
        return fileEngine.storageFileAndGetUrl(bucketName, key, inputStream);
    }

    public String storageFileAndGetUrlByEngine(String bucketName, String key, MultipartFile multipartFile) {
        return fileEngine.storageFileAndGetUrl(bucketName, key, multipartFile);
    }

    public String storageFileAndGetUrlByEngine(String bucketName, String key, File file) {
        return fileEngine.storageFileAndGetUrl(bucketName, key, file);
    }

    public String storageFileAndGetUrlByEngine(String bucketName, String key, byte[] bytes) {
        return fileEngine.storageFileAndGetUrl(bucketName, key, bytes);
    }

    /**
     * 策略模式 得到默认的bucketName
     * @return
     */
    public String getDefaultBucketName() {
        return fileEngine.getDefaultBucketName();
    }


}
