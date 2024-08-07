package ystar.im.core.server.handler.Impl;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.ImMsgCodeEnum;
import ystar.im.constant.RabbitMqConstants;
import ystar.im.core.server.common.ChannelHandlerContextCache;
import ystar.im.core.server.common.ImContextUtils;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.constants.ImCoreServerConstants;
import ystar.im.core.server.dto.ImOfflineDto;
import ystar.im.core.server.handler.SimpleHandler;
import ystart.framework.redis.starter.key.ImCoreServerProviderCacheKeyBuilder;


/**
 * 登出消息处理器
 */
@Component
public class LogoutMsgHandler implements SimpleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutMsgHandler.class);

    @Resource
    private RedisTemplate<String , String> redisTemplate;

    @Resource
    private ImCoreServerProviderCacheKeyBuilder cacheKeyBuilder;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId == null || appId == null) {
            LOGGER.error("attr error, imMsgBody is {}", new String(imMsg.getBody()));
            // 有可能是错误的消息包导致，直接放弃连接
            ctx.close();
            throw new IllegalArgumentException("attr error");
        }
        // 将IM消息回写给客户端
        logoutHandler(ctx, userId, appId);
    }

    public void logoutHandler(ChannelHandlerContext ctx, Long userId, Integer appId) {
        ImMsgBody respBody = new ImMsgBody();
        respBody.setUserId(userId);
        respBody.setAppId(appId);
        respBody.setData("true");
        ctx.writeAndFlush(ImMsg.build(ImMsgCodeEnum.IM_LOGOUT_MSG.getCode(), JSON.toJSONString(respBody)));
        LOGGER.info("[LogoutMsgHandler] logout success, userId is {}, appId is {}", userId, appId);

        handlerLogout(userId, appId);
        sendLogoutMQ(ctx , userId , appId);

        ImContextUtils.removeUserId(ctx);
        ImContextUtils.removeAppId(ctx);
        ImContextUtils.removeRoomId(ctx);
        ctx.close();
    }

    /**
     * 供给下游服务获取用户登录信息
     * @param userId
     * @param appId
     */
    private void sendLogoutMQ(ChannelHandlerContext ctx , Long userId , Integer appId) {
        ImOfflineDto imOfflineDTO = new ImOfflineDto();
        imOfflineDTO.setUserId(userId);
        imOfflineDTO.setAppId(appId);
        imOfflineDTO.setRoomId(ImContextUtils.getRoomId(ctx));
        imOfflineDTO.setLogoutTime(System.currentTimeMillis());

        /**
         * MQ 投递消息到下游微服务
         */
        Message message = new Message(JSON.toJSONString(imOfflineDTO).getBytes());

        try {
            rabbitTemplate.convertAndSend(RabbitMqConstants.Logout_QUEUE, RabbitMqConstants.Logout_ROUTINGKEY, message ,(msg -> {
                //发送消息 并设置delayedTime
                msg.getMessageProperties().setDelay(0);
                return msg;
            }));
            LOGGER.info("[退出消息传递下游服务 MQ 发送成功]");
        } catch (Exception e) {
            LOGGER.info("[退出消息传递下游服务 MQ 发送失败]: {}" , e.getMessage());
        }
    }

    public void handlerLogout(Long userId, Integer appId) {
        // 理想情况下：客户端短线的时候发送短线消息包
        ChannelHandlerContextCache.remove(userId);
        // 删除供Router取出的存在Redis的IM服务器的ip+端口地址
        redisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId);
        // 删除心跳包存活缓存
        redisTemplate.delete(cacheKeyBuilder.buildImLoginTokenKey(userId, appId));
    }
}