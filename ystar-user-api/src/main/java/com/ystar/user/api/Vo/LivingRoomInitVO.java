package com.ystar.user.api.Vo;

import lombok.Data;

@Data
public class LivingRoomInitVO {

    private Long anchorId;

    private Long userId;

    private String anchorImg;

    private String roomName;

    private boolean isAnchor;

    private String avatar;

    private Integer roomId;

    private String watcherNickName;

    private String anchorNickName;

    // 昵称
    private String nickName;

    //观众头像
    private String watcherAvatar;

    //默认背景图，为了方便讲解使用
    private String defaultBgImg;

    private Long pkObjId;
}