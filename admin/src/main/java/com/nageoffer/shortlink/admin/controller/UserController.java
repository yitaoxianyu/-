package com.nageoffer.shortlink.admin.controller;


import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ActualUserRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
