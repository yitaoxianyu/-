package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.remote.ShortLinkService;
import com.nageoffer.shortlink.admin.remote.req.RecycleBinPageReqDTO;
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

    private final GroupService groupService;

    private static final ShortLinkService shortLinkRemoteService = new ShortLinkService() {};

    @PostMapping("/save")
    public Result<Void> saveShortLink2RecycleBin(@RequestBody SaveShortLinkReqDTO requestParams){
        return shortLinkRemoteService.saveShortLink2RecycleBin(requestParams);
    }

    @GetMapping("/page")
    public Result<IPage<RecycleBinPageRespDTO>> pageQueryShortLink(RecycleBinPageReqDTO requestParams){
        String username = UserContext.getUsername();
        List<String> gids = groupService.query().select("gid")
                .eq("username", username)
                .eq("del_flag", 0)
                .orderByDesc("update_time")
                .list().stream()
                .map(GroupDO::getGid)
                .toList();
        requestParams.setGids(gids);
        return shortLinkRemoteService.pageQueryShortLink(requestParams);
    }
}
