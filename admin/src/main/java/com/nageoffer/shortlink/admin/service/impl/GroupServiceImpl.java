package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.constants.UserConstants;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.GroupSortDTO;
import com.nageoffer.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.GroupRespDTO;
import com.nageoffer.shortlink.admin.remote.ShortLinkActualService;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkQueryCountRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.util.BeanUtil;
import com.nageoffer.shortlink.admin.util.RandomGenerator;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    private final ShortLinkActualService shortLinkActualService;

    private final RedissonClient redissonClient;

    @Override
    public void saveGroup(String name, String username) {
        RLock lock = redissonClient.getLock(UserConstants.LOCK_SAVE_GROUP + username);
        lock.lock();
        try{
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0);
            List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
            if(!CollUtil.isEmpty(groupDOList) && groupDOList.size() == 20){
                throw new ClientException("已超出最大分组数量");
            }
            String gid;
            do{
                gid = RandomGenerator.generateRandom();
                GroupDO groupDO = lambdaQuery()
                        .eq(GroupDO::getGid, gid)
                        .eq(GroupDO::getName,name)
                        .eq(GroupDO::getUsername, username).one();
                if(groupDO == null) break;
            }while(true);
            GroupDO groupDO = GroupDO.builder()
                    .gid(gid)
                    .username(username)
                    .sortOrder(0)
                    .name(name).build();
            save(groupDO);
        }finally {
            lock.unlock();
        }

    }

    @Override
    public List<GroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> wrapper = new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag,0)
                .orderByDesc(GroupDO::getSortOrder,GroupDO::getUpdateTime);

        List<GroupDO> groupDOS = baseMapper.selectList(wrapper);

        //查询每个分组的短链数量
        List<String> gids = groupDOS.stream().map(GroupDO::getGid).toList();
        List<ShortLinkQueryCountRespDTO> countList = shortLinkActualService.queryShortLinkCount(gids).getData();

        groupDOS.forEach(group -> {
            countList.stream()
                    .filter(item -> Objects.equals(item.getGid(), group.getGid()))
                    .findFirst()
                    .ifPresent(item -> cn.hutool.core.bean.BeanUtil.copyProperties(item,group));
        });

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
