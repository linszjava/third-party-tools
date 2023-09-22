package com.lin.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/20 00:44
 */
@Configuration
@MapperScan("com.lin.tools.third.*.mapper")
public class Configure {

    /**
     * knife4j配置： xml配置方式
     */
}
