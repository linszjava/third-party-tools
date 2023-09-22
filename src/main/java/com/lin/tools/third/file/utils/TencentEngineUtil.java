package com.lin.tools.third.file.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.lin.common.exception.CommonException;
import com.lin.tools.third.config.provider.DevConfigProvider;
import com.lin.tools.third.file.constant.DevFileConst;
import com.lin.tools.third.file.enums.DevFileBucketAuthEnum;
import com.lin.tools.third.file.strategy.FileEngine;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/15 03:31
 */
public class TencentEngineUtil implements FileEngine {
    /**
     * 腾讯云的对象存储客户端
     */
    @Getter
    private static COSClient client;


    private static TransferManager transferManager;

    /**
     * 默认桶
     * -- GETTER --
     *  获取默认桶

     */
    private  static String defaultBucketName;

    private static void initClient() {
        DevConfigProvider devConfigProvider = SpringUtil.getBean(DevConfigProvider.class);
        // 初始化客户端
        String regionId = devConfigProvider.getValueByKey(DevFileConst.FILE_TENCENT_REGION_ID_KEY);
        String secretId = devConfigProvider.getValueByKey(DevFileConst.FILE_TENCENT_SECRET_ID_KEY);
        String secretKey = devConfigProvider.getValueByKey(DevFileConst.FILE_TENCENT_SECRET_KEY_KEY);
        defaultBucketName = devConfigProvider.getValueByKey(DevFileConst.FILE_TENCENT_DEFAULT_BUCKET_NAME);

        if (ObjectUtil.hasEmpty(regionId,secretId,secretKey,defaultBucketName)){
            throw new CommonException("regionId,secretId,secretKey,defaultBucketName 存在错误，请校对");
        }
        try {
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            // 2 设置 bucket 的地域
            ClientConfig clientConfig = new ClientConfig(new Region(regionId));
            //3. 生成客户端
            client = new COSClient(cred, clientConfig);


            // 4.线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
            // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
            ExecutorService threadPool = Executors.newFixedThreadPool(32);

            // 5.传入一个线程池, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
            transferManager = new TransferManager(client, threadPool);

            // 6.设置高级接口的分块上传阈值和分块大小为10MB
            TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
            transferManagerConfiguration.setMultipartUploadThreshold(10 * 1024 * 1024);
            transferManagerConfiguration.setMinimumUploadPartSize(10 * 1024 * 1024);
            transferManager.setConfiguration(transferManagerConfiguration);

        }catch (Exception e) {
            throw new CommonException("生成cos客户端出错");
        }
    }

    /**
     * 关闭  COS 客户端
     */
    public static void closeClient() {
        initClient();
        client.shutdown();
    }

    /**
     * 查询bucket 是否存在
     * @param bucketName
     * @return
     */
    public static boolean isBucketExist(String bucketName) {
        initClient();
        try {
            return client.doesBucketExist(bucketName);
        }catch (Exception e) {
            throw new CommonException("查询 COS client bucket 信息出错, {}",e.getMessage());
        }
    }

    /**
     * 设置  bucket 的基本权限
     */
    public static void setBucketAcl(String bucketName, DevFileBucketAuthEnum devFileBucketAuthEnum) {
        try {
            initClient();
            CannedAccessControlList accessControlList = toCannedAccessControlList(devFileBucketAuthEnum);
            client.setBucketAcl(bucketName,accessControlList);
        }catch (Exception e) {
            throw new CommonException("设置bucket权限失败,{}",e.getMessage());
        }
    }

    /**
     * 输入一个devFileBucketAuthEnum 匹配其与CannedAccessControlList中的类型相同
     * tip: CannedAccessControlList在 阿里云的oss 和 tencent cloud 的 cos 类名相同
     * @param devFileBucketAuthEnum
     * @return
     */
    private static CannedAccessControlList toCannedAccessControlList(DevFileBucketAuthEnum devFileBucketAuthEnum){
        switch (devFileBucketAuthEnum) {
            case PRIVATE:
                return CannedAccessControlList.Private;
            case PUBLIC_READ:
                return CannedAccessControlList.PublicRead;
            case PUBLIC_READ_WRITE:
                return CannedAccessControlList.PublicReadWrite;
            default:
                return null;
        }
    }

    /**
     * 判断文件是否存在
     * @param bucketName
     * @param key
     * @return
     */
    public static boolean isFileExist(String bucketName,String key) {
        try {
            initClient();
            return client.doesObjectExist(bucketName, key);
        }catch (Exception e){
            throw new CommonException("判断文件是否存在出错,{}", e.getMessage());
        }
    }


    // ==============================存储文件 不返回地址============================================

    public void storageFile(String bucketName, String key, File file) {
        BufferedInputStream inputStream = null;
        try {
            inputStream = FileUtil.getInputStream(file);
        }catch (Exception e) {
            throw new CommonException("存储文件出错,{}",e.getMessage());
        }
        storageFile(bucketName,key,inputStream);
    }

    public  void storageFile(String bucketName, String key, MultipartFile files) {
        try {
            storageFile(bucketName,key,files.getInputStream());
        }catch (Exception e) {
            throw new CommonException("存储文件出错,{}",e.getMessage());
        }
    }

