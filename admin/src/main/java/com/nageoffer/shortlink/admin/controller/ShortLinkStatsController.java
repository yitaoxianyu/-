package com.nageoffer.shortlink.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.ShortLinkService;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkAccessLogsReqDTO;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkAccessLogsRespDTO;
import com.nageoffer.shortlink.admin.remote.resp.stats.ShortLinkStatsRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// todo调用中台接口
@RestController
@RequestMapping("/api/short-link/admin/v1/stats")
public class ShortLinkStatsController {

    private static final ShortLinkService shortLinkRemoteService = new ShortLinkService() {};

    @GetMapping
    public Result<ShortLinkStatsRespDTO> showOneUrlStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO){
        return shortLinkRemoteService.showOneUrlStats(shortLinkStatsReqDTO);
    }

    @GetMapping("/access-logs")
    public Result<IPage<ShortLinkAccessLogsRespDTO>> showQueryOneUrlLogs(ShortLinkAccessLogsReqDTO shortLinkAccessLogsReqDTO){
        return shortLinkRemoteService.showOneUrlLogs(shortLinkAccessLogsReqDTO);
    }
}


