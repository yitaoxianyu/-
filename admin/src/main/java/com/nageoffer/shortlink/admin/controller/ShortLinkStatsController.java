package com.nageoffer.shortlink.admin.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.ShortLinkActualService;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkAccessLogsReqDTO;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkAccessLogsRespDTO;
import com.nageoffer.shortlink.admin.remote.resp.stats.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/short-link/admin/v1/stats")
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkActualService shortLinkActualService;

    @GetMapping
    public Result<ShortLinkStatsRespDTO> showOneUrlStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO){
        return shortLinkActualService.showOneUrlStats(shortLinkStatsReqDTO);
    }

    @GetMapping("/access-record")
    public Result<Page<ShortLinkAccessLogsRespDTO>> showQueryOneUrlLogs(ShortLinkAccessLogsReqDTO shortLinkAccessLogsReqDTO){
        return shortLinkActualService.showOneUrlLogs(shortLinkAccessLogsReqDTO);
    }
}


