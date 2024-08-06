package com.ystar.user.api.Vo;

import lombok.Data;

/**
 * 前端建立 IM 连接返回的视图对象
 */
@Data
public class ImConfigVO {
    
    private String token;
    private String wsImServerAddress;
    private String tcpImServerAddress;
}