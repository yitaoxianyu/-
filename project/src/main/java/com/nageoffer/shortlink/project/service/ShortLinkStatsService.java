package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dto.req.ShortLinkAccessLogsReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.project.dto.resp.logs.ShortLinkAccessLogsRespDTO;
import com.nageoffer.shortlink.project.dto.resp.stats.ShortLinkStatsRespDTO;

public interface ShortLinkStatsService {
    ShortLinkStatsRespDTO showOneUrlStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    IPage<ShortLinkAccessLogsRespDTO> showOneUrlLogs(ShortLinkAccessLogsReqDTO shortLinkAccessLogsReqDTO);
}
