package com.nageoffer.shortlink.admin.configuration;

import com.nageoffer.shortlink.admin.common.biz.user.UserFlowControlFilter;
import com.nageoffer.shortlink.admin.common.biz.user.UserFlowControlProperties;
import com.nageoffer.shortlink.admin.common.biz.user.UserTransmitFilter;
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
        registrationBean.setOrder(0);
        return registrationBean;
    }

    @Bean
//    @ConditionalOnProperty(name = "short-link.flow-limit.enable", havingValue = "true")
    public FilterRegistrationBean<UserFlowControlFilter> globalUserFlowRiskControlFilter(
            StringRedisTemplate stringRedisTemplate,
            UserFlowControlProperties userFlowRiskControlProperties) {
        FilterRegistrationBean<UserFlowControlFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserFlowControlFilter(userFlowRiskControlProperties,stringRedisTemplate));
        registration.addUrlPatterns("/*");
        registration.setOrder(10);
        return registration;
    }

}
