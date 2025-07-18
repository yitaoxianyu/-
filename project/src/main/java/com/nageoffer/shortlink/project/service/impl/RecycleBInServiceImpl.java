package com.nageoffer.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.RecoverShortLinkReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.RemoveShortLinkReqDTO;
import com.nageoffer.shortlink.project.dto.req.SaveShortLinkReqDTO;
import com.nageoffer.shortlink.project.dto.resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.project.service.RecycleBinService;
import com.nageoffer.shortlink.project.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.nageoffer.shortlink.project.common.constants.ShortLinkConstants.SHORT_LINK_GOTO_KEY;

@Service
@RequiredArgsConstructor
public class RecycleBInServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveLink2RecycleBin(SaveShortLinkReqDTO requestParams) {
        //构建查询条件
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParams.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParams.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getEnableStatus,1);
        //构建更新实体
        ShortLinkDO shortLinkDO = ShortLinkDO.builder().enableStatus(0).build();
        //更新启用状态
        int updated = baseMapper.update(shortLinkDO, updateWrapper);
        if(updated > 0) {
            stringRedisTemplate.delete(
                    String.format(SHORT_LINK_GOTO_KEY, requestParams.getFullShortUrl())
            );
        }
    }

    @Override
    public IPage<RecycleBinPageRespDTO> pageQueryShortLink(RecycleBinPageReqDTO requestParms) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .in(ShortLinkDO::getGid, requestParms.getGids())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);

        return baseMapper.selectPage(requestParms, queryWrapper)
                .convert(item -> BeanUtil.convert(item, RecycleBinPageRespDTO.class));
    }

    @Override
    public void recoverShortLink(RecoverShortLinkReqDTO requestParams) {
        //构建查询条件
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParams.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParams.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getEnableStatus,0);
        //构建更新实体
        ShortLinkDO shortLinkDO = ShortLinkDO.builder().enableStatus(1).build();
        //更新启用状态
        int updated = baseMapper.update(shortLinkDO, updateWrapper);
        if(updated > 0) {
            stringRedisTemplate.delete(
                    String.format(SHORT_LINK_GOTO_KEY, requestParams.getFullShortUrl())
            );
        }
    }

    @Override
    public void removeShortLink(RemoveShortLinkReqDTO requestParams) {
        //构建查询条件
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParams.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParams.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getEnableStatus,0);
        //构建更新实体
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setDelFlag(1);
        //更新启用状态
        int updated = baseMapper.update(shortLinkDO, updateWrapper);
        if(updated > 0) {
            stringRedisTemplate.delete(
                    String.format(SHORT_LINK_GOTO_KEY, requestParams.getFullShortUrl())
            );
        }
    }
}
