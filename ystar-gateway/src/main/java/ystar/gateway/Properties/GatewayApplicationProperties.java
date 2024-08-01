package ystar.gateway.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "ystar.gateway")
@Configuration
@RefreshScope
public class GatewayApplicationProperties {

    /**
     * 白名单
     */
    private List<String> notCheckUrlList;

    /**
     * 获取白名单
     * @return
     */
    public List<String> getNotCheckUrlList() {
        return notCheckUrlList;
    }

    public void setNotCheckUrlList(List<String> notCheckUrlList) {
        this.notCheckUrlList = notCheckUrlList;
    }

    @Override
    public String toString() {
        return "GatewayApplicationProperties{" +
                "notCheckUrlList=" + notCheckUrlList +
                '}';
    }
}