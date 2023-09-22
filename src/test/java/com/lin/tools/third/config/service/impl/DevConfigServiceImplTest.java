package com.lin.tools.third.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lin.tools.third.config.entity.DevConfig;
import com.lin.tools.third.config.mapper.DevConfigMapper;
import com.lin.tools.third.config.service.DevConfigService;
import com.lin.tools.third.file.constant.DevFileConst;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class DevConfigServiceImplTest {

    @Autowired
    private DevConfigService devConfigService;

    @Autowired
    private DevConfigMapper devConfigMapper;
    @Test
    void getValueByKey() {
        System.out.println(devConfigService.getValueByKey(DevFileConst.FILE_LOCAL_FOLDER_FOR_UNIX_KEY));
    }


    @Test
    void testGetKey(){
        String key = "FILE_LOCAL_FOLDER_FOR_UNIX";
        QueryWrapper<DevConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DevConfig::getConfigKey,key);
        DevConfig devConfig = devConfigMapper.selectOne(queryWrapper);
        System.out.println(devConfig);
    }

    @Test
    void testMapper(){
        QueryWrapper<DevConfig> queryWrapper = new QueryWrapper<>();
        Long count = devConfigMapper.selectCount(queryWrapper);
        System.out.println(count);
    }


    @Test
    public void test(){
        System.out.println("hello world");
    }
}