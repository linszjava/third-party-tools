package com.lin.tools.third.config.service;

import com.lin.tools.third.config.entity.DevConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author linsz
* @description 针对表【DEV_CONFIG(配置)】的数据库操作Service
* @createDate 2023-09-19 12:29:03
*/
public interface DevConfigService extends IService<DevConfig> {


    /**
     * 根据键获取值 ：从数据库中获取key所对应的值，不用redis缓存
     * @param key
     * @return
     */
    String getValueByKey(String key);

}
