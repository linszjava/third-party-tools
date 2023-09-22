package com.lin.tools.third.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.lin.common.entity.CommonEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 文件
 * @TableName DEV_FILE
 */
@TableName(value ="DEV_FILE")
@Getter
@Setter
@Accessors(chain = true)
public class DevFile extends CommonEntity {


    /** id */
    @ApiModelProperty(value = "主键", position = 1)
    private String id;

    /** 存储引擎 */
    @ApiModelProperty(value = "存储引擎", position = 2)
    private String engine;

    /** 存储桶 */
    @ApiModelProperty(value = "存储桶", position = 3)
    private String bucket;

    /** 文件名称 */
    @ApiModelProperty(value = "文件名称", position = 4)
    private String name;

    /** 文件后缀 */
    @ApiModelProperty(value = "文件后缀", position = 5)
    private String suffix;

    /** 文件大小kb */
    @ApiModelProperty(value = "文件大小kb", position = 6)
    private String sizeKb;

    /** 文件大小（格式化后） */
    @ApiModelProperty(value = "文件大小（格式化后）", position = 7)
    private String sizeInfo;

    /** 文件的对象名（唯一名称） */
    @ApiModelProperty(value = "文件的对象名（唯一名称）", position = 8)
    private String objName;

    /** 文件存储路径 */
    @ApiModelProperty(value = "文件存储路径", position = 9)
    private String storagePath;

    /** 文件下载路径 */
    @ApiModelProperty(value = "文件下载路径", position = 10)
    private String downloadPath;

    /** 图片缩略图 */
    @ApiModelProperty(value = "图片缩略图", position = 11)
    private String thumbnail;

    /** 扩展信息 */
    @ApiModelProperty(value = "扩展信息", position = 12)
    private String extJson;
}