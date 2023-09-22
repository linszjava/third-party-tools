package com.lin.tools.third.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lin.tools.third.config.entity.DevConfig;
import com.lin.tools.third.config.mapper.DevConfigMapper;
import com.lin.tools.third.config.provider.DevConfigProvider;
import com.lin.tools.third.config.service.DevConfigService;
import com.lin.tools.third.file.constant.DevFileConst;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/20 06:23
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private DevConfigMapper devConfigMapper;

    @Autowired
    private DevConfigService devConfigService;

    @Autowired
    private DevConfigProvider devConfigProvider;


    /**
     * 测试mybatis-plus
     */
    @GetMapping("/testMybatisPlus")
    public String testMybatisPlus() {
        QueryWrapper<DevConfig> queryWrapper = new QueryWrapper<>();
        Long count = devConfigMapper.selectCount(queryWrapper);
        return count.toString();

    }

    @GetMapping("/testGetValueByKey")
    public String testGetValueByKey(){
        return devConfigService.getValueByKey(DevFileConst.FILE_LOCAL_FOLDER_FOR_UNIX_KEY);
    }

    @GetMapping("/testGetValueByKey2")
    public String testGetValueByKey2(){
        return devConfigProvider.getValueByKey(DevFileConst.FILE_MINIO_END_POINT_KEY);
    }


}
