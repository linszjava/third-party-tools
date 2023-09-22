package com.lin.tools.third.config.provider;

import com.lin.tools.third.config.service.DevConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/19 12:38
 */
@Component
public class DevConfigProvider {

    @Autowired
    private DevConfigService devConfigService;

    /**
     * 向其他模块提供调用config表中查询字段的方法
      * @param key
     * @return
     */
    public String getValueByKey(String key) {
        return devConfigService.getValueByKey(key);
    }
}
