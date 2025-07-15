package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkQueryCountDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParams){
        return Results.success(shortLinkService.createShortLink(requestParams));
    }

    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> PageQueryShortLink(ShortLinkPageReqDTO requestParams){
        return Results.success(shortLinkService.pageQuery(requestParams));
    }

    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParams){
        shortLinkService.updateShortLink(requestParams);
        return Results.success();
    }

    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkQueryCountDTO>> queryShortLinkCount(@RequestParam(value = "requestParams") List<String> requestParams){
        return Results.success(shortLinkService.queryShortLinkCount(requestParams));
    }

    @GetMapping("/{shortUri}")
    public Result<Void> restoreUrl(@PathVariable(name = "shortUri") String shortUri, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        shortLinkService.restoreUrl(shortUri,httpServletRequest,httpServletResponse);
        return Results.success();
    }

}
