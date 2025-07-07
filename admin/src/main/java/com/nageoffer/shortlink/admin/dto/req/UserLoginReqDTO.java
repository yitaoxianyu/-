package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/*
username用户名
password 密码
 */
@Data
public class UserLoginReqDTO {
    private String username;
    private String password;

}
