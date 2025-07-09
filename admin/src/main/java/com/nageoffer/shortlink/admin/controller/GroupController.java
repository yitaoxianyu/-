package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.req.GroupSortDTO;
import com.nageoffer.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.GroupRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/group")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public Result<Void> saveGroup(@RequestParam String groupName){
        groupService.saveGroup(groupName);
        return Results.success();
    }

    @GetMapping
    public Result<List<GroupRespDTO>> listGroup(){
        return Results.success(groupService.listGroup());
    }

    @PutMapping
    public Result<Void> updateGroup(@RequestBody GroupUpdateReqDTO requestParams){
        groupService.updateGroup(requestParams);
        return Results.success();
    }

    @DeleteMapping
    public Result<Void> deleteGroup(@RequestParam String gid){
        groupService.deleteGroup(gid);
        return Results.success();
    }

    @PostMapping("/sort")
    public Result<Void> sortGroup(@RequestBody List<GroupSortDTO> list){
        groupService.sortGroup(list);
        return Results.success();
    }

}
