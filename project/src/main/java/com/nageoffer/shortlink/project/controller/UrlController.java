package com.nageoffer.shortlink.project.controller;

import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 原始链接控制层
 */
@RestController
@RequestMapping("/api/short-link/v1/url")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @GetMapping("/title")
    public Result<String> getTitleByUrl(@RequestParam(value = "url") String originUrl) throws IOException {
        return Results.success(urlService.getTitleByUrl(originUrl));
    }

    @GetMapping("/favicon")
    public Result<String> getFaviconByUrl(@RequestParam("url") String originUrl){
        return Results.success(urlService.getFaviconByUrl(originUrl));
    }

}
