package com.nageoffer.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

import static com.nageoffer.shortlink.admin.common.constants.UserCacheConstants.USER_LOGIN;

@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String username = httpServletRequest.getHeader("username");
        String token = httpServletRequest.getHeader("token");

        Object jsonStr = stringRedisTemplate.opsForHash().get(USER_LOGIN + username, token);
        if(jsonStr != null){
            UserInfoDTO userInfoDTO = JSONObject.parseObject(jsonStr.toString(), UserInfoDTO.class);
            UserContext.set(userInfoDTO);
        }
        try{
            filterChain.doFilter(servletRequest,servletResponse);
        }finally{
            UserContext.remove();
        }
    }
}
