package com.ystar.user.provider.config;

import com.alibaba.fastjson.JSON;
import com.ystar.user.dto.UserDTO;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import ystart.framework.redis.starter.key.UserProviderCacheKeyBuilder;

import java.util.List;

/**
 * 消费者配置类
 */
public class RocketMQConsumerConfig implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerConfig.class);

    @Resource
    private RocketMQConsumerProperties rocketMQConsumerProperties;

    @Resource
    private RedisTemplate<String , UserDTO> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    /**
     * 在 Spring 容器的初始化完成后回调函数完成 Consumer 初始化
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        initConsumer();
    }

    public void initConsumer() {
        try {
            // 初始化 RocketMQ 消费者
            DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
            // 设置服务地址和分组
            defaultMQPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
            defaultMQPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName());
            // 每次只消费一条
            defaultMQPushConsumer.setConsumeMessageBatchMaxSize(1);
            // 初次从消息队列头部开始消费，即历史消息(还存在broker的)，全部消费一遍，后续再启动接着上次消费的进度开始消费
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            // TODO 设置消费者监听的 Topic , 暂时写死
            defaultMQPushConsumer.subscribe("user-update-cache" , "*");
            // 注册消费者并发消费模式, 消费速度要比有序消费更快，并发消费消费失败后消息返回的次数要慢很多
            defaultMQPushConsumer.setMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    // 打印消息测试
                    UserDTO userDTO = JSON.parseObject(new String(msgs.get(0).getBody()) , UserDTO.class);
                    if (userDTO == null || userDTO.getUserId() == null) {
                        LOGGER.info("用户数据异常，延迟双删失败");
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    // TODO 触发延迟双删除
                    redisTemplate.delete(userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId()));
                    LOGGER.info("触发延迟双删 , userDto is {}" , userDTO);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            defaultMQPushConsumer.start();
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("用户中台服务 MQ 消费者启动成功 , nameSrv is {}" , rocketMQConsumerProperties.getNameSrv());
    }
}
