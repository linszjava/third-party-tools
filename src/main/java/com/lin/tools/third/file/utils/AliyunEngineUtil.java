package com.lin.tools.third.file.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.aliyun.oss.*;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.lin.common.exception.CommonException;
import com.lin.tools.third.config.provider.DevConfigProvider;
import com.lin.tools.third.file.constant.DevFileConst;
import com.lin.tools.third.file.enums.DevFileBucketAuthEnum;
import com.lin.tools.third.file.strategy.FileEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/15 03:31
 */
public class AliyunEngineUtil implements FileEngine {

    private static OSS client;


    // 默认的bucketName
    private  static String defaultBucketName;

    private static void initClient() {
        DevConfigProvider devConfigProvider = SpringUtil.getBean(DevConfigProvider.class);

        String endPoint = devConfigProvider.getValueByKey(DevFileConst.FILE_ALIYUN_END_POINT_KEY);
//        String endPoint = "http://oss-cn-guangzhou.aliyuncs.com";
        String accessKeyId = devConfigProvider.getValueByKey(DevFileConst.FILE_ALIYUN_ACCESS_KEY_ID_KEY);

        String accessSecretKey = devConfigProvider.getValueByKey(DevFileConst.FILE_ALIYUN_ACCESS_KEY_SECRET_KEY);

        defaultBucketName = devConfigProvider.getValueByKey(DevFileConst.FILE_ALIYUN_DEFAULT_BUCKET_NAME);

        if (ObjectUtil.isEmpty(defaultBucketName)) {
            throw new CommonException("ALIYUN-OSS客户端配置失败，请校验{}的值",defaultBucketName);
        }
            client = new OSSClientBuilder().build(endPoint, accessKeyId, accessSecretKey);
    }

//    ============================file engine interface=====================================
    /**
     * before save file to aliyun oss server, change file to inputStream
     * @param bucketName
     * @param key
     * @param file
     * */
    @Override
    public  void storageFile(String bucketName, String key, File file) {
        BufferedInputStream inputStream;
        try {
            inputStream = FileUtil.getInputStream(file);
        }catch (Exception e){
            throw new CommonException("文件流异常,{}",file.getName());
        }
        storageFile(bucketName,key,inputStream);
    }

    /**
     * before save file to aliyun oss server, change file to inputStream
     * @param bucketName
     * @param key
     * @param files
     * */
    @Override
    public  void storageFile(String bucketName, String key, MultipartFile files) {
        InputStream inputStream;
        try {
            inputStream = files.getInputStream();
        } catch (IOException e) {
            throw new CommonException("文件流异常:{}",files.getName());
        }
        storageFile(bucketName,key,inputStream);
    }

    /**
     * storage file to the oss server, but the type is byte
     * @param bucketName
     * @param key
     * @param bytes
     */
    @Override
    public  void storageFile(String bucketName, String key, byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream =null;
        try {
            initClient();
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(key);
            client.putObject(bucketName,key,byteArrayInputStream,objectMetadata);
        }catch (OSSException | ClientException e) {
            throw new CommonException("文件通过byte流上传至oss失败,{}",e.getMessage());
        }finally {
            IoUtil.close(byteArrayInputStream);
        }
    }

    public static OSS getOSsClient(){
        initClient();
        return client;
    }
    @Override
    public String getDefaultBucketName() {
        initClient();
        return defaultBucketName;
    }

    /**
     * save file to aliyun oss server
     * @param bucketName
     * @param key
     * @param inputStream
     */
    @Override
    public void storageFile(String bucketName, String key, InputStream inputStream) {
        try {
            initClient();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(key);
            client.putObject(bucketName, key, inputStream, objectMetadata);
        } catch (OSSException | ClientException e) {
            throw new CommonException("文件存储异常，{}", e.getMessage());
        } finally {
            IoUtil.close(inputStream);
        }
    }

//    /**
//     * 外网路径说明
//     * <Schema>://<Bucket>.<外网Endpoint>/<Object>
//     *  Schema：HTTP或者为HTTPS。
//     *
//     * Bucket：OSS存储空间名称。
//     *
//     * 外网Endpoint：Bucket所在数据中心供外网访问的Endpoint，各地域Endpoint详情请参见访问域名和数据中心。
//     *              eg:  oss-cn-hangzhou.aliyuncs.com
//     * Object：上传到OSS上的文件的访问路径。  eg: exampledir/example.txt
//     *
//     * eg:https://examplebucket.oss-cn-hangzhou.aliyuncs.com/exampledir/example.txt
//     */

