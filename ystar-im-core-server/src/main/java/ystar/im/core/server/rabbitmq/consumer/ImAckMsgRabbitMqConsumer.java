package ystar.im.core.server.rabbitmq.consumer;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.RabbitMqConstants;
import ystar.im.core.server.common.ChannelHandlerContextCache;
import ystar.im.core.server.service.IMsgAckCheckService;
import ystar.im.core.server.service.IRouterHandlerService;

@Component
public class ImAckMsgRabbitMqConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImAckMsgRabbitMqConsumer.class);

    @Resource
    private IMsgAckCheckService iMsgAckCheckService;

    @Resource
    private IRouterHandlerService iRouterHandlerService;

    /**
     * 用户 A 发送消息给 用户 B ， 消息由 im core server 投递给 msg provider
     * msg provider 进行业务处理后投递会 im core server 再由其投递到用户 B
     * 如何确认投递到哪一个 im core server 记录 IP
     * @param message
     * @param channel
     */
    @RabbitListener(queues = RabbitMqConstants.ACK_QUEUE)
    public void receiveMessage(Message message, Channel channel){
        /**
         * 取出消息
         */
        ImMsgBody imMsgBody = JSON.parseObject(new String(message.getBody()) , ImMsgBody.class);

        /**
         * 消费的时候判断一下这个消息是否有被移除，被移除才能代表客户端成功收到消息
         */
        int retryTimes = iMsgAckCheckService.getMsgAckTimes(imMsgBody.getMsgId(), imMsgBody.getUserId(), imMsgBody.getAppId());

        /**
         * 如果延时次数是1，说明客户端还没消费消息，执行重发
         */
        if (retryTimes == 1) {

            ChannelHandlerContext ctx = ChannelHandlerContextCache.get(imMsgBody.getUserId());

            /**
             * 重发消息
             */
            ctx.writeAndFlush(imMsgBody);

            /**
             * 重发记录和延迟消息
             */
            iMsgAckCheckService.recordMsgAck(imMsgBody , retryTimes + 1);
            iMsgAckCheckService.sendDelayMsg(imMsgBody);

            LOGGER.info("触发重试，retryTime = {}" , retryTimes);

        } else {
            /**
             * 已经重新发送过了
             */
            iMsgAckCheckService.doMsgAck(imMsgBody);
        }
    }

}