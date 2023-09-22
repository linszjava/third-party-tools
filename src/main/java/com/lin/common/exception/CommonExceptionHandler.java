package com.lin.common.exception;

import com.lin.common.result.CommonResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/20 09:29
 */
@ControllerAdvice
public class CommonExceptionHandler {


    @ExceptionHandler(CommonException.class)
    public CommonResult handleException(Exception e) {
        return CommonResult.error("CommonException异常："+e.getMessage());
    }
}
