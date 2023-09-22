package com.lin.tools.third.file.enums;

import com.aliyun.oss.model.CannedAccessControlList;
import com.lin.common.exception.CommonException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/4 20:44
 */
@Getter
@AllArgsConstructor
public enum DevFileBucketAuthEnum {

    /**
     * 私有的（仅有 owner 可以读写）
     */
    PRIVATE("Private"),

    /**
     * 公有读，私有写（ owner 可以读写， 其他客户可以读）
     */
    PUBLIC_READ("PublicRead"),

    /**
     * 公共读写（即所有人都可以读写，慎用）
     */
    PUBLIC_READ_WRITE("PublicReadWrite");


    private String value;


    /**
     * 校验文件读写权限类型
     *
     * @param authType
     * @return
     */
    public static boolean validate(String authType) {
        for (DevFileBucketAuthEnum auth : DevFileBucketAuthEnum.values()) {
            if (!auth.getValue().equals(authType)) {
                throw new CommonException("文件读写权限类型有误,{}", authType);
            }
        }
        return true;
    }


    /**
     * 根据输入的enum类型，得到CannedAccessControlList相应的种类，
     * 注意CannedAccessControlList只是名字取得带List,实际上是一个类
     * eg:因为重写了toString, CannedAccessControlList.Private 得到的是它Private枚举括号的值
     *
     * @param devFileBucketAuthEnum
     * @return
     */
    public static CannedAccessControlList toCannedAccessControlList(DevFileBucketAuthEnum devFileBucketAuthEnum) {
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


}