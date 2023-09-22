package com.lin.common.result;

import com.lin.common.result.enums.CommonResultEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/19 10:28
 */
@ToString
@Data
@AllArgsConstructor
public class CommonResult<T> implements Serializable {

    public static final long serialVerApnID = 1L;

    @ApiModelProperty(value = "状态码")
    private int code;

    @ApiModelProperty(value = "返回信息")
    private String msg;

    @ApiModelProperty(value = "返回数据")
    private T data;

    public CommonResult<T> setCode(int code){
        this.code = code;
        return this;
    }

    public CommonResult<T> setMsg(String msg){
        this.msg = msg;
        return this;
    }

    public CommonResult<T> setData(T data){
        this.data = data;
        return this;
    }

    // ============================成功===============================

    public static <T>CommonResult<T> ok(){
        return new CommonResult<>(CommonResultEnum.CODE_SUCCESS.getCode(),
                CommonResultEnum.CODE_SUCCESS.getDesc(), null);
    }

    public static <T>CommonResult<T> ok(String msg){
        return new CommonResult<>(CommonResultEnum.CODE_SUCCESS.getCode(),
                msg, null);
    }

    public static <T>CommonResult<T> ok(int code){
        return new CommonResult<>(code,
                null, null);
    }
    public static <T>CommonResult<T> ok(int code, String msg){
        return new CommonResult<>(code,
                msg, null);
    }
    public static <T>CommonResult<T> ok(T data){
        return new CommonResult<>(CommonResultEnum.CODE_SUCCESS.getCode(),
                CommonResultEnum.CODE_SUCCESS.getDesc(), data);
    }

    public static <T> CommonResult<T> data(T data){
        return new CommonResult<>(CommonResultEnum.CODE_SUCCESS.getCode(),
                CommonResultEnum.CODE_SUCCESS.getDesc(), data);
    }

    public static <T> CommonResult<T> code(int code){
        return new CommonResult<>(code,
                null, null);
    }

//    =============================构建失败==========================================

    public static <T>CommonResult<T> error(){
        return new CommonResult<>(CommonResultEnum.CODE_ERROR.getCode(), CommonResultEnum.CODE_ERROR.getDesc(), null);
    }

    public static <T>CommonResult<T> error(int code){
        return new CommonResult<>(code, CommonResultEnum.CODE_ERROR.getDesc(), null);
    }
    public static <T>CommonResult<T> error(String msg){
        return new CommonResult<>(CommonResultEnum.CODE_ERROR.getCode(), msg, null);
    }

    public static <T>CommonResult<T> get(int code,String msg,T data){
        return new CommonResult<>(code,msg,data);
    }



}
