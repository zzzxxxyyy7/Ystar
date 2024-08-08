package com.ystar.user.api.Vo;

import lombok.Data;

@Data
public class GiftReqVO {
    
    private int giftId;
    private Integer roomId;
    private Long senderUserId;
    private Long receiverId;
    private Integer type;
}
