package com.ystar.user.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserTagDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 4079363033448860398L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 标签记录字段
     */
    private Long tagInfo01;

    /**
     * 标签记录字段
     */
    private Long tagInfo02;

    /**
     * 标签记录字段
     */
    private Long tagInfo03;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
