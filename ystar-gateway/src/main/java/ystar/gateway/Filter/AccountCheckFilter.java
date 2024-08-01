package ystar.gateway.Filter;

import com.ystar.common.Enums.GatewayHeaderEnum;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ystar.auth.account.interfaces.IAccountTokenRPC;
import ystar.gateway.Properties.GatewayApplicationProperties;

import java.net.InetSocketAddress;
import java.util.List;

import static io.netty.handler.codec.http.cookie.CookieHeaderNames.MAX_AGE;
import static org.springframework.web.cors.CorsConfiguration.ALL;


@Component
public class AccountCheckFilter implements GlobalFilter, Ordered {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountCheckFilter.class);

    @DubboReference
    private IAccountTokenRPC accountTokenRPC;

    @Resource
    private GatewayApplicationProperties gatewayApplicationProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求url，判断是否为空，如果为空则返回请求不通过
        ServerHttpRequest request = exchange.getRequest();
        String reqUrl = request.getURI().getPath();

        // 动态设置 Access-Control-Allow-Origin
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, request.getHeaders().getOrigin());
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");

        if (StringUtils.isEmpty(reqUrl)) return Mono.empty();

        // 根据url，判断是否存在于url白名单中，如果存在，则不对token进行校验
        List<String> notCheckUrlList = gatewayApplicationProperties.getNotCheckUrlList();
        for (String notCheckUrl : notCheckUrlList) {
            if (reqUrl.startsWith(notCheckUrl)) {
                LOGGER.info("请求资源不需要 Token 校验，直接传达给业务下游");
                // 直接将请求转给下游
                return chain.filter(exchange);
            }
        }

        // 如果不存在url白名单，那么就需要提取cookie，并且对cookie做基本的格式校验
        List<HttpCookie> httpCookieList = request.getCookies().get("ystar");
        if (CollectionUtils.isEmpty(httpCookieList)) {
            LOGGER.error("请求没有检索到 YStar 的 cookie，请求被拦截");
            return Mono.empty();
        }

        String YStarTokenCookieValue = httpCookieList.get(0).getValue();
        if (StringUtils.isEmpty(YStarTokenCookieValue) || StringUtils.isEmpty(YStarTokenCookieValue.trim())) {
            LOGGER.error("请求的 cookie 中的 YStar 是空，被拦截");
            return Mono.empty();
        }

        // token 获取到之后，调用 rpc 判断 token 是否合法，如果合法则吧 token 换取到的 userId 传递给到下游
        Long userId = accountTokenRPC.getUserIdByToken(YStarTokenCookieValue);
        // 如果 token 不合法，则拦截请求，日志记录token失效
        if (userId == null) {
            LOGGER.error("请求的 Token 不合法，请求被拦截");
            return Mono.empty();
        }

        LOGGER.info("Token 校验成功！");

        ServerWebExchange ex = exchange.mutate()
                .request(b -> b.header("userId" , userId.toString()))
                .build();

        return chain.filter(ex);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}