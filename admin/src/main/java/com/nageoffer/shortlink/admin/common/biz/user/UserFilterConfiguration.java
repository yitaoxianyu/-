package com.nageoffer.shortlink.admin.common.biz.user;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;

@Configuration
public class UserFilterConfiguration {

    @Bean
    public FilterRegistrationBean<UserTransmitFilter> userFilter(StringRedisTemplate stringRedisTemplate){
        FilterRegistrationBean<UserTransmitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UserTransmitFilter(stringRedisTemplate));
        registrationBean.setUrlPatterns(Collections.singleton("/*"));
        return registrationBean;
    }
}
