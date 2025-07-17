package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/url")
public class UrlController {

    private static final ShortLinkService shortLinkRemoteService = new ShortLinkService() {};

    @GetMapping("/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String originUrl){
        return shortLinkRemoteService.getTitleByUrl(originUrl);
    }

    @GetMapping("/favicon")
    public Result<String> getFaviconByUrl(@RequestParam("url") String originUrl){
        return shortLinkRemoteService.getFaviconByUrl(originUrl);
    }

}
