package com.ystar.common.Dto;

import lombok.Data;

/**
 * 直播间送礼模块 MQ 削峰消息体
 */
@Data
public class SendGiftMq {

    private Long userId;
    private Integer giftId;
    private Integer price;
    private Long receiverId;
    private Integer roomId;
    private String url;
    private String uuid;
    private Integer type;
}