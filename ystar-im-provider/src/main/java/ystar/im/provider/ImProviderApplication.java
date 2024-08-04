package ystar.im.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import ystar.im.constant.AppIdEnum;
import ystar.im.constant.ImConstants;
import ystar.im.core.server.constants.ImCoreServerConstants;
import ystar.im.provider.Service.ImOnlineService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class ImProviderApplication {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        SpringApplication springApplication = new SpringApplication(ImProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
        countDownLatch.await();
    }

    @Resource
    private ImOnlineService imOnlineService;
}