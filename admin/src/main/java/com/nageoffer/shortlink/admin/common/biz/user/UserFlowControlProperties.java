package com.nageoffer.shortlink.admin.common.biz.user;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "short-link.user-flow-control")
public class UserFlowControlProperties {


    public Integer maxCount;

    public String timeout;

}
