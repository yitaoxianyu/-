package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ClientException;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkQueryCountDTO;
import com.nageoffer.shortlink.project.dto.resp.stats.ShortLinkStatsRecordDTO;
import com.nageoffer.shortlink.project.mq.DelayShortLinkStatsProducer;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.service.ShortLinkStatsTodayService;
import com.nageoffer.shortlink.project.util.BeanUtil;
import com.nageoffer.shortlink.project.util.HashUtil;
import com.nageoffer.shortlink.project.util.LinkUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.nageoffer.shortlink.project.common.constants.ShortLinkConstants.*;
import static com.nageoffer.shortlink.project.common.enums.ValidDateTypeEnum.PERMANENT;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService, ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    private ApplicationContext applicationContext;

    private final ShortLinkMapper shortLinkMapper;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final ShortLinkStatsMapper shortLinkStatsMapper;

    private final ShortLinkLocaleStatsMapper shortLinkLocaleStatsMapper;

    private final ShortLinkBrowserStatsMapper shortLinkBrowserStatsMapper;

    private final ShortLinkOsStatsMapper shortLinkOsStatsMapper;

    private final ShortLinkNetworkStatsMapper shortLinkNetworkStatsMapper;

    private final ShortLinkDeviceStatsMapper shortLinkDeviceStatsMapper;

    private final ShortLinkAccessLogsMapper shortlinkAccessLogsMapper;

    private final ShortLinkStatsTodayMapper shortLinkStatsTodayMapper;

    private final ShortLinkStatsTodayService shortLinkStatsTodayService;

    private final RBloomFilter<String> shortUriCreateBloomFilter;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;


    @Value("${short-link.api.locale-stats.key}")
    private String localeStatsKey;

    @Value("${short-link.default-domain}")
    private String defaultDomain;

    //这个方法使用了 try-catch 不会造成事务失效因为捕获异常之后又抛出了
    @Override
    @Transactional
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParams) {
        String domain = Optional.ofNullable(requestParams.getDomain()).orElse(defaultDomain);
        String suffix = generateSuffix(requestParams,domain);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(domain)
                .shortUri(suffix)
                .fullShortUrl(domain + "/" + suffix)
                .originUrl(requestParams.getOriginUrl())
                .gid(requestParams.getGid())
                .describe(requestParams.getDescribe())
                .validDateType(requestParams.getValidDateType())
                .validDate(
                        !Objects.equals(requestParams.getValidDateType(),PERMANENT.getType()) ?
                                requestParams.getValidDate() : null
                )
                .createdType(requestParams.getCreatedType())
                .enableStatus(1)
                .delTime(0L)
                .favicon(requestParams.getFavicon()).build();

        //这里不仅要插入短链接数据,还要插入跳转数据
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(domain + "/" + suffix)
                .gid(requestParams.getGid()).build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        }catch (DuplicateKeyException ex){
            throw new ServiceException(String.format("短链接%s重复",domain + "/" + suffix));
        }
        //缓存预热
        stringRedisTemplate.opsForValue().set(
                String.format(SHORT_LINK_GOTO_KEY,domain + "/" + suffix),
                requestParams.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParams.getValidDate()), TimeUnit.SECONDS
        );
        shortUriCreateBloomFilter.add(domain + "/" + suffix);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + domain + "/" + suffix)
                .originUrl(requestParams.getOriginUrl())
                .gid(requestParams.getGid()).build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageQuery(ShortLinkPageReqDTO requestParams) {
        IPage<ShortLinkDO> shortLinkDOPage = shortLinkMapper.pageQuery(requestParams);
        return shortLinkDOPage.convert(item -> BeanUtil.convert(item, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkQueryCountDTO> queryShortLinkCount(List<String> requestParams) {
        QueryWrapper<ShortLinkDO> wrapper = new QueryWrapper<ShortLinkDO>()
                .select("gid,count(*) as shortLinkCount")
                .eq("enable_status", 1)
                .eq("del_flag", 0)
                .eq("del_time",0L)
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
        // 1. 参数校验：非永久类型时，validDate 必须非 null
        Integer validDateType = requestParms.getValidDateType();
        Date validDate = requestParms.getValidDate();
        if (validDateType != null && !Objects.equals(validDateType, PERMANENT.getType())) {
            // 非永久类型（如自定义），必须传入 validDate
            if (validDate == null) {
                throw new ClientException("非永久类型的短链接，有效期不能为空");
            }
        }
        //sharding sphere中的分片键默认不能进行修改的,但是可以先删除再插入这样
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParms.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParms.getOriginalGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 1);

        ShortLinkDO shortLinkInDB = baseMapper.selectOne(queryWrapper);
        if (shortLinkInDB == null) {
            throw new ClientException("短链接不存在");
        }

        if(requestParms.getTargetGid() == null || Objects.equals(requestParms.getTargetGid(),shortLinkInDB.getGid())){
            //证明不想更改分组直接在原来数据修改
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParms.getFullShortUrl())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 1)
                    .set(Objects.equals(requestParms.getValidDateType(), PERMANENT.getType()), ShortLinkDO::getValidDate, null)
                    .set(Objects.equals(shortLinkInDB.getValidDateType(), PERMANENT.getType())
                                    && requestParms.getValidDateType() == null,
                            ShortLinkDO::getValidDate, null);

            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .originUrl(requestParms.getOriginUrl())
                    .describe(requestParms.getDescribe())
                    .validDateType(requestParms.getValidDateType())
                    .validDate(requestParms.getValidDate())
                    .favicon(Objects.equals(requestParms.getOriginUrl(),shortLinkInDB.getOriginUrl()) ? null : getFavicon(requestParms.getOriginUrl()))
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParms.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            if (!rLock.tryLock()) {
                throw new ServiceException("短链接正在跳转");
            }
            try{
                LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, shortLinkInDB.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getDelTime, 0L)
                        .eq(ShortLinkDO::getEnableStatus, 1);
                ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                delShortLinkDO.setDelFlag(1);
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .domain(defaultDomain)
                        .originUrl(requestParms.getOriginUrl())
                        .gid(requestParms.getTargetGid())
                        .createdType(shortLinkInDB.getCreatedType())
                        .validDateType(requestParms.getValidDateType())
                        .validDate(requestParms.getValidDate())
                        .describe(requestParms.getDescribe())
                        .shortUri(shortLinkInDB.getShortUri())
                        .enableStatus(shortLinkInDB.getEnableStatus())
                        .totalPv(shortLinkInDB.getTotalPv())
                        .totalUv(shortLinkInDB.getTotalUv())
                        .totalUip(shortLinkInDB.getTotalUip())
                        .fullShortUrl(shortLinkInDB.getFullShortUrl())
                        .favicon(getFavicon(requestParms.getOriginUrl()))
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);
                LambdaQueryWrapper<ShortLinkStatsTodayDO> statsTodayQueryWrapper = Wrappers.lambdaQuery(ShortLinkStatsTodayDO.class)
                        .eq(ShortLinkStatsTodayDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkStatsTodayDO::getGid, shortLinkInDB.getGid())
                        .eq(ShortLinkStatsTodayDO::getDelFlag, 0);
                List<ShortLinkStatsTodayDO> linkStatsTodayDOList = shortLinkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    shortLinkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(ShortLinkStatsTodayDO::getId)
                            .toList()
                    );
                    linkStatsTodayDOList.forEach(each -> each.setGid(requestParms.getTargetGid()));
                    shortLinkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, shortLinkInDB.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(requestParms.getTargetGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);
                LambdaUpdateWrapper<ShortLinkStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkStatsDO.class)
                        .eq(ShortLinkStatsDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkStatsDO::getGid, shortLinkInDB.getGid())
                        .eq(ShortLinkStatsDO::getDelFlag, 0);
                ShortLinkStatsDO linkAccessStatsDO = ShortLinkStatsDO.builder()
                        .gid(requestParms.getTargetGid())
                        .build();
                shortLinkStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                LambdaUpdateWrapper<ShortLinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkLocaleStatsDO.class)
                        .eq(ShortLinkLocaleStatsDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkLocaleStatsDO::getGid, shortLinkInDB.getGid())
                        .eq(ShortLinkLocaleStatsDO::getDelFlag, 0);
                ShortLinkLocaleStatsDO linkLocaleStatsDO = ShortLinkLocaleStatsDO.builder()
                        .gid(requestParms.getTargetGid())
                        .build();
                shortLinkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                LambdaUpdateWrapper<ShortLinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkOsStatsDO.class)
                        .eq(ShortLinkOsStatsDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkOsStatsDO::getGid, shortLinkInDB.getGid())
                        .eq(ShortLinkOsStatsDO::getDelFlag, 0);
                ShortLinkOsStatsDO linkOsStatsDO = ShortLinkOsStatsDO.builder()
                        .gid(requestParms.getTargetGid())
                        .build();
                shortLinkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<ShortLinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkBrowserStatsDO.class)
                        .eq(ShortLinkBrowserStatsDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkBrowserStatsDO::getGid, shortLinkInDB.getGid())
                        .eq(ShortLinkBrowserStatsDO::getDelFlag, 0);
                ShortLinkBrowserStatsDO linkBrowserStatsDO = ShortLinkBrowserStatsDO.builder()
                        .gid(requestParms.getTargetGid())
                        .build();
                shortLinkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<ShortLinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDeviceStatsDO.class)
                        .eq(ShortLinkDeviceStatsDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkDeviceStatsDO::getGid, shortLinkInDB.getGid())
                        .eq(ShortLinkDeviceStatsDO::getDelFlag, 0);
                ShortLinkDeviceStatsDO linkDeviceStatsDO = ShortLinkDeviceStatsDO.builder()
                        .gid(requestParms.getTargetGid())
                        .build();
                shortLinkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                LambdaUpdateWrapper<ShortLinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkNetworkStatsDO.class)
                        .eq(ShortLinkNetworkStatsDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkNetworkStatsDO::getGid, shortLinkInDB.getGid())
                        .eq(ShortLinkNetworkStatsDO::getDelFlag, 0);
                ShortLinkNetworkStatsDO linkNetworkStatsDO = ShortLinkNetworkStatsDO.builder()
                        .gid(requestParms.getTargetGid())
                        .build();
                shortLinkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<ShortLinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkAccessLogsDO.class)
                        .eq(ShortLinkAccessLogsDO::getFullShortUrl, requestParms.getFullShortUrl())
                        .eq(ShortLinkAccessLogsDO::getGid, shortLinkInDB.getGid())
                        .eq(ShortLinkAccessLogsDO::getDelFlag, 0);
                ShortLinkAccessLogsDO linkAccessLogsDO = ShortLinkAccessLogsDO.builder()
                        .gid(requestParms.getTargetGid())
                        .build();
                shortlinkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);
            }finally {
                rLock.unlock();
            }
            
        }

        String fullShortUrl = requestParms.getFullShortUrl();
        stringRedisTemplate.delete(String.format(SHORT_LINK_GOTO_KEY,fullShortUrl));
    }

    @Override
    @SneakyThrows
    public void restoreUrl(String shortUri, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String fullShortUrl = httpServletRequest.getServerName() + "/" + shortUri;
        //先去查一下缓存,可能会缓存了一些空对象,或者之前缓存好的数据
        String originUrl = stringRedisTemplate.opsForValue().get(String.format(SHORT_LINK_GOTO_KEY, fullShortUrl));
        if(originUrl != null) {
            if(originUrl.equals("-")){
                httpServletResponse.sendRedirect("/page/notfound");
                return ;
            }
            ShortLinkStatsRecordDTO shortLinkStatsRecordDTO = buildShortLinkStatsRecordDTO(httpServletRequest, httpServletResponse, fullShortUrl);
            recordStats(fullShortUrl,null,shortLinkStatsRecordDTO);
            httpServletResponse.sendRedirect(originUrl);
            return ;
        }
        if (!shortUriCreateBloomFilter.contains(fullShortUrl)) {
            httpServletResponse.sendRedirect("/page/notfound");
            return ;
        }
        //缓存中没有,用双重检查锁并且布隆过滤器也可能存在误判
        RLock lock = redissonClient.getLock(String.format(LOCK_SHORT_LINK_GOTO_KEY, fullShortUrl));
        lock.lock();
        try{
            //这里再查一下,有可能之前获取的线程已经更新了缓存
            originUrl = stringRedisTemplate.opsForValue().get(String.format(SHORT_LINK_GOTO_KEY, fullShortUrl));
            //这里可能缓存了一个空对象
            if(originUrl != null) {
                if(originUrl.equals("-")){
                    httpServletResponse.sendRedirect("/page/notfound");
                    return ;
                }
                ShortLinkStatsRecordDTO shortLinkStatsRecordDTO = buildShortLinkStatsRecordDTO(httpServletRequest, httpServletResponse, fullShortUrl);
                recordStats(fullShortUrl,null,shortLinkStatsRecordDTO);
                httpServletResponse.sendRedirect(originUrl);
                return ;
            }
            //缓存中还是没有,查数据之后更新缓存,如果没有查到说明布隆过滤器误判,缓存一个空对象
            LambdaQueryWrapper<ShortLinkGotoDO> shortLinkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl,fullShortUrl);

            ShortLinkGotoDO shortLinkGotoInDB = shortLinkGotoMapper.selectOne(shortLinkGotoQueryWrapper);
            if(shortLinkGotoInDB == null){
                //这里需要缓存一个空对象
                stringRedisTemplate.opsForValue().set(
                        String.format(SHORT_LINK_GOTO_KEY,fullShortUrl),
                        "-",
                        600,
                        TimeUnit.SECONDS
                );
                httpServletResponse.sendRedirect("/page/notfound");
                return ;
            }
            String gid = shortLinkGotoInDB.getGid();
            //获取到 gid 再查短链表
            LambdaQueryWrapper<ShortLinkDO> shortLinkQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid,gid)
                    .eq(ShortLinkDO::getFullShortUrl,fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag,0)
                    .eq(ShortLinkDO::getEnableStatus,1);
            ShortLinkDO shortLinkInDB = baseMapper.selectOne(shortLinkQueryWrapper);
            //查出来的过期了,或者被禁用了或者不存在
            if(shortLinkInDB == null || (shortLinkInDB.getValidDate() != null && shortLinkInDB.getValidDate().before(new Date()))){
                //缓存一个空对象
                stringRedisTemplate.opsForValue().set(
                        String.format(SHORT_LINK_GOTO_KEY,fullShortUrl),
                        "-",
                        600,
                        TimeUnit.SECONDS
                );
                httpServletResponse.sendRedirect("/page/notfound");
                return ;
            }
            //这里有效期为永久或者还未过期
            originUrl = shortLinkInDB.getOriginUrl();
            //这里将查到的数据进行缓存
            stringRedisTemplate.opsForValue().set(
                    String.format(SHORT_LINK_GOTO_KEY, fullShortUrl),
                    originUrl,
                    LinkUtil.getLinkCacheValidTime(shortLinkInDB.getValidDate()),
                    TimeUnit.SECONDS
            );
            //进行跳转并且更新更新数据
            ShortLinkStatsRecordDTO shortLinkStatsRecordDTO = buildShortLinkStatsRecordDTO(httpServletRequest, httpServletResponse, fullShortUrl);
            recordStats(shortLinkInDB.getFullShortUrl(),shortLinkInDB.getGid(),shortLinkStatsRecordDTO);
            httpServletResponse.sendRedirect(originUrl);
        }finally{
            lock.unlock();
        }
    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParams) {
        //这里使用自注入避免事务注解失效
        Integer createdType = requestParams.getCreatedType();
        Integer validDateType = requestParams.getValidDateType();
        Date validDate = requestParams.getValidDate();
        String gid = requestParams.getGid();
        String domain = requestParams.getDomain();
        List<String> originUrls = requestParams.getOriginUrl();
        List<String> describes = requestParams.getDescribe();

        //这里使用代理对象
        ShortLinkService proxy = applicationContext.getBean(ShortLinkService.class);
        ShortLinkBatchCreateRespDTO shortLinkBatchCreateRespDTO = new ShortLinkBatchCreateRespDTO();
        List<ShortLinkBatchCreateRespDTO.ShortLinkInfo> results = new ArrayList<>();
        for (int i = 0;i < originUrls.size();i ++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = ShortLinkCreateReqDTO.builder()
                    .gid(gid)
                    .domain(domain)
                    .originUrl(originUrls.get(i))
                    .validDate(validDate)
                    .validDateType(validDateType)
                    .createdType(createdType)
                    .describe(describes.get(i))
                    .build();
            try{
                ShortLinkCreateRespDTO shortLinkCreateRespDTO = proxy.createShortLink(shortLinkCreateReqDTO);
                ShortLinkBatchCreateRespDTO.ShortLinkInfo e = ShortLinkBatchCreateRespDTO.ShortLinkInfo.builder()
                        .fullShortUrl(shortLinkCreateRespDTO.getFullShortUrl())
                        .originUrl(shortLinkCreateRespDTO.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                results.add(e);
            } catch (Exception e) {
                log.info("短链接创建失败;{}",originUrls.get(i));
            }
        }
        shortLinkBatchCreateRespDTO.setGid(gid);
        shortLinkBatchCreateRespDTO.setTotal(results.size());
        shortLinkBatchCreateRespDTO.setShortLinkInfoList(results);

        return shortLinkBatchCreateRespDTO;
    }

    public void recordStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO shortLinkStatsRecordDTO){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        if(!rLock.tryLock()){
            //读锁获取失败,证明正在更新中
            delayShortLinkStatsProducer.add(shortLinkStatsRecordDTO);
            return ;
        }

       try { //填充 fullShortUrl
           if (fullShortUrl == null) {
               fullShortUrl = shortLinkStatsRecordDTO.getFullShortUrl();
           }
           String originIp = shortLinkStatsRecordDTO.getRemoteAddr();
           //填充 gid 字段
           if (Objects.isNull(gid)) {
               LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                       .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
               ShortLinkGotoDO shortLinkGotoInDB = shortLinkGotoMapper.selectOne(queryWrapper);
               gid = shortLinkGotoInDB.getGid();
           }
           Date date = new Date();
           //根据 ip查询地址数据
           HashMap<String, Object> params = new HashMap<>();
           params.put("key", localeStatsKey);
           params.put("ip", originIp);
           String jsonStr = HttpUtil.get("https://restapi.amap.com/v3/ip", params);
           JSONObject jsonObject = JSONObject.parse(jsonStr);
           if (!jsonObject.get("infocode").equals("10000")) {
               throw new ServiceException("服务调用失败");
           }
           //高德 api 只能查询中国这里默认中国
           String province = jsonObject.getString("province");
           String city = jsonObject.getString("city");
           ShortLinkLocaleStatsDO shortLinkLocaleStatsDO = ShortLinkLocaleStatsDO.builder()
                   .country("中国")
                   .province(province)
                   .city(city)
                   .adcode(jsonObject.getString("adcode"))
                   .gid(gid)
                   .fullShortUrl(fullShortUrl)
                   .cnt(1)
                   .date(date).build();
           //获取浏览器信息
           String browser = shortLinkStatsRecordDTO.getBrowser();
           ShortLinkBrowserStatsDO shortLinkBrowserStatsDO = ShortLinkBrowserStatsDO.builder()
                   .browser(browser)
                   .gid(gid)
                   .fullShortUrl(fullShortUrl)
                   .date(date)
                   .cnt(1).build();
           //获取操作系统
           String os = shortLinkStatsRecordDTO.getOs();
           ShortLinkOsStatsDO shortLinkOsStatsDO = ShortLinkOsStatsDO.builder()
                   .os(os)
                   .fullShortUrl(fullShortUrl)
                   .gid(gid)
                   .date(date)
                   .cnt(1).build();
           //获取网络类型
           String network = shortLinkStatsRecordDTO.getNetwork();
           ShortLinkNetworkStatsDO shortLinkNetworkStatsDO = ShortLinkNetworkStatsDO.builder()
                   .network(network)
                   .gid(gid)
                   .fullShortUrl(fullShortUrl)
                   .cnt(1)
                   .date(date)
                   .build();
           //设备信息
           String device = shortLinkStatsRecordDTO.getDevice();
           ShortLinkDeviceStatsDO shortLinkDeviceStatsDO = ShortLinkDeviceStatsDO.builder()
                   .device(device)
                   .gid(gid)
                   .fullShortUrl(fullShortUrl)
                   .cnt(1)
                   .date(date)
                   .build();
           //构建访问记录
           Boolean uvFlag = shortLinkStatsRecordDTO.getUvFirstFlag();
           Boolean ipFlag = shortLinkStatsRecordDTO.getUipFirstFlag();
           String user = shortLinkStatsRecordDTO.getUv();
           int weekday = DateUtil.dayOfWeek(date);
           int hour = DateUtil.hour(date, true);
           ShortLinkStatsDO shortLinkStatsDO = ShortLinkStatsDO.builder()
                   .gid(gid)
                   .fullShortUrl(fullShortUrl)
                   .pv(1)
                   .uv(uvFlag ? 1 : 0)
                   .uip(ipFlag ? 1 : 0)
                   .date(date)
                   .weekday(weekday)
                   .hour(hour)
                   .build();
           //统计将 uip,uv,pv记录到t_link表
           update().setSql("total_pv = total_pv + 1")
                   .setSql(uvFlag, "total_uv = total_uv + 1")
                   .setSql(ipFlag, "total_uip = total_uip + 1")
                   .eq("gid", gid)
                   .eq("full_short_url", fullShortUrl).update();
           //统计今日 uip,uv,pv
           ShortLinkStatsTodayDO shortLinkStatsTodayDO = ShortLinkStatsTodayDO.builder()
                   .date(date)
                   .fullShortUrl(fullShortUrl)
                   .gid(gid)
                   .todayPv(1)
                   .todayUv(uvFlag ? 1 : 0)
                   .todayUip(ipFlag ? 1 : 0)
                   .build();
           //构建访问日志(类似于各个统计数据的概括),地区信息默认是国内
           ShortLinkAccessLogsDO shortLinkAccessLogsDO = ShortLinkAccessLogsDO.builder()
                   .gid(gid)
                   .fullShortUrl(fullShortUrl)
                   .user(user)
                   .os(os)
                   .ip(originIp)
                   .browser(browser)
                   .locale(StrUtil.join("-", "中国", province, city))
                   .network(network)
                   .device(device)
                   .build();

           shortLinkStatsMapper.insertStatsOrUpdate(shortLinkStatsDO);
           shortLinkStatsTodayMapper.insertStatsOrUpdate(shortLinkStatsTodayDO);
           shortLinkLocaleStatsMapper.insertStatsOrUpdate(shortLinkLocaleStatsDO);
           shortLinkBrowserStatsMapper.insertStatsOrUpdate(shortLinkBrowserStatsDO);
           shortLinkOsStatsMapper.insertStatsOrUpdate(shortLinkOsStatsDO);
           shortLinkNetworkStatsMapper.insertStatsOrUpdate(shortLinkNetworkStatsDO);
           shortLinkDeviceStatsMapper.insertStatsOrUpdate(shortLinkDeviceStatsDO);
           shortlinkAccessLogsMapper.insert(shortLinkAccessLogsDO);
       } catch (Throwable e) {
           log.error("短链接统计异常",e);
       }
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParams,String domain){
        int count = 0;
        String suffix;
        while(true){
            if(count == 10) throw new ServiceException("短链接创建繁忙");
            suffix = HashUtil.hashToBase62(requestParams.getOriginUrl() + System.currentTimeMillis());

            if (!shortUriCreateBloomFilter.contains(domain + "/" + suffix)) {
                break;
            }
            count ++;
        }
        return suffix;
    }

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }

    private ShortLinkStatsRecordDTO buildShortLinkStatsRecordDTO(HttpServletRequest request,HttpServletResponse response,String fullShortUrl){
        Cookie[] cookies = request.getCookies();
        //true代表没有该 cookie 没有访问过当前短链接
        AtomicReference<Boolean> uvFlag = new AtomicReference<>(false);
        AtomicReference<String> value = new AtomicReference<>();
        Runnable task = () -> {
            value.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv",value.get());
            //默认 cookie默认关闭浏览器自动失效
            //这里设置为一个月
            uvCookie.setMaxAge(DEFAULT_UV_VALID_TIME);
            //cookie 的作用范围为域名 + uri
            //这里考虑设置一个全局的,根据不同的uri redis 集合来区分是否访问这个 uri了
            response.addCookie(uvCookie);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_UV_KEY + fullShortUrl, value.get());
            uvFlag.set(true);
        };
        //判断是否是空
        if (cookies == null) {
            task.run();
        }else{
            Arrays.stream(cookies)
                    .filter(cookie -> Objects.equals(cookie.getName(),"uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(value1 -> {
                                value.set(value1);
                                Long added = stringRedisTemplate.opsForSet().add(SHORT_LINK_UV_KEY + fullShortUrl,value1);
                                uvFlag.set(added != null && added > 0);
                            },task
                    );
        }
        //判断 ip 是否访问过
        String originIp = LinkUtil.getIp(request);
        Long added = stringRedisTemplate.opsForSet().add(SHORT_LINK_IP_KEY + fullShortUrl, originIp);
        Boolean ipFlag = (added != null && added > 0);

        return ShortLinkStatsRecordDTO.builder()
                .uv(value.get())
                .uipFirstFlag(ipFlag)
                .uipFirstFlag(uvFlag.get())
                .remoteAddr(originIp)
                .browser(LinkUtil.getBrowser(request))
                .network(LinkUtil.getDevice(request))
                .os(LinkUtil.getOs(request))
                .device(LinkUtil.getDevice(request))
                .fullShortUrl(fullShortUrl)
                .build();
    }


}
