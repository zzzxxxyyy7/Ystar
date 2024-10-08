package com.ystar.user.api.Vo;

import lombok.Data;

@Data
public class HomePageVO {

    private boolean loginStatus;

    private long userId;

    private String nickName;

    private String avatar;

    //是否是主播身份
    private boolean showStartLivingBtn;
}