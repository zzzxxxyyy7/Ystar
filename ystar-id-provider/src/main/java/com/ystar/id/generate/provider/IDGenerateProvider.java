package com.ystar.id.generate.provider;

import com.ystar.id.generate.provider.Service.IdGeneratePoService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IDGenerateProvider implements CommandLineRunner {

    @Resource
    private IdGeneratePoService idGeneratePoService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(IDGenerateProvider.class);
        // Dubbo 不需要启动 Tomcat
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

    @Override
    public void run(String... args) {
        for (int i = 1 ; i <= 3000 ; ++i) {
            Long id = idGeneratePoService.getUnSeqId(1);
            System.out.println(id);
        }
    }
}
