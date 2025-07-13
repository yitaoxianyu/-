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
import com.nageoffer.shortlink.project.dto.resp.ShortLinkUpdateRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/short-link/v1")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @PostMapping("/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParams){
        return Results.success(shortLinkService.createShortLink(requestParams));
    }

    @GetMapping("/page")
    public Result<IPage<ShortLinkPageRespDTO>> PageQueryShortLink(ShortLinkPageReqDTO requestParams){
        return Results.success(shortLinkService.pageQuery(requestParams));
    }

    @PostMapping("/update")
    public Result<ShortLinkUpdateRespDTO> updateShortLink(@RequestBody ShortLinkUpdateReqDTO shortLinkUpdateReqDTO){
        return null;
    }

    @GetMapping("/count")
    public Result<List<ShortLinkQueryCountDTO>> queryShortLinkCount(@RequestParam(value = "requestParams") List<String> requestParams){
        return Results.success(shortLinkService.queryShortLinkCount(requestParams));
    }


}
