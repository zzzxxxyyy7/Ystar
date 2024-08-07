package ystar.im.core.server.handler.Impl;

import com.alibaba.fastjson2.JSON;
import io.micrometer.common.util.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.AppIdEnum;
import ystar.im.constant.ImConstants;
import ystar.im.constant.ImMsgCodeEnum;
import ystar.im.constant.RabbitMqConstants;
import ystar.im.core.server.common.ChannelHandlerContextCache;
import ystar.im.core.server.common.ImContextUtils;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.constants.ImCoreServerConstants;
import ystar.im.core.server.dto.ImOnlineDto;
import ystar.im.core.server.handler.SimpleHandler;
import ystar.im.interfaces.ImTokenRpc;

import java.util.concurrent.TimeUnit;

/**
 * 登录消息处理器
 */
@Component
public class LoginMsgHandler implements SimpleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginMsgHandler.class);

    @DubboReference
    private ImTokenRpc imTokenRpc;

    @Resource
    private RedisTemplate<String , String> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 想要建立连接的话，我们需要进行一系列的参数校验，
     * 然后参数无误后，验证存储的userId和消息中的userId是否相同，相同才允许建立连接
     */
    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        // 防止重复请求：若已经允许连接就不再接收login请求包
        if (ImContextUtils.getUserId(ctx) != null) {
            return;
        }

        byte[] body = imMsg.getBody();
        if (body == null || body.length == 0) {
            ctx.close();
            LOGGER.error("body error, imMsg is {}", imMsg);
            throw new IllegalArgumentException("body error");
        }

        ImMsgBody imMsgBody = JSON.parseObject(new String(body), ImMsgBody.class);
        String token = imMsgBody.getToken();
        Long userIdFromMsg = imMsgBody.getUserId();
        int appId = imMsgBody.getAppId();

        if (StringUtils.isEmpty(token) || appId < 10000) {
            ctx.close();
            LOGGER.error("param error, imMsg is {}", imMsg);
            throw new IllegalArgumentException("param error");
        }

        Long userId = imTokenRpc.getUserIdByToken(token);
        // 从RPC获取的 userId 和传递过来的 userId 相等，则没出现差错，允许建立连接
        if (userId != null && userId.equals(userIdFromMsg)) {
            loginSuccessHandler(ctx, userId, appId , null);
            return;
        }

        // 登录信息不正确 ， 不允许建立连接 ， 关闭 Channel
        ctx.close();
        LOGGER.error("token error, imMsg is {}", imMsg);
        throw new IllegalArgumentException("token error");
    }

    /**
     * 供给下游服务获取用户登录信息
     * @param userId
     * @param appId
     */
    private void sendLoginMQ(Long userId , Integer appId , Integer roomId) {
        ImOnlineDto imOnlineDto = new ImOnlineDto();
        imOnlineDto.setUserId(userId);
        imOnlineDto.setAppId(appId);
        imOnlineDto.setRoomId(roomId);
        imOnlineDto.setLoginTime(System.currentTimeMillis());
        /**
         * MQ 投递消息到下游微服务
         */
        Message message = new Message(JSON.toJSONString(imOnlineDto).getBytes());

        try {
            rabbitTemplate.convertAndSend(RabbitMqConstants.Login_EXCHANGE, RabbitMqConstants.Login_ROUTINGKEY, message ,(msg -> {
                //发送消息 并设置delayedTime
                msg.getMessageProperties().setDelay(0);
                return msg;
            }));
        } catch (Exception e) {
            LOGGER.info("[登录消息传递下游服务 MQ 发送失败]:{}" , e.getMessage());
        }
    }

    /**
     * 如果用户成功登录，就处理相关记录
     */
    public void loginSuccessHandler(ChannelHandlerContext ctx, Long userId, Integer appId , Integer roomId) {
        // 按照userId保存好相关的channel信息
        ChannelHandlerContextCache.put(userId, ctx);
        // 将userId保存到netty域信息中，用于正常/非正常logout的处理
        ImContextUtils.setUserId(ctx, userId);
        ImContextUtils.setAppId(ctx, appId);
        // 将im消息回写给客户端
        ImMsgBody respBody = new ImMsgBody();
        respBody.setAppId(AppIdEnum.YStar_LIVE_BIZ.getCode());
        respBody.setUserId(userId);
        respBody.setData("true");
        ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), JSON.toJSONString(respBody));
        // 将im服务器的ip+端口地址保存到Redis，以供Router服务取出进行转发
        redisTemplate.opsForValue().set(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId,
                ChannelHandlerContextCache.getServerIpAddress() + "%" + userId,
                2 * ImConstants.DEFAULT_HEART_BEAT_GAP, TimeUnit.SECONDS);
        LOGGER.info("[LoginMsgHandler] login success, userId is {}, appId is {}", userId, appId);
        ctx.writeAndFlush(respMsg);
        sendLoginMQ(userId , appId , roomId);
    }
}