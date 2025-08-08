package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.ShortLinkActualService;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkBatchCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkBatchCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.admin.util.EasyExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1")
public class ShortLinkController {

    private final ShortLinkActualService shortLinkActualService;

    @GetMapping("/page")
    public Result<Page<ShortLinkPageRespDTO>> PageQueryShortLink(ShortLinkPageReqDTO requestParams){
        return shortLinkActualService.pageQueryShortLink(requestParams);
    }

    @PostMapping("/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParams) {
        return shortLinkActualService.createShortLink(requestParams);
    }

    @PostMapping("/create/batch")
    public void createBatchShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParams, HttpServletResponse response){
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkActualService.batchCreateShortLink(requestParams);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBatchCreateRespDTO.ShortLinkInfo> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getShortLinkInfoList();
            EasyExcelUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBatchCreateRespDTO.ShortLinkInfo.class, baseLinkInfos);
        }

    }

    @PostMapping("/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParms){
        return shortLinkActualService.updateShortLink(requestParms);
    }




}
