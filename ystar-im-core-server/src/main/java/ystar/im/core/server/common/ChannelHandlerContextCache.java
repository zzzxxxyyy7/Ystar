package ystar.im.core.server.common;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 封装ChannelHandlerContext的缓存，将已建立连接的ChannelHandlerContext放到这里
 * 用来存储 userId 和其对应的 ChannelContext
 */
public class ChannelHandlerContextCache {

    /**
     * 当前服务启动时缓存，对外暴露的 IP 和 端口
     */
    private static String SERVER_IP_ADDRESS = "";

    public static String getServerIpAddress() {
        return SERVER_IP_ADDRESS;
    }

    public static void setServerIpAddress(String serverIpAddress) {
        SERVER_IP_ADDRESS = serverIpAddress;
    }
    
    private static Map<Long, ChannelHandlerContext> channelHandlerContextMap = new HashMap<>();
    
    public static ChannelHandlerContext get(Long userId) {
        return channelHandlerContextMap.get(userId);
    }
    
    public static void put(Long userId, ChannelHandlerContext channelHandlerContext) {
        channelHandlerContextMap.put(userId, channelHandlerContext);
    }
    
    public static void remove(Long userId) {
        channelHandlerContextMap.remove(userId);
    }
}