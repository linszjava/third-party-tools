package com.lin.tools.third.file.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import com.lin.common.exception.CommonException;
import com.lin.tools.third.config.provider.DevConfigProvider;
import com.lin.tools.third.file.constant.DevFileConst;
import com.lin.tools.third.file.enums.DevFileBucketAuthEnum;
import com.lin.tools.third.file.strategy.FileEngine;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/15 03:32
 */
public class LocalEngineUtil implements FileEngine {

    /**
     * 存放文件路径路径的jsonObject
     * -- GETTER --
     *   得到客户端
     *
     * @return

     */
    @Getter  // 得到client
    private static JSONObject client;


    /**
     * 默认bucketName
     */
    private static String defaultBucketName = "defaultBucketName";


    /**
     * 判断系统类型，得到文件路径并赋值给client
     */
    static {

        DevConfigProvider devConfigProvider = SpringUtil.getBean(DevConfigProvider.class);
         //上传文件目录
        String uploadFileFolder = "";

        //1. 判断系统类型
        if (SystemUtil.getOsInfo().isWindows()) {
            //windows
            String fileLocalWindowsPath = devConfigProvider.getValueByKey(DevFileConst.FILE_LOCAL_FOLDER_FOR_WINDOWS_KEY);
            if (ObjUtil.isEmpty(fileLocalWindowsPath)){
                throw new CommonException("本地文件路径配置存在问题,核实{}的值","FILE_LOCAL_FOLDER_FOR_WINDOWS_KEY");
            }
            uploadFileFolder = fileLocalWindowsPath;
        }else {
            // linux or macOS
            String fileLocalLinuxPath = devConfigProvider.getValueByKey(DevFileConst.FILE_LOCAL_FOLDER_FOR_UNIX_KEY);
            System.out.println("fileLocalLinuxPath: " + fileLocalLinuxPath);
            if (ObjUtil.isEmpty(fileLocalLinuxPath)){
                throw new CommonException("本地文件路径配置存在问题,核实{}的值","FILE_LOCAL_FOLDER_FOR_UNIX_KEY");
            }
            uploadFileFolder = fileLocalLinuxPath;

        }
        //2. 判断uploadFileFolder是否存在
        if (!FileUtil.exist(uploadFileFolder)){
            FileUtil.mkdir(uploadFileFolder);
        }

        client = JSONUtil.createObj();
        client.set("uploadFileFolder", uploadFileFolder);
    }

    /**
     * 删除client
     * @return
     */
    public static void deleteClient() {
        client.clear();
    }

    /**
     * 获取上传地址
     * @return
     */
    public static String getUploadFileFolder(){
        return client.getStr("uploadFileFolder");
    }

    /**
     * 获得上传路径+key
     * @param key
     * @return
     */
    public static String getPathAddKey(String key) {
        //eg:   /tmp/xxx/key     or   D:/temp/xxx/key
        return getUploadFileFolder() + FileUtil.FILE_SEPARATOR + key;
    }

    /**
     * 获得带bucketName的绝对路径
     * @param bucketName
     * @param key
     * @return
     */
    public static String getPathWithBucketNameAddKey(String bucketName,String key) {
        //eg:   /tmp/xxx/bucketName/a.txt     or   D:/temp/xxx/bucketName/a.txt
        return getUploadFileFolder() + FileUtil.FILE_SEPARATOR + bucketName + FileUtil.FILE_SEPARATOR + key;
    }

    /**
     * 判断bucketName是否存在
     * @param bucketName
     * @return
     */
    public static boolean isBucketExist(String bucketName){
        return FileUtil.exist(getPathAddKey(bucketName));

    }

    /**
     * 判断文件是否存在
     * @param bucketName
     * @param fileName
     * @return
     * */
    public static boolean isFileExist(String bucketName,String fileName) {
        return FileUtil.exist(getPathWithBucketNameAddKey(bucketName,fileName));
    }



    /**
     * 预先设定的bucket读写策略
     * @param bucketName
     * @param devFileBucketAuthEnum
     */
    public static void setBucketAcl(String bucketName, DevFileBucketAuthEnum devFileBucketAuthEnum) {
        // TODO
    }

    //    ===============START==========存储文件并无返回路径==========START==============================

