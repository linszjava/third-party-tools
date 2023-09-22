package com.lin.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>TODO</p>
 *
 * @author linsz
 * @version v1.0
 * @date 2023/9/19 11:56
 */
@Setter
@Getter
public class CommonEntity implements Serializable {

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "删除标志",position = 999)
    private String deleteFlag;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间",position = 1000)
    private Date createTime;


    @TableField(fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "修改时间", position = 1002)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人",position = 1001)
    private String createUser;

    @TableField(fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "修改者",position = 1003)
    private String updateUser;

}

