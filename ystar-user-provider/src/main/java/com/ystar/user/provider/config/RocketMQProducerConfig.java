package com.ystar.user.provider.config;

import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class RocketMQProducerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerConfig.class);

    @Resource
    private RocketMQProducerProperties rocketMQProducerProperties;
    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public MQProducer myProducer() {

        ThreadPoolExecutor asyncThreadPoolExecutor = new ThreadPoolExecutor(100, 150, 3, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000), r -> {
                    Thread thread = new Thread(r);
                    thread.setName(applicationName + ":rmq-producer" + ThreadLocalRandom.current().nextInt(1000));
                    return null;
                }, new ThreadPoolExecutor.AbortPolicy());

        DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
        try {
            // 设置服务地址
            defaultMQProducer.setNamesrvAddr(rocketMQProducerProperties.getNameSrv());
            // 设置分组
            defaultMQProducer.setProducerGroup(rocketMQProducerProperties.getGroupName());
            // 设置重试次数
            defaultMQProducer.setRetryTimesWhenSendFailed(rocketMQProducerProperties.getRetryTimes());
            defaultMQProducer.setRetryTimesWhenSendAsyncFailed(rocketMQProducerProperties.getRetryTimes());
            // 未被接收投递到下一个Broker
            defaultMQProducer.setRetryAnotherBrokerWhenNotStoreOK(true);
            // 绑定异步线程池
            defaultMQProducer.setAsyncSenderExecutor(asyncThreadPoolExecutor);
            defaultMQProducer.start();
            LOGGER.info("用户中台服务 MQ 生产者启动成功 , nameSrv is {}" , rocketMQProducerProperties.getNameSrv());
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
        return defaultMQProducer;
    }
}
