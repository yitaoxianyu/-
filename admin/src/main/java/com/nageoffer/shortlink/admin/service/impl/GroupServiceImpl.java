package com.nageoffer.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.GroupSortDTO;
import com.nageoffer.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.GroupRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.util.BeanUtil;
import com.nageoffer.shortlink.admin.util.RandomGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String name) {
        String gid;
        do{
            gid = RandomGenerator.generateRandom();
            GroupDO groupDO = lambdaQuery()
                    .eq(GroupDO::getGid, gid)
                    .eq(GroupDO::getName,name)
                    .eq(GroupDO::getUsername, UserContext.getUsername()).one();
            if(groupDO == null) break;
        }while(true);

        GroupDO groupDO = GroupDO.builder()
                .gid(gid).username(UserContext.getUsername()).sortOrder(0).name(name)
                .build();
        save(groupDO);
    }

    @Override
    public List<GroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> wrapper = new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getUsername, UserContext.getUsername()).eq(GroupDO::getDelFlag,0)
                        .orderByDesc(GroupDO::getSortOrder,GroupDO::getUpdateTime);

        List<GroupDO> groupDOS = baseMapper.selectList(wrapper);
        return BeanUtil.convert(groupDOS, GroupRespDTO.class);
    }

    @Override
    public void updateGroup(GroupUpdateReqDTO requestParams) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParams.getGid())
                .eq(GroupDO::getDelFlag,0);

        baseMapper.update(BeanUtil.convert(requestParams,GroupDO.class),updateWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        UpdateWrapper<GroupDO> updateWrapper = new UpdateWrapper<GroupDO>()
                .eq("gid", gid)
                .eq("username",UserContext.getUsername())
                .setSql("del_flag = 1");
        update(updateWrapper);
    }

    @Override
    public void sortGroup(List<GroupSortDTO> list) {
        list.forEach((item) -> {
            GroupDO groupDO = GroupDO.builder()
                    .gid(item.getGid())
                    .sortOrder(item.getSortOrder())
                    .build();
            LambdaQueryWrapper<GroupDO> wrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, item.getGid())
                    .eq(GroupDO::getUsername, UserContext.getUsername());
            update(groupDO,wrapper);
        });
    }
}
