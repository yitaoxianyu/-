package com.nageoffer.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSONObject;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static com.nageoffer.shortlink.admin.common.constants.UserConstants.USER_FLOW_CONTROL_KEY;
import static com.nageoffer.shortlink.admin.common.convention.errorcode.BaseErrorCode.FLOW_LIMIT_ERROR;

@Slf4j
public class UserFlowControlFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    private final UserFlowControlProperties userFlowControlProperties;

    public UserFlowControlFilter(UserFlowControlProperties userFlowControlProperties,StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userFlowControlProperties = userFlowControlProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setLocation(new ClassPathResource("user_flow_control.lua"));
        defaultRedisScript.setResultType(Long.class);

        //如果没有解析到用户信息username 内容是 null
        String username = String.valueOf(UserContext.getUsername());
        Long execute;
        try{
            execute = stringRedisTemplate.execute(
                    defaultRedisScript, List.of(USER_FLOW_CONTROL_KEY + username),
                    userFlowControlProperties.getTimeout()
            );
        }catch (Throwable t){
            log.error("执行 lua 脚本失败",t);
            returnJson((HttpServletRequest) request,(HttpServletResponse) response, JSONObject.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            return ;
        }
        if(execute > userFlowControlProperties.getMaxCount()){
            returnJson((HttpServletRequest) request,(HttpServletResponse) response, JSONObject.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            return ;
        }
        filterChain.doFilter(request,response);

    }

    private void returnJson(HttpServletRequest request, HttpServletResponse response,String json) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
        }

    }
}
