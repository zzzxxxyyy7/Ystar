package com.ystar.user.api.Vo;

import lombok.Data;

import java.util.Date;

@Data
public class GiftConfigVO {

    private Integer giftId;
    private Integer price;
    private String giftName;
    private Integer status;
    private String coverImgUrl;
    private String svgaUrl;
    private Date createTime;
    private Date updateTime;
}
