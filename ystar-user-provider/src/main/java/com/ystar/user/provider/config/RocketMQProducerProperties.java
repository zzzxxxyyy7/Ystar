package com.ystar.user.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ 配置类
 *
 * 生产者配置信息
 */
@Configuration
@ConfigurationProperties(prefix = "ystar.rmq.producer")
@Data
public class RocketMQProducerProperties {
    // 服务地址
    private String nameSrv;
    // 分组名称
    private String groupName;
    // 重试次数
    private Integer retryTimes;
    // 超时时间
    private Integer sendTimeout;

}
