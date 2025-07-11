package com.nageoffer.shortlink.admin.controller;


import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ActualUserRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Stack;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/user")
public class UserController {

    private final UserService userService;


    @GetMapping("/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username){
        return Results.success(userService.getUserByUsername(username));
    }

    @GetMapping("/actual/{username}")
    public Result<ActualUserRespDTO> getActualUserByUsername(@PathVariable String username){
        return Results.success(userService.getActualUserByUsername(username));
    }

    @GetMapping("/has-username")
    public Result<Boolean> hasUser(@RequestParam String username){
        return Results.success(userService.hasUser(username));
    }

    @PostMapping
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParams){
        userService.register(requestParams);
        return Results.success();
    }

    @PutMapping
    public Result<Void> updateUser(@RequestBody UserUpdateReqDTO requestParams){
        userService.updateUser(requestParams);
        return Results.success();
    }

    @PostMapping("/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParams){
        return Results.success(userService.login(requestParams));
    }

    @GetMapping("/check-login")
    public Result<Boolean> checkLogin(@RequestParam String username,@RequestParam String token){
        return Results.success(userService.checkLogin(username,token));
    }

    @DeleteMapping("/logout")
    public Result<Void> logout(@RequestParam String username,@RequestParam String token){
        Stack stk = new Stack();
        userService.logout(username,token);
        return Results.success();
    }
}
