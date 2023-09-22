package com.lin.tools.third.config.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.exception.CommonException;
import com.lin.tools.third.config.entity.DevConfig;
import com.lin.tools.third.config.service.DevConfigService;
import com.lin.tools.third.config.mapper.DevConfigMapper;
import org.springframework.stereotype.Service;

/**
* @author linsz
* @description 针对表【DEV_CONFIG(配置)】的数据库操作Service实现
* @createDate 2023-09-19 12:29:03
*/
@Service
public class DevConfigServiceImpl extends ServiceImpl<DevConfigMapper, DevConfig>
    implements DevConfigService{

    /**
     * 根据键获取值 ：从数据库中获取key所对应的值，不用redis缓存
     *
     * @param key
     * @return
     */
    @Override
    public String getValueByKey(String key) {
        QueryWrapper<DevConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DevConfig::getConfigKey,key);
        DevConfig devConfig = this.getOne(queryWrapper);
        if(ObjectUtil.isEmpty(devConfig)){
            throw new CommonException("配置key不存在,key:{}",key);
        }
        return devConfig.getConfigValue();
    }
}




