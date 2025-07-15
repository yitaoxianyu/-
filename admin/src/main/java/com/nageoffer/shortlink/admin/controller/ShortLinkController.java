package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.ShortLinkService;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/short-link/admin/v1")
public class ShortLinkController {

    private static final ShortLinkService shortLinkRemoteService = new ShortLinkService() {};

    @GetMapping("/page")
    public Result<IPage<ShortLinkPageRespDTO>> PageQueryShortLink(ShortLinkPageReqDTO requestParams){
        return shortLinkRemoteService.PageQueryShortLink(requestParams);
    }

    @PostMapping("/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParams){
        return shortLinkRemoteService.createShortLink(requestParams);
    }

    @PostMapping("/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParms){
        return shortLinkRemoteService.updateShortLink(requestParms);
    }
}
