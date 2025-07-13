package com.nageoffer.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkQueryCountDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.util.BeanUtil;
import com.nageoffer.shortlink.project.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateBloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParams) {
        String suffix = generateSuffix(requestParams);

        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParams.getDomain())
                .shortUri(suffix)
                .fullShortUrl(requestParams.getDomain() + "/" + suffix)
                .originUrl(requestParams.getOriginUrl())
                .gid(requestParams.getGid())
                .describe(requestParams.getDescribe())
                .validDateType(requestParams.getValidDateType())
                .validDate(requestParams.getValidDate())
                .createdType(requestParams.getCreatedType())
                .enableStatus(1)
                .favicon(requestParams.getFavicon()).build();
        try {
            baseMapper.insert(shortLinkDO);
        }catch (DuplicateKeyException ex){
            throw new ServiceException(String.format("短链接%s重复",requestParams.getDomain() + "/" + suffix));
        }
        shortUriCreateBloomFilter.add(requestParams.getDomain() + "/" + suffix);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + requestParams.getDomain() + "/" + suffix)
                .originUrl(requestParams.getOriginUrl())
                .gid(requestParams.getGid()).build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageQuery(ShortLinkPageReqDTO requestParams) {
        LambdaQueryWrapper<ShortLinkDO> wrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParams.getGid())
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getEnableStatus,1);

        return baseMapper.selectPage(requestParams, wrapper)
                .convert(item -> BeanUtil.convert(item, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkQueryCountDTO> queryShortLinkCount(List<String> requestParams) {
        QueryWrapper<ShortLinkDO> wrapper = new QueryWrapper<ShortLinkDO>()
                .select("gid,count(*) as shortLinkCount")
                .eq("enable_status", 1)
                .eq("del_flag", 0)
                .in("gid", requestParams)
                .groupBy("gid");


        return  baseMapper.selectMaps(wrapper).stream().
                map(item -> BeanUtil.convert(item, ShortLinkQueryCountDTO.class))
                .toList();
    }
    private String generateSuffix(ShortLinkCreateReqDTO requestParams){
        int count = 0;
        String suffix;
        while(true){
            if(count == 10) throw new ServiceException("短链接创建繁忙");
            suffix = HashUtil.hashToBase62(requestParams.getOriginUrl() + System.currentTimeMillis());

            if (!shortUriCreateBloomFilter.contains(requestParams.getDomain() + "/" + suffix)) {
                break;
            }
            count ++;
        }
        return suffix;
    }
}
