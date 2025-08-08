package com.nageoffer.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try{
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            String userId = httpServletRequest.getHeader("userId");
            String realName = httpServletRequest.getHeader("realName");
            String username = httpServletRequest.getHeader("username");

            if(StrUtil.isNotBlank(userId)){
                UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                        .realName(realName)
                        .name(username)
                        .userId(userId)
                        .build();
                UserContext.set(userInfoDTO);
            }
            filterChain.doFilter(servletRequest,servletResponse);
        }finally{
            UserContext.remove();
        }
    }

}
