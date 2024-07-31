package ystar.msg.provider;

import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ystar.msg.provider.Service.TSmsService;

@SpringBootApplication
public class YstarMsgApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(YstarMsgApplication.class);

        springApplication.run(args);
    }
}
