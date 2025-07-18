package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.RecoverShortLinkReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.RemoveShortLinkReqDTO;
import com.nageoffer.shortlink.project.dto.req.SaveShortLinkReqDTO;
import com.nageoffer.shortlink.project.dto.resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/v1/recycle-bin")
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    @PostMapping("/save")
    public Result<Void> saveShortLink2RecycleBin(@RequestBody SaveShortLinkReqDTO requestParams){
        recycleBinService.saveLink2RecycleBin(requestParams);
        return Results.success();
    }

    @GetMapping("/page")
    public Result<IPage<RecycleBinPageRespDTO>> pageQueryShortLink(RecycleBinPageReqDTO requestParams){
        return Results.success(recycleBinService.pageQueryShortLink(requestParams));
    }

    @PostMapping("/recover")
    public Result<Void> recoverShortLink(@RequestBody RecoverShortLinkReqDTO requestParams){
        recycleBinService.recoverShortLink(requestParams);
        return Results.success();
    }

    @PostMapping("/remove")
    public Result<Void> removeShortLink(@RequestBody RemoveShortLinkReqDTO requestParams){
        recycleBinService.removeShortLink(requestParams);
        return Results.success();
    }


}
