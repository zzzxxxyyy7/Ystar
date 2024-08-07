package ystar.im.core.server.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ystar.im.constant.RabbitMqConstants;

import java.util.HashMap;

@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter messageConverter(){
        // 1.定义消息转换器
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        // 2.配置自动创建消息id，用于识别不同消息，也可以在业务中基于ID判断是否是重复消息
        jackson2JsonMessageConverter.setCreateMessageIds(true);
        return jackson2JsonMessageConverter;
    }

    /**
     * ACK消息确认机制
     * @return
     */
    //声明延迟交换机
    @Bean
    public CustomExchange AckExchange(){
        HashMap<String, Object> arguments = new HashMap<>();
        //自定义交换机的类型
        arguments.put("x-delayed-type", "direct");
        /**
         * 交换机名
         * 交换机类型
         * 持久化
         * 自动删除
         */
        return new CustomExchange(RabbitMqConstants.Logout_EXCHANGE,"x-delayed-message",true,false,arguments);
    }

    /**
     * 声明队列
     * @return
     */
    @Bean
    public Queue AckQueue(){
        return new Queue(RabbitMqConstants.Logout_QUEUE , true , false , false);
    }

    //延迟交换机和队列绑定
    @Bean
    public Binding AckQueueBindingDelayedExchange(Queue delayedQueue, CustomExchange delayedExchange){
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(RabbitMqConstants.Logout_ROUTINGKEY).noargs();
    }
}
