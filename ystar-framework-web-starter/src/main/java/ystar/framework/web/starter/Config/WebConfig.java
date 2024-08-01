package ystar.framework.web.starter.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ystar.framework.web.starter.Interceptor.YStarUserInfoInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public YStarUserInfoInterceptor YStarUserInfoInterceptor() {
        return new YStarUserInfoInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(YStarUserInfoInterceptor()).addPathPatterns("/**").excludePathPatterns("/error");
    }
}