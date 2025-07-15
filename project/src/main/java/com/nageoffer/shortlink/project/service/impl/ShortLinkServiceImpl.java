package com.nageoffer.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ClientException;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkQueryCountDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.util.BeanUtil;
import com.nageoffer.shortlink.project.util.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.nageoffer.shortlink.project.common.constants.ShortLinkConstants.LOCK_SHORT_LINK_GOTO_KEY;
import static com.nageoffer.shortlink.project.common.constants.ShortLinkConstants.SHORT_LINK_GOTO_KEY;
import static com.nageoffer.shortlink.project.common.enums.ValidDateTypeEnum.PERMANENT;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final RBloomFilter<String> shortUriCreateBloomFilter;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    @Override
    @Transactional
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
        //这里不仅要插入短链接数据,还要插入跳转数据
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(requestParams.getDomain() + "/" + suffix)
                .gid(requestParams.getGid()).build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
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
    /*
    在 MyBatis-Plus 里，默认的更新策略是 NOT_NULL，也就是说，只有那些值不为 null 的字段才会被更新到数据库中。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParms) {
        //这里 gid 认为是不能更改的,sharding sphere中的分片键默认不能进行修改的,但是可以先删除再插入这样
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParms.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParms.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 1);

        ShortLinkDO shortLinkInDB = baseMapper.selectOne(queryWrapper);
        if (shortLinkInDB == null) {
            throw new ClientException("短链接不存在");
        }
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParms.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 1)
                .set(Objects.equals(requestParms.getValidDateType(), PERMANENT.getType()), ShortLinkDO::getValidDate, null);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .originUrl(requestParms.getOriginUrl())
                .describe(requestParms.getDescribe())
                .validDateType(requestParms.getValidDateType())
                .validDate(requestParms.getValidDate())
                .build();
    }

    @Override
    @SneakyThrows
    public void restoreUrl(String shortUri, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        //跳转表获取 gid
        String fullShortUrl = httpServletRequest.getServerName() + "/" + shortUri;
        if (!shortUriCreateBloomFilter.contains(fullShortUrl)) {
            throw new ClientException("短链接不存在");
        }
        //此时 fullShortUrl 可能存在误判,查询一下缓存,查的是 originUrl
        String originUrl = stringRedisTemplate.opsForValue().get(String.format(SHORT_LINK_GOTO_KEY, fullShortUrl));
        if(originUrl != null) {
            httpServletResponse.sendRedirect(originUrl);
            return ;
        }
        //缓存中没有,用双重检查锁
        RLock lock = redissonClient.getLock(String.format(LOCK_SHORT_LINK_GOTO_KEY, fullShortUrl));
        lock.lock();
        try{
            //这里再查一下,有可能之前获取的线程已经更新了缓存
            originUrl = stringRedisTemplate.opsForValue().get(String.format(SHORT_LINK_GOTO_KEY, fullShortUrl));
            if(originUrl != null) {
                httpServletResponse.sendRedirect(originUrl);
                return ;
            }
            //缓存中还是没有,查数据之后更新缓存,如果没有查到说明布隆过滤器误判,缓存一个空对象
            LambdaQueryWrapper<ShortLinkGotoDO> shortLinkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl,fullShortUrl);

            ShortLinkGotoDO shortLinkGotoInDB = shortLinkGotoMapper.selectOne(shortLinkGotoQueryWrapper);
            if(shortLinkGotoInDB == null){
                //这里需要缓存一个空对象
                stringRedisTemplate.opsForValue().set(String.format(SHORT_LINK_GOTO_KEY,fullShortUrl),"-");
                throw new ClientException("短链接不存在");
            }
            String gid = shortLinkGotoInDB.getGid();
            //获取到 gid 再查短链表
            LambdaQueryWrapper<ShortLinkDO> shortLinkQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid,gid)
                    .eq(ShortLinkDO::getFullShortUrl,fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag,0)
                    .eq(ShortLinkDO::getEnableStatus,1);
            ShortLinkDO shortLinkInDB = baseMapper.selectOne(shortLinkQueryWrapper);
            originUrl = shortLinkInDB.getOriginUrl();
            //这里将查到的数据进行缓存
            stringRedisTemplate.opsForValue().set(String.format(SHORT_LINK_GOTO_KEY, fullShortUrl),originUrl);
            //进行跳转
            httpServletResponse.sendRedirect(originUrl);
        }finally{
            lock.unlock();
        }

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
