package ystar.im.router.provider;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.router.provider.service.ImRouterService;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class ImRouterProviderApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ImRouterProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }
    
    @Resource
    private ImRouterService routerService;

    @Override
    public void run(String... args) throws Exception {
        for(int i = 0; i < 1; i++) {
            ImMsgBody imMsgBody = new ImMsgBody();
            routerService.sendMsg(1001L , JSON.toJSONString(imMsgBody));
            Thread.sleep(1000);
        }
    }
}