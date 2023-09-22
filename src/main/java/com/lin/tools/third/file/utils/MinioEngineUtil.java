package com.lin.tools.third.file.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lin.common.exception.CommonException;
import com.lin.tools.third.config.provider.DevConfigProvider;
import com.lin.tools.third.file.constant.DevFileConst;
import com.lin.tools.third.file.enums.DevFileBucketAuthEnum;
import com.lin.tools.third.file.strategy.FileEngine;
import io.minio.*;
import io.minio.http.Method;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * <p>TODO</p>
 * tips: 如果出现 minio内部出错但不提醒
 * java.util.concurrent.ExecutionException: error occurred
 * ErrorResponse(code = RequestTimeTooSkewed, message = The difference between the request time and the server's time is too large., bucketName = null, objectName = null, resource = /linsz-bucket, requestId = 17840635A63B7271, hostId = dd9025bab4ad464b049177c95eb6ebf374d3b3fd1af9251148b658df7ac2e3e8)
 * request={method=GET, url=http://192.168.55.130:9000/linsz-bucket?location=, headers=Host: 192.168.55.130:9000
 * Accept-Encoding: identity
 * User-Agent: MinIO (Mac OS X; x86_64) minio-java/8.5.2
 * Content-MD5: 1B2M2Y8AsgTpgAmY7PhCfg==
 * x-amz-content-sha256: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
 * x-amz-date: 20230920T061809Z
 * Authorization: ██
 * }
 * response={code=403, headers=Accept-Ranges: bytes
 * Content-Length: 339
 * Content-Type: application/xml
 * Server: MinIO
 * Strict-Transport-Security: max-age=31536000; includeSubDomains
 * Vary: Origin
 * X-Amz-Id-2: dd9025bab4ad464b049177c95eb6ebf374d3b3fd1af9251148b658df7ac2e3e8
 * X-Amz-Request-Id: 17840635A63B7271
 * X-Content-Type-Options: nosniff
 * X-Xss-Protection: 1; mode=block
 * Date: Tue, 12 Sep 2023 02:36:27 GMT
 * }
 *原因： 服务器时间和客户端时间相差太大
 * 解决： 需要服务器同步时间
 * @author linsz
 * @version v1.0
 * @date 2023/9/15 03:31
 */
public class MinioEngineUtil implements FileEngine {

    private static MinioClient client;


    /**
     *
     *  get default bucket name
     */
    private static String defaultBucketName;

    private static void initClient() {

        DevConfigProvider devConfigProvider = SpringUtil.getBean(DevConfigProvider.class);

        String endPoint = devConfigProvider.getValueByKey(DevFileConst.FILE_MINIO_END_POINT_KEY);

        String accessKey = devConfigProvider.getValueByKey(DevFileConst.FILE_MINIO_ACCESS_KEY_KEY);

        String secretKey = devConfigProvider.getValueByKey(DevFileConst.FILE_MINIO_SECRET_KEY_KEY);

        defaultBucketName = devConfigProvider.getValueByKey(DevFileConst.FILE_MINIO_DEFAULT_BUCKET_NAME);

        if (ObjectUtil.isEmpty(defaultBucketName)){
            throw new CommonException("MinioClient配置客户端存在错误，请检查{}的值","FILE_MINIO_DEFAULT_BUCKET_NAME");
        }
        client = MinioClient.builder().endpoint(endPoint).credentials(accessKey, secretKey).build();

    }


    public static MinioClient getClient(){
        initClient();
        return client;
    }


    public static void destroyClient(){
        // delete
    }

    /**
     * 判断bucket是否存在
     */
    public static boolean isBucketExist(String bucketName) {
        try {
            initClient();
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
            return client.bucketExists(bucketExistsArgs);
        }catch (Exception e) {
            throw new CommonException("bucketExist判断存在错误 ,{}",e.getMessage());
        }
    }

    /**
     * 判断文件是否存在
     * @param bucketName
     * @return
     */
    public static boolean isFileExist(String bucketName) {
        try {
            initClient();
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketName).build();
            GetObjectResponse object = client.getObject(getObjectArgs);
            return ObjectUtil.isNotEmpty(object);
        }catch (Exception e) {
            return false;
        }
    }

    /**
     * 文件访问策略
     * @param bucketName
     * @param key
     * @param devFileBucketAuthEnum
     *
     *
     *  Assume policyJson contains below JSON string;
     *  {
     *      "Statement": [
     *          {
     *              "Action": [
     *                  "s3:GetBucketLocation",
     *                  "s3:ListBucket"
     *              ],
     *              "Effect": "Allow",
     *              "Principal": "*",
     *              "Resource": "arn:aws:s3:::my-bucketname"
     *          },
     *          {
     *              "Action": "s3:GetObject",
     *              "Effect": "Allow",
     *              "Principal": "*",
     *              "Resource": "arn:aws:s3:::my-bucketname/myobject*"
     *          }
     *      ],
     *      "Version": "2012-10-17"
     *  }
     */
    public static void setBucketPolicy(String bucketName, String key, DevFileBucketAuthEnum devFileBucketAuthEnum) {
        try {
            initClient();
            JSONArray actionArr =null;
            if (devFileBucketAuthEnum.equals(DevFileBucketAuthEnum.PUBLIC_READ)){
                actionArr = JSONUtil.createArray().put("s3:GetObject");
            } else if (devFileBucketAuthEnum.equals(DevFileBucketAuthEnum.PUBLIC_READ_WRITE)) {
                actionArr = JSONUtil.createArray().put("s3:GetObject").put("s3:PutObject");
            }
            // 包装Statement里面的内容
            JSONArray statementArray = JSONUtil.createArray();
            statementArray.put(0,JSONUtil.createObj().set("Action",actionArr)
                    .set("Effect","Allow")
                    .set("Principal",JSONUtil.createObj().set("AWS", JSONUtil.createArray().put("*")))
                    .set("Resource",JSONUtil.createArray().put("arn:aws:s3:::"+bucketName+"/*")));

            JSONObject configObj = JSONUtil.createObj();
            configObj.set("Statement",statementArray);
            configObj.set("Version","2012-10-17");
            String config = JSONUtil.toJsonStr(configObj);
            SetBucketPolicyArgs setBucketPolicyArgs = SetBucketPolicyArgs.builder().bucket(bucketName).config(config).build();
            client.setBucketPolicy(setBucketPolicyArgs);
        }catch (Exception e) {
            throw new CommonException("setBucketPolicy错误,{}",e.getMessage());
        }
    }

    /**
     * 设置桶权限
     * @param bucketName
     * @param key
     * @param devFileBucketAuthEnum
     */
    public static void setBucketAcl(String bucketName,String key, DevFileBucketAuthEnum devFileBucketAuthEnum){
        setBucketPolicy(bucketName,"*",devFileBucketAuthEnum);
    }

    //==========================存储文件，不返回地址====================================

    /**
     * 上传文件
     * */
    public void storageFile(String bucketName, String key, File file) {
        BufferedInputStream inputStream;
        try {
            inputStream = FileUtil.getInputStream(file);
        }catch (Exception e) {
            throw new CommonException("file流存在错误,{}",file.getName());
        }
        // 流的关闭在下方函数执行
        storageFile(bucketName,key,inputStream);
    }
    /**
     * 上传文件
     * */
    public void storageFile(String bucketName, String key, MultipartFile files) {
        InputStream inputStream;
        try {
            inputStream = files.getInputStream();
        }catch (Exception e) {
            throw new CommonException("file流存在错误,{}",files.getName());
        }
        storageFile(bucketName,key,inputStream);
    }

    @Override
    public String getDefaultBucketName() {
        return defaultBucketName;
    }

    /**
     * 上传文件
     * */
    public  void storageFile(String bucketName, String key, InputStream inputStream) {
        try {
            initClient();
            //  If object size is known, pass -1 to partSize for auto detect;
            PutObjectArgs putObjetArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .contentType(getFileContentType(key))
                    .stream(inputStream, inputStream.available(), -1)
                    .build();
            client.putObject(putObjetArgs);
        }catch (Exception e) {
            throw new CommonException("storageFile错误,{}",e.getMessage());
        }finally {
            IoUtil.close(inputStream);
        }
    }
    /**
     * 上传文件
     * */
    public void storageFile(String bucketName, String key, byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream =null;
        try {
            initClient();
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .contentType(getFileContentType(key))
                    .stream(byteArrayInputStream, bytes.length, -1)
                    .build();
            client.putObject(putObjectArgs);
        }catch (Exception e) {
            throw new CommonException("storageFile错误,{}",e.getMessage());
        }finally {
            IoUtil.close(byteArrayInputStream);
        }
    }

    //==========================存储文件，  返回地址====================================
    //    https://min.io/docs/minio/linux/developers/java/API.html
    //    https://minio-java.min.io/

    /**
     * 存储文件并返回文件的地址
     * @param bucketName
     * @param key
     * @param file
     * @return
     */
    public  String storageFileAndGetUrl(String bucketName, String key, File file) {
        //1. storage file
        storageFile(bucketName, key, file);
        //2. set acl
        setBucketAcl(bucketName,key,DevFileBucketAuthEnum.PUBLIC_READ);
        //3. get file url
        return getFileAbsolutePath(bucketName, key);
    }

    /**
     * 存储文件并返回文件的地址
     * @param bucketName
     * @param key
     * @param files
     * @return
     */
    public String storageFileAndGetUrl(String bucketName, String key, MultipartFile files) {
        // 1. storage file
        storageFile(bucketName, key, files);
        // 2. set acl
        setBucketAcl(bucketName, key, DevFileBucketAuthEnum.PUBLIC_READ);
        // 3. get file url
        return getFileAbsolutePath(bucketName, key);
    }

    /**
     * 存储文件并返回文件的地址
     * @param bucketName
     * @param key
     * @param bytes
     * @return
     */
    public String storageFileAndGetUrl(String bucketName, String key, byte[] bytes) {
        //  1. storage file
        storageFile(bucketName, key, bytes);
        // 2. set acl
        setBucketAcl(bucketName, key, DevFileBucketAuthEnum.PUBLIC_READ);
        // 3. get file url
        return getFileAbsolutePath(bucketName, key);

    }

    /**
     * 存储文件并返回文件的地址
     * @param bucketName
     * @param key
     * @param inputStream
     * @return
     */
    public  String storageFileAndGetUrl(String bucketName, String key, InputStream inputStream) {
        // 1. storage file
        storageFile(bucketName, key, inputStream);
        // 2. set acl
        setBucketAcl(bucketName, key, DevFileBucketAuthEnum.PUBLIC_READ);
        // 3. get file url
        return getFileAbsolutePath(bucketName, key);
    }

    /**
     * 获取文件的绝对地址
     * @param bucketName
     * @param key
     * @return
     */
    public String getFileAbsolutePath(String bucketName, String key){
        // get eg:  http://play.mio.io:9000/<bucketName>/<key>
        // 其中 http://play.mio.io:9000  == Endpoint
        try{
            DevConfigProvider devConfigProvider = SpringUtil.getBean(DevConfigProvider.class);
            String endPoint = devConfigProvider.getValueByKey(DevFileConst.FILE_MINIO_END_POINT_KEY);
            String path = endPoint + "/" + bucketName + "/" + key;
            return path;
        }catch (Exception e) {
            throw new CommonException("getFileAbsolutePath错误,{}",e.getMessage());
        }
    }

    /**
     * 获取文件的绝对地址 带鉴权和有效时间过期
     * @param bucketName
     * @param key
     * @param timeoutMills
     * @return
     */
    public String getFileAbsolutePath(String bucketName, String key, Long timeoutMills) {
        try {
            initClient();
            GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .method(Method.GET)
                    .expiry(timeoutMills.intValue())
                    .build();
            return client.getPresignedObjectUrl(getPresignedObjectUrlArgs);
        }catch (Exception e) {
            throw new CommonException("getFileAbsolutePath错误,{}", e.getMessage());
        }
    }

    /**
     * 复制文件
     */
    public void copyFile(String bucketName, String key,String newBucketName, String newKey) {
        try {
            initClient();
            // 判断新文件的归属地是否和原来的相同
            validateNewFilePositionRegularly(bucketName, key ,newBucketName, newKey);
            CopyObjectArgs copyObjectArgs = CopyObjectArgs.builder()
                    .bucket(newBucketName)
                    .object(newKey)
                    .source(CopySource.builder().bucket(bucketName).object(key).build())
                    .build();
            client.copyObject(copyObjectArgs);
        }catch (Exception e) {
            throw new  CommonException("copyFile错误,{}", e.getMessage());
        }
    }

    /**
     * 校验新文件的位置和存储的位置是否一致
     * @param oldBucketName
     * @param oldKey
     * @param newBucketName
     * @param newKey
     */
    private void validateNewFilePositionRegularly(String oldBucketName, String oldKey, String newBucketName, String newKey) {
        if (oldBucketName.equals(newBucketName) && oldKey.equals(newKey)) {
            throw new CommonException("新文件和旧文件地址相同");
        }
    }


    /**
     * 删除文件
     */
    public static boolean deleteFile(String bucketName, String key) {
        try {
            initClient();
            client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(key).build());
        }catch (Exception e) {
            throw new CommonException("deleteFile错误,{}", e.getMessage());
        }
        return true;
    }


    /**
     * get file bytes
     */
    public static byte[] getFileBytes(String bucketName, String key) {
        try {
            initClient();
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(key).build();
            InputStream inputStream = client.getObject(getObjectArgs);
            return IoUtil.readBytes(inputStream);
        }catch (Exception e) {
            throw new CommonException("getFileBytes错误,{}", e.getMessage());
        }
    }

    /**
     * 根据文件名获取文件的ContenType
     *
     * @param key
     * @return
     * **/
    private static String getFileContentType(String key) {
        // 根据文件名获取contentType
        String contentType = "application/octet-stream";
        if (key.contains(".")) {
            contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(key);
        }
        return contentType;
    }
}
