package com.lin.common.exception;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/19 12:23
 */
@Getter
@Setter
@Accessors(chain = true)
public class CommonException extends RuntimeException {

    private Integer code;

    private String msg;

    public CommonException(){
        super("服务器异常");
        this.code = 500;
        this.msg = "服务器异常";

    }

    public CommonException(String msg, Object... args){
        super(StrUtil.format(msg,args));
        this.code = 500;
        this.msg = StrUtil.format(msg,args);
    }

    public CommonException(String msg,Integer code, Object... args){
        super(StrUtil.format(msg,args));
        this.code = code;
        this.msg = StrUtil.format(msg,args);
    }






}