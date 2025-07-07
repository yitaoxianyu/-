package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ActualUserRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<UserDO> {
    UserRespDTO getUserByUsername(String username);

    ActualUserRespDTO getActualUserByUsername(String username);

    Boolean hasUser(String username);

    void register(UserRegisterReqDTO requestParams);

    void updateUser(UserUpdateReqDTO requestParams);

    UserLoginRespDTO login(UserLoginReqDTO requestParams);

    Boolean checkLogin(String username, String token);

    void logout(String username, String token);
}
