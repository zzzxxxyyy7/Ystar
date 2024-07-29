package com.ystar.user.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ystar.rmq.consumer")
@Data
public class RocketMQConsumerProperties {
    // 服务地址
    private String nameSrv;
    // 分组名称
    private String groupName;
}
