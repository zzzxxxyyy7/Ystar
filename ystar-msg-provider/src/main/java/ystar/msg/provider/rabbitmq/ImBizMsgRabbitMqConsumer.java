package ystar.msg.provider.rabbitmq;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ystar.im.constant.RabbitMqConstants;

@Component
public class ImBizMsgRabbitMqConsumer {

    @RabbitListener(queues = RabbitMqConstants.DELAYED_QUEUE)
    public void receiveMessage(Message message, Channel channel){
        String msg = new String(message.getBody());
        System.out.println(msg);
    }

}