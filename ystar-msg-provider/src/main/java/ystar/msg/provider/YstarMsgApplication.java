package ystar.msg.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import ystar.msg.provider.Service.Impl.ISmsServiceImpl;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class YstarMsgApplication {


    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(YstarMsgApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }
}
