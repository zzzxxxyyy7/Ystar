package ystar.msg.provider.rabbitmq;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ystar.im.constant.RabbitMqConstants;

@Component
public class ImBizMsgRabbitMqConsumer {

    /**
     * 用户 A 发送消息给 用户 B ， 消息由 im core server 投递给 msg provider
     * msg provider 进行业务处理后投递会 im core server 再由其投递到用户 B
     * 如何确认投递到哪一个 im core server 记录 IP
     * @param message
     * @param channel
     */
    @RabbitListener(queues = RabbitMqConstants.DELAYED_QUEUE)
    public void receiveMessage(Message message, Channel channel){
        String msg = new String(message.getBody());
        System.out.println(msg);
    }

}