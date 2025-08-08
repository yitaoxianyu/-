package com.nageoffer.shortlink.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nageoffer.shortlink.gateway.Config;
import com.nageoffer.shortlink.gateway.GatewayErrorResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final StringRedisTemplate stringRedisTemplate;

    public TokenValidateGatewayFilterFactory(StringRedisTemplate stringRedisTemplate) {
        super(Config.class);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (ServerWebExchange exchange, GatewayFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();
            String requestMethod = request.getMethod().toString();
            List<String> whiteList = config.getWhitePathList();
            //在白名单内
            if(isInWhiteList(whiteList,requestMethod,path)){
                return chain.filter(exchange);
            }
            String username = request.getHeaders().getFirst("username");
            String token = request.getHeaders().getFirst("token");
            Object jsonStr;
            if(StringUtils.hasText(username) && StringUtils.hasText(token) && (jsonStr = stringRedisTemplate.opsForHash().get("short-link:user-login:" + username, token)) != null){
                    JSONObject jsonObject = JSONObject.parseObject(jsonStr.toString());
                    ServerHttpRequest build = request.mutate().headers(headers -> {
                        headers.set("userId", jsonObject.getString("id"));
                        headers.set("realName", jsonObject.getString("realName"));
                    }).build();
                    //这里每次请求过来刷新一下
                stringRedisTemplate.expire("short-link:user-login:" + username, 30,TimeUnit.MINUTES);
                return chain.filter(exchange.mutate().request(build).build());
            }
            //验证失败
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.writeWith(Mono.fromSupplier(() -> {
                DataBufferFactory bufferFactory = response.bufferFactory();
                GatewayErrorResult resultMessage = GatewayErrorResult.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message("Token validation error")
                        .build();
                return bufferFactory.wrap(JSON.toJSONString(resultMessage).getBytes());
            }));
        };
    }

    private boolean isInWhiteList(List<String> whiteList,String requestMethod,String path){
        return !CollectionUtils.isEmpty(whiteList) && (whiteList.stream().anyMatch(path::equals) || (path.equals("/api/short-link/admin/v1/user") && requestMethod.equals("POST")));
    }
}
