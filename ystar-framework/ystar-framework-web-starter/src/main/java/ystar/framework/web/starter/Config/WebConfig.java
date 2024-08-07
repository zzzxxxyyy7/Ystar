package ystar.framework.web.starter.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ystar.framework.web.starter.Interceptor.RequestLimitInterceptor;
import ystar.framework.web.starter.Interceptor.YStarUserInfoInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public YStarUserInfoInterceptor qiyuUserInfoInterceptor() {
        return new YStarUserInfoInterceptor();
    }

    @Bean
    public RequestLimitInterceptor requestLimitInterceptor(){
        return new RequestLimitInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(qiyuUserInfoInterceptor()).addPathPatterns("/**").excludePathPatterns("/error");
        registry.addInterceptor(requestLimitInterceptor()).addPathPatterns("/**").excludePathPatterns("/error");
    }

}