    @Override
    public String getDefaultBucketName() {
        return defaultBucketName;
    }

    public  void storageFile(String bucketName, String key, InputStream inputStream) {
        try {
            initClient();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(getFileContentType(key));
            client.putObject(bucketName,key,inputStream,objectMetadata);
        }catch (Exception exception){
            throw new CommonException("存储文件出错, {}", exception.getMessage());
        }finally {
            IoUtil.close(inputStream);
        }
    }

    /**
     * 存储文件不返回地址
     * @param bucketName
     * @param key
     * @param bytes
     */
    public  void storageFile(String bucketName, String key, byte[] bytes) {
        try {
            initClient();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(getFileContentType(key));
            client.putObject(bucketName,key,byteArrayInputStream,objectMetadata);
        }catch (Exception e){
            throw new CommonException("存储文件byte存在错误,{}",e.getMessage());
        }
    }


    // ==============================存储文件   返回地址============================================


    public  String storageFileAndGetUrl(String bucketName, String key,File file){
        //1. storage file
        storageFile(bucketName,key,file);
        //2. set file acl
        setFileAcl(bucketName,key,DevFileBucketAuthEnum.PUBLIC_READ);
        //3. get return url
        return getFileAbsolutePath(bucketName,key);
    }
    public  String storageFileAndGetUrl(String bucketName, String key,MultipartFile multipartFile){
        //1. storage file
        storageFile(bucketName,key,multipartFile);
        //2. set file acl
        setFileAcl(bucketName,key,DevFileBucketAuthEnum.PUBLIC_READ);
        //3. get return url
        return getFileAbsolutePath(bucketName,key);
    }
    public  String storageFileAndGetUrl(String bucketName, String key,byte[] bytes){
        //1. storage file
        storageFile(bucketName,key,bytes);
        //2. set file acl
        setFileAcl(bucketName,key,DevFileBucketAuthEnum.PUBLIC_READ);
        //3. get return url
        return getFileAbsolutePath(bucketName,key);
    }
    public  String storageFileAndGetUrl(String bucketName, String key,InputStream inputStream){
        //1. storage file
        storageFile(bucketName,key,inputStream);
        //2. set file acl
        setFileAcl(bucketName,key,DevFileBucketAuthEnum.PUBLIC_READ);
        //3. get return url
        return getFileAbsolutePath(bucketName,key);
    }

    /**
     * 设置文件权限
     * @param bucketName
     * @param key
     * @param devFileBucketAuthEnum
     */
    public static void setFileAcl(String bucketName, String key,DevFileBucketAuthEnum devFileBucketAuthEnum) {
        try {
            initClient();
            CannedAccessControlList cannedAccessControlList = toCannedAccessControlList(devFileBucketAuthEnum);
            client.setObjectAcl(bucketName,key,cannedAccessControlList);
        }catch (Exception e) {
            throw new CommonException("设置文件权限控制异常,{}",e.getMessage());
        }
    }


    /**
     * 得到文件的下载地址带授权时间
     */
    public static String getFileAuthUrl(String bucketName, String key, long timeoutMillis) {
        try {
            initClient();
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName,key, HttpMethodName.GET);
            request.setContentType(getFileContentType(key));
            request.setExpiration(new Date(System.currentTimeMillis() + timeoutMillis));
            return client.generatePresignedUrl(request).toString();
        }catch (Exception e) {
            throw new CommonException("生成文件的的带过期时间的文件地址失败,{}",e.getMessage());
        }
    }


    // ==============================   其      他   ============================================

    /**
     * 复制文件
     */
    public static void copyFile(String originalBucketName,String originalKey, String newBucketName, String newKey) {
        try {
            initClient();
            CopyObjectResult copyObjectResult = client.copyObject(originalBucketName, originalKey,
                    newBucketName, newKey);
        }catch (Exception e) {
            throw new CommonException("复制文件失败,{}",e.getMessage());
        }
    }

    /**
     * 得到文件的绝对地址
     */
    public static String getFileAbsolutePath(String bucketName,String key){
        try {
            initClient();
            return client.getObjectUrl(bucketName, key).toString();
        }catch (Exception e) {
            throw new CommonException("获取文件的绝对地址失败,{}",e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param bucketName
     * @param key
     */
    public static void deleteFile(String bucketName, String key) {
        try {
            initClient();
            client.deleteObject(bucketName,key);
        }catch (Exception e) {
            throw new CommonException("删除文件失败,{}",e.getMessage());
        }
    }

    /**
     * 获取文件的byte流
     * @param bucketName
     * @param key
     * @return
     */
    public static byte[] getFileBytes(String bucketName, String key){
        COSObjectInputStream objectContent = null;
        try {
            initClient();
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
            COSObject clientObject = client.getObject(getObjectRequest);
            objectContent = clientObject.getObjectContent();
            return IoUtil.readBytes(objectContent);
        }catch (Exception e) {
            throw new CommonException("获取文件的byte流失败,{}",e.getMessage());
        }finally {
            IoUtil.close(objectContent);
        }

    }

    private static String getFileContentType(String key) {
        // 根据文件名获取contentType
        String contentType = "application/octet-stream";
        if (key.contains(".")) {
            contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(key);
        }
        return contentType;
    }


}
