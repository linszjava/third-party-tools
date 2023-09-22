package com.lin.common.result.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/19 10:40
 */
@AllArgsConstructor
@Getter
public enum CommonResultEnum {
    CODE_SUCCESS(200,"操作成功"),
    CODE_ERROR(500,"操作失败");




    private final int code;

    private final String desc;
}
