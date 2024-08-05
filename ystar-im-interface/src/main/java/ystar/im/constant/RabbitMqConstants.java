package ystar.im.constant;

public class RabbitMqConstants {

    /**
     * 业务消息向下游传递
     */
    //交换机
    public static final String DELAYED_EXCHANGE ="delayed_exchange";
    //队列
    public static final String DELAYED_QUEUE ="delayed_queue";
    //routeingKey
    public static final String DELAYED_ROUTINGKEY ="delayed_routingKey";


    /**
     * ACK 消息确认机制
     */
    //交换机
    public static final String ACK_EXCHANGE ="Ack_exchange";
    //队列
    public static final String ACK_QUEUE ="Ack_queue";
    //routeingKey
    public static final String ACK_ROUTINGKEY ="Ack_routingKey";
}
