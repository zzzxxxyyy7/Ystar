package ystar.im.core.server.service.Impl;


import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.RabbitMqConstants;
import ystar.im.core.server.service.IMsgAckCheckService;
import ystart.framework.redis.starter.key.ImCoreServerProviderCacheKeyBuilder;

@Service
public class MsgAckCheckServiceImpl implements IMsgAckCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgAckCheckServiceImpl.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ImCoreServerProviderCacheKeyBuilder cacheKeyBuilder;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 收到客户端回传的 ACK ， 移除缓存
     * @param imMsgBody
     */
    @Override
    public void doMsgAck(ImMsgBody imMsgBody) {
        // 删除唯一标识的 msgId 对应的 key-value 键值对，代表该消息已经被客户端确认
        String redisKey = cacheKeyBuilder.buildImAckMapKey(imMsgBody.getUserId(), imMsgBody.getAppId());
        redisTemplate.opsForHash().delete(redisKey, imMsgBody.getMsgId());
    }

    @Override
    public void recordMsgAck(ImMsgBody imMsgBody, int times) {
        String redisKey = cacheKeyBuilder.buildImAckMapKey(imMsgBody.getUserId(), imMsgBody.getAppId());
        System.out.println(redisKey);
        // 记录未被确认的消息id以及重试次数
        redisTemplate.opsForHash().put(redisKey, imMsgBody.getMsgId(), times);
    }

    @Override
    public void sendDelayMsg(ImMsgBody imMsgBody) {
        String jsonString = JSON.toJSONString(imMsgBody);

        /**
         * MQ 投递消息到下游服务
         */
        Message message = MessageBuilder.withBody(jsonString.getBytes()).setHeader("x-delay" , 5000).build();

        try {
            rabbitTemplate.convertAndSend(RabbitMqConstants.ACK_EXCHANGE, RabbitMqConstants.ACK_ROUTINGKEY, message ,(msg -> {
                // 消息持久化
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                // 发送消息 并设置delayedTime
                msg.getMessageProperties().setDelay(5000);
                return msg;
            }));
            LOGGER.info("[IMsgAckCheckServiceImpl] 发送成功 ， msg is {}" , jsonString);
        } catch (Exception e) {
            LOGGER.error("[IMsgAckCheckServiceImpl] ack msg send error, error is ", e);
        }

    }

    @Override
    public int getMsgAckTimes(String msgId, Long userId, int appId) {
        Object value = redisTemplate.opsForHash().get(cacheKeyBuilder.buildImAckMapKey(userId, appId), msgId);
        if (value == null) return -1;
        return (int) value;
    }
}
