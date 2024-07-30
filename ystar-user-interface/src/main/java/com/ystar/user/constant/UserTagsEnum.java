package com.ystar.user.constant;


public enum UserTagsEnum {

    IS_RICH((long) Math.pow(2, 0), "一诺千金", "tag_info_01"),
    IS_VIP((long) Math.pow(2, 1), "VIP", "tag_info_01"),
    IS_SVIP((long) Math.pow(2, 3), "SVIP", "tag_info_01"),
    IS_YSTAR_STAR((long) Math.pow(2, 4), "YStar星", "tag_info_01"),

    IS_YSTAR_Life_FirstDay((long) Math.pow(2 , 0) , "命中注定,Ystar股东" , "tag_info_02"),
    IS_OLD_USER_1((long) Math.pow(2, 1), "真诚的陪伴,Ystar一年之约", "tag_info_02"),
    IS_OLD_USER_3((long) Math.pow(2, 2), "不变的情谊,Ystar三年之约", "tag_info_02"),
    IS_OLD_USER_10((long) Math.pow(2, 3), "永恒的约定,Ystar十年之约", "tag_info_02"),

    IS_YSTAR_Admin((long) Math.pow(2, 0), "YStar管理员", "tag_info_03");

    long tag;
    String desc;
    String fieldName;

    UserTagsEnum(long tag, String desc, String fieldName) {
        this.tag = tag;
        this.desc = desc;
        this.fieldName = fieldName;
    }

    public long getTag() {
        return tag;
    }

    public String getDesc() {
        return desc;
    }

    public String getFieldName() {
        return fieldName;
    }
}