package com.lin.tools.third.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.lin.common.entity.CommonEntity;
import lombok.Data;

/**
 * 配置
 * @TableName DEV_CONFIG
 */
@TableName(value ="DEV_CONFIG")
@Data
public class DevConfig extends CommonEntity {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 分类
     */
    private String category;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序码
     */
    private Integer sortCode;

    /**
     * 扩展信息
     */
    private String extJson;

}