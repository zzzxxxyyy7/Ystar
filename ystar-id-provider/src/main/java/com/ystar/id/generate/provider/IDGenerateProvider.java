package com.ystar.id.generate.provider;

import com.ystar.id.generate.provider.Service.IdGeneratePoService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class IDGenerateProvider {

    @Resource
    private IdGeneratePoService idGeneratePoService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(IDGenerateProvider.class);
        // Dubbo 不需要启动 Tomcat
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }
}
