package com.nageoffer.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class UserInfoDTO {

    @JSONField(name = "username")
    private String name;

    private String realName;

    @JSONField(name = "id")
    private String userId;


}
