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

    /**
     * 用户登录成功消息传递
     */
    //交换机
    public static final String Login_EXCHANGE ="Login _exchange";
    //队列
    public static final String Login_QUEUE ="Login_queue";
    //routeingKey
    public static final String Login_ROUTINGKEY ="Login_routingKey";

    /**
     * 用户退出登录成功消息传递
     */
    //交换机
    public static final String Logout_EXCHANGE ="Logout_exchange";
    //队列
    public static final String Logout_QUEUE ="Logout_queue";
    //routeingKey
    public static final String Logout_ROUTINGKEY ="Logout_routingKey";

    /**
     * 赠送礼物消息下游传递
     */
    //交换机
    public static final String SendGift_EXCHANGE ="Logout_exchange";
    //队列
    public static final String SendGift_QUEUE ="Logout_queue";
    //routeingKey
    public static final String SendGift_ROUTINGKEY ="Logout_routingKey";
}
