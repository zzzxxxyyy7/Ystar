package ystar.im.core.server.common;

import io.netty.channel.ChannelHandlerContext;

/**
 * 封装 获取/存入 netty域信息的工具类
 * 记录 通道上下文 和 用户Id 的关联
 * 这个域用来记录已经登录过的 userId 方便进行正常/非正常的 logout 退出
 */
public class ImContextUtils {
    
    public static Long getUserId(ChannelHandlerContext ctx) {
        return ctx.attr(ImContextAttr.USER_ID).get();
    }
    
    public static void setUserId(ChannelHandlerContext ctx, Long userId) {
        ctx.attr(ImContextAttr.USER_ID).set(userId);
    }
    
    public static void removeUserId(ChannelHandlerContext ctx) {
        ctx.attr(ImContextAttr.USER_ID).remove();
    }
    
    public static Integer getAppId(ChannelHandlerContext ctx) {
        return ctx.attr(ImContextAttr.APP_ID).get();
    }
    
    public static void setAppId(ChannelHandlerContext ctx, Integer appId) {
        ctx.attr(ImContextAttr.APP_ID).set(appId);
    }

    public static void removeAppId(ChannelHandlerContext ctx) {
        ctx.attr(ImContextAttr.APP_ID).remove();
    }
}