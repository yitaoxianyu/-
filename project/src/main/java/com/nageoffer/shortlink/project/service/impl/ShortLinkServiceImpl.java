package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
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
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkQueryCountDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.util.BeanUtil;
import com.nageoffer.shortlink.project.util.HashUtil;
import com.nageoffer.shortlink.project.util.LinkUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.nageoffer.shortlink.project.common.constants.ShortLinkConstants.*;
import static com.nageoffer.shortlink.project.common.enums.ValidDateTypeEnum.PERMANENT;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final ShortLinkStatsMapper shortLinkStatsMapper;

    private final ShortLinkLocaleStatsMapper shortLinkLocaleStatsMapper;

    private final ShortLinkBrowserStatsMapper shortLinkBrowserStatsMapper;

    private final ShortLinkOsStatsMapper shortLinkOsStatsMapper;

    private final ShortLinkNetworkStatsMapper shortLinkNetworkStatsMapper;

    private final ShortLinkDeviceStatsMapper shortLinkDeviceStatsMapper;

    private final ShortLinkAccessLogsMapper shortlinkAccessLogsMapper;

    private final RBloomFilter<String> shortUriCreateBloomFilter;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    @Value("${short-link.api.locale-stats.key}")
    private String localeStatsKey;

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
                .validDate(
                        !Objects.equals(requestParams.getValidDateType(),PERMANENT.getType()) ?
                                requestParams.getValidDate() : null
                )
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
        stringRedisTemplate.opsForValue().set(
                String.format(SHORT_LINK_GOTO_KEY,requestParams.getDomain() + "/" + suffix),
                requestParams.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParams.getValidDate()), TimeUnit.SECONDS
        );
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
        baseMapper.update(shortLinkDO,updateWrapper);
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
            recordStats(fullShortUrl,null,httpServletRequest,httpServletResponse);
            httpServletResponse.sendRedirect(originUrl);
            return ;
        }
        //缓存没有命中,考虑查询数据库之前使用布隆过滤器判断有没有
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
                recordStats(fullShortUrl,null,httpServletRequest,httpServletResponse);
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
            recordStats(shortLinkInDB.getFullShortUrl(),shortLinkInDB.getGid(),httpServletRequest,httpServletResponse);
            httpServletResponse.sendRedirect(originUrl);
        }finally{
            lock.unlock();
        }
    }

    private void recordStats(String fullShortUrl,String gid,HttpServletRequest request,HttpServletResponse response){
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
        //填充 gid 字段
        if (Objects.isNull(gid)) {
            LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoInDB = shortLinkGotoMapper.selectOne(queryWrapper);
            gid = shortLinkGotoInDB.getGid();
        }
        Date date = new Date();
        //根据 ip查询地址数据
        HashMap<String,Object> params = new HashMap<>();
        params.put("key",localeStatsKey);
        params.put("ip",originIp);
        String jsonStr = HttpUtil.get("http://restapi.amap.com/v3/ip", params);
        JSONObject jsonObject = JSONObject.parse(jsonStr);
        if (!jsonObject.get("infocode").equals("10000")){
            throw new ServiceException("服务调用失败");
        }
        //高德 api 只能查询中国这里默认中国
        ShortLinkLocaleStatsDO shortLinkLocaleStatsDO = ShortLinkLocaleStatsDO.builder()
                .country("中国")
                .province(jsonObject.getString("province"))
                .city(jsonObject.getString("city"))
                .adcode(jsonObject.getString("adcode"))
                .gid(gid)
                .fullShortUrl(fullShortUrl)
                .cnt(1)
                .date(date).build();
        //获取浏览器信息
        String browser = LinkUtil.getBrowser(request);
        ShortLinkBrowserStatsDO shortLinkBrowserStatsDO = ShortLinkBrowserStatsDO.builder()
                .browser(browser)
                .gid(gid)
                .fullShortUrl(fullShortUrl)
                .date(date)
                .cnt(1).build();
        //获取操作系统
        String os = LinkUtil.getOs(request);
        ShortLinkOsStatsDO shortLinkOsStatsDO = ShortLinkOsStatsDO.builder()
                .os(os)
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(date)
                .cnt(1).build();
        //获取网络类型
        String network = LinkUtil.getNetwork(request);
        ShortLinkNetworkStatsDO shortLinkNetworkStatsDO = ShortLinkNetworkStatsDO.builder()
                .network(network)
                .gid(gid)
                .fullShortUrl(fullShortUrl)
                .cnt(1)
                .date(date)
                .build();
        //设备信息
        String device = LinkUtil.getDevice(request);
        ShortLinkDeviceStatsDO shortLinkDeviceStatsDO = ShortLinkDeviceStatsDO.builder()
                .device(device)
                .gid(gid)
                .fullShortUrl(fullShortUrl)
                .cnt(1)
                .date(date)
                .build();
        //构建访问记录
        int weekday = DateUtil.dayOfWeek(date);
        int hour = DateUtil.hour(date, true);
        ShortLinkStatsDO shortLinkStatsDO = ShortLinkStatsDO.builder()
                .gid(gid)
                .fullShortUrl(fullShortUrl)
                .pv(1)
                .uv(uvFlag.get() ? 1 : 0)
                .uip(ipFlag ? 1 : 0)
                .date(date)
                .weekday(weekday)
                .hour(hour)
                .build();
        //构建访问日志(类似于各个统计数据的概括)
        ShortLinkAccessLogsDO shortLinkAccessLogsDO = ShortLinkAccessLogsDO.builder()
                .gid(gid)
                .fullShortUrl(fullShortUrl)
                .user(value.get())
                .os(os)
                .ip(originIp)
                .browser(browser)
                .build();

        shortLinkStatsMapper.insertStatsOrUpdate(shortLinkStatsDO);
        shortLinkLocaleStatsMapper.insertStatsOrUpdate(shortLinkLocaleStatsDO);
        shortLinkBrowserStatsMapper.insertStatsOrUpdate(shortLinkBrowserStatsDO);
        shortLinkOsStatsMapper.insertStatsOrUpdate(shortLinkOsStatsDO);
        shortLinkNetworkStatsMapper.insertStatsOrUpdate(shortLinkNetworkStatsDO);
        shortLinkDeviceStatsMapper.insertStatsOrUpdate(shortLinkDeviceStatsDO);
        shortlinkAccessLogsMapper.insert(shortLinkAccessLogsDO);
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
