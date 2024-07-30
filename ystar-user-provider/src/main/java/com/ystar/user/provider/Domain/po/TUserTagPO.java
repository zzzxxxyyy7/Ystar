package com.ystar.user.provider.Domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户标签记录
 * @TableName t_user_tag_00
 */
@TableName(value ="t_user_tag")
@Data
public class TUserTagPO implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private Long userId;

    /**
     * 标签记录字段
     */
    @TableField(value = "tag_info_01")
    private Long tagInfo01;

    /**
     * 标签记录字段
     */
    @TableField(value = "tag_info_02")
    private Long tagInfo02;

    /**
     * 标签记录字段
     */
    @TableField(value = "tag_info_03")
    private Long tagInfo03;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}