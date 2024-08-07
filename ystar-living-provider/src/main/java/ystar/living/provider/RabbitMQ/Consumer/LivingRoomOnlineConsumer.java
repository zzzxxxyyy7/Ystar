package ystar.living.provider.RabbitMQ.Consumer;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ystar.im.constant.RabbitMqConstants;
import ystar.im.core.server.dto.ImOnlineDto;
import ystar.living.provider.service.TLivingRoomService;

@Component
public class LivingRoomOnlineConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LivingRoomOnlineConsumer.class);

    @Resource
    private TLivingRoomService tLivingRoomService;

    @RabbitListener(queues = RabbitMqConstants.Login_QUEUE)
    public void receiveMessage(Message message, Channel channel){
        /**
         * 取出消息
         */
        ImOnlineDto imOnlineDto = JSON.parseObject(new String(message.getBody()), ImOnlineDto.class);

        /**
         * 记录到 Redis
         */
        tLivingRoomService.userOnlineHandler(imOnlineDto);
    }
}
