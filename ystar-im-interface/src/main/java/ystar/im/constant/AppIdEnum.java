package ystar.im.constant;

public enum AppIdEnum {
    YStar_LIVE_BIZ(10001, "YStar直播业务");

    private int code;
    private String desc;

    AppIdEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}