    /**
     * 得到文件在oss存储的外网地址:文件的访问是公有读才行 有规律拼接而来的
     * @param bucketName
     * @param key
     * @return
     */
    public static String getFileAuthUrl(String bucketName, String key) {
        // eg:https://examplebucket.oss-cn-hangzhou.aliyuncs.com/exampledir/example.txt
        try {
            initClient();
            OSSClient ossClient = (OSSClient) client;
            String endPointUrl = ossClient.getEndpoint().toString();
            List<String> urlList = StrUtil.split(endPointUrl, "://");
            return urlList.get(0) +"://" +  bucketName + "." + urlList.get(1) + "/" + key;
        }catch (OSSException | ClientException e) {
            throw new CommonException("获取文件外网地址异常，{}", e.getMessage());
        }

    }

    /**
     * 根据有效时间获取文件访问的外网地址
     * @param bucketName
     * @param key
     * @param timeoutMillis
     * @return
     */
    public static String getFileAuthUrl(String bucketName, String key, long timeoutMillis){
        URL url = null;
        try {
            initClient();
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName,key,HttpMethod.GET);
            request.setExpiration(new Date(System.currentTimeMillis() + timeoutMillis));
            url = client.generatePresignedUrl(request);
        }catch (OSSException | ClientException e){
            throw new CommonException("获取文件外网地址异常，{}", e.getMessage());
        }
        return url.toString();
    }

    /**
     * 存储文件并返回文件的外网地址  注意 该方法只限于公共读权限
     * @param bucketName
     * @param key
     * @param bytes
     * @return
     */
    @Override
    public  String storageFileAndGetUrl(String bucketName, String key, byte[] bytes) {
        //1. storage
        storageFile(bucketName,key,bytes);

        //2.set file auth
        setFileAcl(bucketName,key, DevFileBucketAuthEnum.PUBLIC_READ);
        //3. return url
        return getFileAuthUrl(bucketName,key);

    }

    /**
     * 存储文件并返回文件的外网地址  注意 该方法只限于公共读权限
     * @param bucketName
     * @param key
     * @param file
     * @return
     */
    @Override
    public String storageFileAndGetUrl(String bucketName, String key, File file) {
        //1. storage
        storageFile(bucketName,key,file);

        //2.set file auth
        setFileAcl(bucketName,key,DevFileBucketAuthEnum.PUBLIC_READ);
        //3. return url
        return getFileAuthUrl(bucketName,key);
    }

    /**
     * 存储文件并返回文件的外网地址  注意 该方法只限于公共读权限
     * @param bucketName
     * @param key
     * @param files
     * @return
     */
    @Override
    public  String storageFileAndGetUrl(String bucketName, String key, MultipartFile files) {
        //1. storage
        storageFile(bucketName,key,files);

        //2.set file auth
        setFileAcl(bucketName,key,DevFileBucketAuthEnum.PUBLIC_READ);
        //3. return url
        return getFileAuthUrl(bucketName,key);
    }

    /**
     * 存储文件并返回文件的外网地址  注意 该方法只限于公共读权限
     * @param bucketName
     * @param key
     * @param inputStream
     * @return
     */
    @Override
    public  String storageFileAndGetUrl(String bucketName, String key, InputStream inputStream) {
        //1. storage
        storageFile(bucketName,key,inputStream);

        //2.set file auth
        setFileAcl(bucketName,key,DevFileBucketAuthEnum.PUBLIC_READ);
        //3. return url
        return getFileAuthUrl(bucketName,key);
    }

    /**
     * 获取某个文件的文件字节
     * @param bucketName
     * @param key
     * @return
     */
    public static byte[] getFileBytes(String bucketName, String key) {

        OSSObject ossObject =null;
        try {
            initClient();
            ossObject = client.getObject(bucketName, key);
            return  IoUtil.readBytes(ossObject.getObjectContent());
        }catch (OSSException | ClientException e) {
            throw new CommonException("获取文件字节流失败,{}",e.getMessage());
        }finally {
            IoUtil.close(ossObject);
        }

    }

    public static void destroyClient() {
        if (client!= null) {
            client.shutdown();
        }
    }

    /**
     * 判断bucket是否存在
     */
    public static boolean isBucketExist(String bucketName) {
        try {
            initClient();
            // 该方法内置存在
            client.doesBucketExist(bucketName);
            return true;
        } catch (OSSException e) {
            return false;
        }
    }

    /**
     * 设置bucket的访问权限 eg:可读可写 私有...
     * 设置预定义策略
     * 预定义策略如公有读、公有读写、私有读
     */
    public static void setBucketAcl(String bucketName, DevFileBucketAuthEnum devFileBucketAuthEnum) {
        //DevFileBucketAuthEnum.validate(devFileBucketAuthEnum.getValue());
        //CannedAccessControlList重写了toString, CannedAccessControlList.parse()得到的是CannedAccessControlList枚举类括号里面的值
        // 因为我们的DevFileBucketAuthEnum枚举类型的括号值与CannedAccessControlList不同，则此处不能用CannedAccessControlList.parse()，
        // 若 eg:DevFileBucketAuthEnum.Private改成  Private("private")则可
        try {
            initClient();
            client.setBucketAcl(bucketName, DevFileBucketAuthEnum.toCannedAccessControlList(devFileBucketAuthEnum));
        }catch (Exception e){
            throw new CommonException("bucket桶权限设置异常，{}",e.getMessage());
        }
    }

    /**
     * 设置文件的访问权限
     * @param bucketName
     * @param key
     * @param devFileBucketAuthEnum
     */
    public static void setFileAcl(String bucketName,String key,DevFileBucketAuthEnum devFileBucketAuthEnum) {
        try {
            initClient();
            client.setObjectAcl(bucketName,key, DevFileBucketAuthEnum.toCannedAccessControlList(devFileBucketAuthEnum));
        }catch (OSSException | ClientException e) {
            throw new CommonException("文件权限设置异常，{}",e.getMessage());
        }
    }

    /**
     * 判断文件是否存在
     * @param bucketName
     * @param key
     * @return
     */
    public static boolean isFileExist(String bucketName,String key){
        try {
            initClient();
            // 该方法内置存在
            return client.doesObjectExist(bucketName,key);
        }catch (OSSException | ClientException e) {
            throw new CommonException("文件不存在异常，{}",e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param bucketName
     * @param key
     */
    public static void delete(String bucketName,String key) {
        try {
            initClient();
            client.deleteObject(bucketName,key);
        }catch (OSSException | ClientException e) {
            throw new CommonException("文件删除异常，{}",e.getMessage());
        }
    }

    /**
     * 获取文件的ContentType 判断文件的类型
     */
    public static String getFileContentType(String bucketName,String key){
        //根据文件名获取ContentType
        try {
            initClient();
            return client.getObjectMetadata(bucketName,key).getContentType();
        }catch (OSSException | ClientException e) {
            throw new CommonException("获取文件ContentType异常，{}",e.getMessage());
        }
    }

    /**
     * 根据文件名获取ContentType
     * @param key
     * @return
     */
    public static String getFileContentType(String key) {
        //根据文件名获取ContentType
        String contentType = "application/octet-stream";
        if (key.contains(".")) {
            contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(key);
        }
        return contentType;
    }

    /**
     * copy file
     */
    public static void copyFile(String bucketName,String key,String newBucketName,String newKey) {
        try {
            initClient();
            client.copyObject(bucketName,key,newBucketName,newKey);
        }catch (OSSException | ClientException e) {
            throw new CommonException("文件复制异常，{}",e.getMessage());
        }
    }


}
