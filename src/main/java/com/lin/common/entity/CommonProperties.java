package com.lin.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/19 13:09
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sys.config.common")
public class CommonProperties {

    /**
     * 前端地址
     */
    private String frontUrl;

    /**
     * 后端地址
     */
    private String backendUrl;
}