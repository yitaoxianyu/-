package com.nageoffer.shortlink.admin.common.biz.user;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Optional;

public class UserContext {

    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static void set(UserInfoDTO userInfoDTO){
        USER_THREAD_LOCAL.set(userInfoDTO);
    }

    public static String getUserId(){
        return Optional.ofNullable(USER_THREAD_LOCAL.get())
                .map(UserInfoDTO::getUserId).orElse(null);
    }

    public static String getUsername(){
        return Optional.ofNullable(USER_THREAD_LOCAL.get())
                .map(UserInfoDTO::getName).orElse(null);
    }

    public static String getRealName(){
        return Optional.ofNullable(USER_THREAD_LOCAL.get())
                .map(UserInfoDTO::getRealName).orElse(null);
    }

    public static void remove(){
        USER_THREAD_LOCAL.remove();
    }

}