    @Override
    public String getDefaultBucketName() {
        return defaultBucketName;
    }

    /**
     * 存储文件 无返回值
     */
    public  void storageFile(String bucketName, String key, InputStream inputStream) {
        FileUtil.writeFromStream(inputStream,getPathWithBucketNameAddKey(bucketName, key));

    }

    /**
     * 存储文件 无返回值
     */
    public  void storageFile(String bucketName,String key,byte[] bytes) {
        FileUtil.writeBytes(bytes,getPathWithBucketNameAddKey(bucketName, key));

    }

    /**
     * 存储文件 无返回值
     * @param bucketName key file
     */
    public  void storageFile(String bucketName, String key, File file) {
        BufferedInputStream inputStream;
        try {
            inputStream = FileUtil.getInputStream(file);
        }catch (IORuntimeException e) {
            throw new CommonException("获取文件流失败,名称是:{}",file.getName());
        }
        storageFile(bucketName,key,inputStream);

    }

    /**
     * 存储文件 无返回值
     * @param bucketName key file
     */
    public  void storageFile(String bucketName, String key, MultipartFile files) {
        InputStream inputStream;
        try {
            inputStream  = files.getInputStream();
        } catch (IOException e) {
            throw new CommonException("获取文件流失败,名称是:{}",files.getName());
        }
        storageFile(bucketName,key,inputStream);

    }

    //    ===============END==========存储文件并无返回路径==========END==============================

    /**
     * 文件访问权限
     */
    public static void setFileAcl() {
        // TODO
    }

    /**
     * get file through bucketName and key
     */
    public static File getFileByBucketNameAndKey(String bucketName, String key) {
        String path = getPathWithBucketNameAddKey(bucketName, key);
        File file = FileUtil.file(path);
        if (ObjectUtil.isEmpty(file)){
            throw new CommonException("文件不存在:{}",path);
        }
        return file;
    }

    /**
     * delete the file selected
     * @param bucketName
     * @param key
     */
    public static void deleteFile(String bucketName, String key) {
        FileUtil.del(getFileByBucketNameAndKey(bucketName, key));
    }

    /**
     * get file absolute path
     * tips: getPathWithBucketNameAddKey() just returns the path we created,
     *    but we do not know whether it exists, but this one we can get the real path, if it's not
     *    exist, exception will be thrown.
     * @param bucketName
     * @param key
     * @return absolute path
     *
     * */
    public static String getFileAbsolutePath(String bucketName, String key) {
        File file = getFileByBucketNameAndKey(bucketName, key);
        return file.getAbsolutePath();
    }

    /**
     * copy file from original path to another.
     * @param originalBucketName
     * @param originalKey
     * @param newBucketName
     * */
    public static void copyFile(String originalBucketName,String originalKey,
                                 String newBucketName,String newKey) {
        File originalFile = getFileByBucketNameAndKey(originalBucketName, originalKey);
        File newfile = FileUtil.file(getPathWithBucketNameAddKey(newBucketName, newKey));
        FileUtil.copy(originalFile,newfile,true);
    }

    /**
     * 获取某个文件的字节
     * @param bucketName
     * @param key
     * @return
     */
    public static byte[] getFileBytes(String bucketName, String key) {
        File file = getFileByBucketNameAndKey(bucketName, key);
        return FileUtil.readBytes(file);
    }


//    ========================存储文件并且获取文件的绝对路径===================================
    /**
     * 存储文件并且获取文件的绝对路径
     */
    @Override
    public String storageFileAndGetUrl(String bucketName, String key, InputStream inputStream) {
        storageFile(bucketName,key,inputStream);
        return getFileAbsolutePath(bucketName,key);
    }

    @Override
    public String storageFileAndGetUrl(String bucketName, String key, File file) {
        storageFile(bucketName,key,file);
        return getFileAbsolutePath(bucketName,key);
    }

    @Override
    public String storageFileAndGetUrl(String bucketName, String key, byte[] bytes) {
        storageFile(bucketName,key,bytes);
        return getFileAbsolutePath(bucketName,key);
    }

    @Override
    public String storageFileAndGetUrl(String bucketName, String key, MultipartFile multipartFile) {
        storageFile(bucketName,key,multipartFile);
        return getFileAbsolutePath(bucketName,key);
    }
}
