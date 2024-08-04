package ystar.msg.provider.rabbitmq;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.RabbitMqConstants;
import ystar.msg.provider.Service.Impl.ISmsServiceImpl;
import ystar.msg.provider.rabbitmq.handler.MessageHandler;

@Component
public class ImBizMsgRabbitMqConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISmsServiceImpl.class);

    @Resource
    private MessageHandler singleMessageHandler;

    /**
     * 用户 A 发送消息给 用户 B ， 消息由 im core server 投递给 msg provider
     * msg provider 进行业务处理后投递会 im core server 再由其投递到用户 B
     * 如何确认投递到哪一个 im core server 记录 IP
     * @param message
     * @param channel
     */
    @RabbitListener(queues = RabbitMqConstants.DELAYED_QUEUE)
    public void receiveMessage(Message message, Channel channel){
        LOGGER.info("Msg Provider 成功收到消息，交给业务层处理");
        /**
         * 取出消息
         */
        ImMsgBody imMsgBody = JSON.parseObject(new String(message.getBody()), ImMsgBody.class);
        singleMessageHandler.onMsgReceive(imMsgBody);
    }

}