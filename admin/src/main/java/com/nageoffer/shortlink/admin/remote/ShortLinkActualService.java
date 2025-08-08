package com.nageoffer.shortlink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dto.req.RecoverShortLinkReqDTO;
import com.nageoffer.shortlink.admin.remote.req.*;
import com.nageoffer.shortlink.admin.remote.resp.*;
import com.nageoffer.shortlink.admin.remote.resp.stats.ShortLinkStatsRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "short-link",path = "/api/short-link/v1")
public interface ShortLinkActualService{

    @GetMapping("/page")
    Result<Page<ShortLinkPageRespDTO>> pageQueryShortLink(@SpringQueryMap ShortLinkPageReqDTO requestParams);

    @PostMapping("/create")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParams);

    @PostMapping("/create/batch")
    Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParams);

    @GetMapping("/count")
    Result<List<ShortLinkQueryCountRespDTO>> queryShortLinkCount(@RequestParam(value = "requestParams")List<String> requestParams);

    @PostMapping("/update")
    Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParms);

    @GetMapping("/url/title")
    Result<String> getTitleByUrl(@RequestParam(value = "url")String originUrl);

    @GetMapping("/url/favicon")
    Result<String> getFaviconByUrl(@RequestParam(value = "url") String originUrl);

    @GetMapping("/recycle-bin/page")
    Result<Page<RecycleBinPageRespDTO>> pageQueryShortLink(@SpringQueryMap RecycleBinPageReqDTO requestParams);

    @PostMapping("/recycle-bin/save")
    Result<Void> saveShortLink2RecycleBin(@RequestBody SaveShortLinkReqDTO requestParams);

    @PostMapping("/recycle-bin/recover")
    Result<Void> recoverShortLink(@RequestBody RecoverShortLinkReqDTO requestParams);

    @PostMapping("/recycle-bin/remove")
    Result<Void> removeShortLink(@RequestBody RemoveShortLinkReqDTO requestParams);

    @GetMapping("/stats")
    Result<ShortLinkStatsRespDTO> showOneUrlStats(@SpringQueryMap ShortLinkStatsReqDTO requestParams);

    @GetMapping("/stats/access-record")
    Result<Page<ShortLinkAccessLogsRespDTO>> showOneUrlLogs(@SpringQueryMap ShortLinkAccessLogsReqDTO requestParams);
}
