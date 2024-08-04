package ystar.im.core.server.handler.Impl;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.ImMsgCodeEnum;
import ystar.im.constant.RabbitMqConstants;
import ystar.im.core.server.common.ImContextUtils;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.handler.SimpleHandler;

/**
 * 业务消息处理器
 */
@Component
public class BizImMsgHandler implements SimpleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BizImMsgHandler.class);

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        // 前期的参数校验
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId == null || appId == null) {
            LOGGER.error("attr error, imMsg is {}", imMsg);
            // 有可能是错误的消息包导致，直接放弃连接
            ctx.close();
            throw new IllegalArgumentException("attr error");
        }

        byte[] body = imMsg.getBody();
        if (body == null || body.length == 0) {
            LOGGER.error("body error ,imMsg is {}", imMsg);
            return;
        }

        LOGGER.info("成功收到来自 userId：{} 的消息 ， 并且投递到业务下游" , userId);

        /**
         * MQ 投递消息到下游微服务
         */
        Message message = new Message(body);
        rabbitTemplate.convertAndSend(RabbitMqConstants.DELAYED_EXCHANGE, RabbitMqConstants.DELAYED_ROUTINGKEY, message ,(msg -> {
            //发送消息 并设置delayedTime
            msg.getMessageProperties().setDelay(1);
            return msg;
        }));

        ImMsgBody respBody = new ImMsgBody();
        respBody.setUserId(userId);
        respBody.setAppId(appId);
        respBody.setData("成功发送后，写回客户端业务信息");

        System.out.println("[业务信息]:" + imMsg);
        ctx.writeAndFlush(ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(respBody)));
    }
}