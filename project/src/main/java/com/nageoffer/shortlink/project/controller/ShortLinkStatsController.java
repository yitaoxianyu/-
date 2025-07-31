package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.ShortLinkAccessLogsReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.project.dto.resp.logs.ShortLinkAccessLogsRespDTO;
import com.nageoffer.shortlink.project.dto.resp.stats.ShortLinkStatsRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/short-link/v1/stats")
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    //查询单个短链接统计信息
    @GetMapping
    public Result<ShortLinkStatsRespDTO> showOneUrlStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO){
        return Results.success(shortLinkStatsService.showOneUrlStats(shortLinkStatsReqDTO));
    }

    //分页查询单个短链接的日志信息
    @GetMapping("/access-logs")
    public Result<IPage<ShortLinkAccessLogsRespDTO>> showQueryOneUrlLogs(ShortLinkAccessLogsReqDTO requestParams){
        return Results.success(shortLinkStatsService.showOneUrlLogs(requestParams));
    }

}
