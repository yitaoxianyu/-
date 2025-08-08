package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.ShortLinkActualService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private ShortLinkActualService shortLinkActualService;



    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String originUrl){
        return shortLinkActualService.getTitleByUrl(originUrl);
    }

    @GetMapping("/api/short-link/admin/v1/favicon")
    public Result<String> getFaviconByUrl(@RequestParam("url") String originUrl){
        return shortLinkActualService.getFaviconByUrl(originUrl);
    }

}
