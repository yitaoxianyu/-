package com.nageoffer.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.nageoffer.shortlink.admin.common.constants.UserCacheConstants.USER_LOGIN;

@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final ArrayList<String> uris = new ArrayList<>(List.of(
            "/api/short-link/admin/v1/user",
            "/api/short-link/admin/v1/user/check-login",
            "/api/short-link/admin/v1/user/login"
    ));

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String method = ((HttpServletRequest) servletRequest).getMethod();
        String uri = httpServletRequest.getRequestURI();
        if(
                !uris.contains(uri) || (uris.get(0).equals(uri) && !(method.equals("POST")))
        ) {
            String username = httpServletRequest.getHeader("username");
            String token = httpServletRequest.getHeader("token");
            if(!StrUtil.isAllNotBlank(username,token)){
                HttpServletResponse HttpServletResponse = (HttpServletResponse) servletResponse;
                resolveException(HttpServletResponse);
                return ;
            }
            Object jsonStr = stringRedisTemplate.opsForHash().get(USER_LOGIN + username, token);
            if(jsonStr != null){
                UserInfoDTO userInfoDTO = JSONObject.parseObject(jsonStr.toString(), UserInfoDTO.class);
                UserContext.set(userInfoDTO);
            }
        }
        try{
            filterChain.doFilter(servletRequest,servletResponse);
        }finally{
            UserContext.remove();
        }
    }

    private void resolveException(HttpServletResponse httpServletResponse) {
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setStatus(401);
        try(PrintWriter out = httpServletResponse.getWriter()){
            String jsonStr = JSONObject.toJSONString(
                    Results.failure(new ClientException(UserErrorCodeEnum.REQUIRE_AUTH))
            );
            out.print(jsonStr);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
