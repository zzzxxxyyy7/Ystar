package ystar.im.core.server.handler.Impl;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.RabbitMqConstants;
import ystar.im.core.server.common.ImContextUtils;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.handler.SimpleHandler;
import ystar.im.core.server.service.IMsgAckCheckService;

import java.util.UUID;

/**
 * 业务消息处理器
 */
@Component
public class BizImMsgHandler implements SimpleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BizImMsgHandler.class);

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private IMsgAckCheckService iMsgAckCheckService;

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

        ImMsgBody imMsgBody = JSON.parseObject(new String(body) , ImMsgBody.class);

        /**
         * 发送消息时候设置消息ID
         */
        imMsgBody.setMsgId(UUID.randomUUID().toString());
        body = JSON.toJSONBytes(imMsgBody);

        /**
         * MQ 投递消息到下游微服务
         */
        Message message = new Message(body);
        rabbitTemplate.convertAndSend(RabbitMqConstants.DELAYED_EXCHANGE, RabbitMqConstants.DELAYED_ROUTINGKEY, message ,(msg -> {
            //发送消息 并设置delayedTime
            msg.getMessageProperties().setDelay(1);
            return msg;
        }));

        /**
         * IM 服务器把消息推送给客户端了，记录在 Map 里
         */
        iMsgAckCheckService.recordMsgAck(imMsgBody , 1);

        /**
         * 发送延时消息
         */
        iMsgAckCheckService.sendDelayMsg(imMsgBody);
    }
}