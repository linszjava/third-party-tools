package com.lin.tools.third.file.strategy;

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
public interface FileEngine {

    String getDefaultBucketName();

    void storageFile(String bucketName, String key, InputStream inputStream);
    void storageFile(String bucketName, String key, File file);
    void storageFile(String bucketName, String key, byte[] bytes);
    void storageFile(String bucketName, String key, MultipartFile multipartFile);


    String storageFileAndGetUrl(String bucketName, String key, InputStream inputStream);
    String storageFileAndGetUrl(String bucketName, String key, File file);
    String storageFileAndGetUrl(String bucketName, String key, byte[] bytes);
    String storageFileAndGetUrl(String bucketName, String key, MultipartFile multipartFile);


}
