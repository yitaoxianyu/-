package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dto.req.RecoverShortLinkReqDTO;
import com.nageoffer.shortlink.admin.remote.ShortLinkActualService;
import com.nageoffer.shortlink.admin.remote.req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.admin.remote.req.RemoveShortLinkReqDTO;
import com.nageoffer.shortlink.admin.remote.req.SaveShortLinkReqDTO;
import com.nageoffer.shortlink.admin.remote.resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/recycle-bin")
public class RecycleBinController {

    private final ShortLinkActualService shortLinkActualService;

    private final GroupService groupService;

    @PostMapping("/save")
    public Result<Void> saveShortLink2RecycleBin(@RequestBody SaveShortLinkReqDTO requestParams){
        return shortLinkActualService.saveShortLink2RecycleBin(requestParams);
    }
    @GetMapping("/page")
    public Result<Page<RecycleBinPageRespDTO>> pageQueryShortLink(RecycleBinPageReqDTO requestParams){
        String username = UserContext.getUsername();
        List<String> gids = groupService.query().select("gid")
                .eq("username", username)
                .eq("del_flag", 0)
                .orderByDesc("update_time")
                .list().stream()
                .map(GroupDO::getGid)
                .toList();
        requestParams.setGids(gids);
        return shortLinkActualService.pageQueryShortLink(requestParams);
    }

    @PostMapping("/recover")
    public Result<Void> recoverShortLink(@RequestBody RecoverShortLinkReqDTO requestParams){
        return shortLinkActualService.recoverShortLink(requestParams);
    }

    @PostMapping("/remove")
    public Result<Void> removeShortLink(@RequestBody RemoveShortLinkReqDTO requestParams){
        return shortLinkActualService.removeShortLink(requestParams);
    }
}
