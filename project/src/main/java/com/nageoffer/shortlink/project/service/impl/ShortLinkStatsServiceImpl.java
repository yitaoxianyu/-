package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.project.common.convention.exception.ClientException;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.req.ShortLinkAccessLogsReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.project.dto.resp.logs.ShortLinkAccessLogsRespDTO;
import com.nageoffer.shortlink.project.dto.resp.stats.*;
import com.nageoffer.shortlink.project.service.ShortLinkStatsService;
import com.nageoffer.shortlink.project.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    @Autowired
    private ShortLinkStatsMapper shortLinkStatsMapper;

    @Autowired
    private ShortLinkLocaleStatsMapper shortLinkLocaleStatsMapper;

    @Autowired
    private ShortLinkAccessLogsMapper shortLinkAccessLogsMapper;

    @Autowired
    private ShortLinkBrowserStatsMapper shortLinkBrowserStatsMapper;

    @Autowired
    private ShortLinkNetworkStatsMapper shortLinkNetworkStatsMapper;

    @Autowired
    private ShortLinkDeviceStatsMapper shortLinkDeviceStatsMapper;

    @Override
    public ShortLinkStatsRespDTO showOneUrlStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO) {
        DateTime start = DateUtil.parse(shortLinkStatsReqDTO.getStartDate(),"yyyy-MM-dd");
        DateTime end = DateUtil.parseDate(shortLinkStatsReqDTO.getEndDate());
        String gid = shortLinkStatsReqDTO.getGid();
        String fullShortUrl = shortLinkStatsReqDTO.getFullShortUrl();
        //按照日期分组
        List<ShortLinkStatsDO> shortLinkStatsByDate = shortLinkStatsMapper.selectListByDate(start,end,gid,fullShortUrl);
        if(CollUtil.isEmpty(shortLinkStatsByDate)){
            return null;
        }
        // 计算总PV
        int totalPv = shortLinkStatsByDate.stream()
                .mapToInt(ShortLinkStatsDO::getPv)
                .sum();

        // 计算总UV
        int totalUv = shortLinkStatsByDate.stream()
                .mapToInt(ShortLinkStatsDO::getUv)
                .sum();

        // 计算总UIP
        int totalUip = shortLinkStatsByDate.stream()
                .mapToInt(ShortLinkStatsDO::getUip)
                .sum();
        //将一天 24小时的数据合成一个对象
        DateTime current = DateTime.of(start.toJdkDate());
        List<ShortLinkDailyStatsRespDTO> daily = new ArrayList<>();
        while(!current.isAfter(end)){
            shortLinkStatsByDate.stream()
                    .filter(item -> Objects.equals(item.getDate(),current))
                    .findFirst()
                    .ifPresentOrElse(
                        item -> {
                            ShortLinkDailyStatsRespDTO shortLinkDailyStatsRespDTO = ShortLinkDailyStatsRespDTO.builder()
                                    .date(item.getDate())
                                    .uip(item.getUip())
                                    .uv(item.getUv())
                                    .pv(item.getPv())
                                    .build();
                            daily.add(shortLinkDailyStatsRespDTO);
                        },() -> {
                                ShortLinkDailyStatsRespDTO shortLinkDailyStatsRespDTO = ShortLinkDailyStatsRespDTO.builder()
                                        .date(current.toJdkDate())
                                        .uip(0)
                                        .uv(0)
                                        .pv(0)
                                        .build();
                                daily.add(shortLinkDailyStatsRespDTO);
                            }
                    );
            current.offset(DateField.DAY_OF_MONTH,1);
        }
        //根据地区分组查询数据
        List<ShortLinkLocaleStatsDO> shortLinkLocaleStatsDOS = shortLinkLocaleStatsMapper.selectListByProvince(start, end, gid, fullShortUrl);
        //lambda 中的外部变量需要是effective final:声明之后不能被重新赋值
        Integer localeSum = shortLinkLocaleStatsDOS.stream().mapToInt(ShortLinkLocaleStatsDO::getCnt).sum();
        List<ShortLinkLocaleStatsRespDTO> localeStats = new ArrayList<>();
        shortLinkLocaleStatsDOS.forEach(
                item -> {
                    Double radio = item.getCnt() / (localeSum * 1.00);
                    Double actualRadio = Math.round(radio * 100.0) / 100.0;
                    ShortLinkLocaleStatsRespDTO shortLinkLocaleStatsRespDTO = ShortLinkLocaleStatsRespDTO.builder()
                            .cnt(item.getCnt())
                            .radio(actualRadio)
                            .locale(item.getProvince())
                            .build();
                    localeStats.add(shortLinkLocaleStatsRespDTO);
                }
        );
        List<ShortLinkStatsDO> shortLinkStatsByHour = shortLinkStatsMapper.selectListByHour(start, end, gid, fullShortUrl);
        List<Integer> hourStats = new ArrayList<>();
        for(int i = 0;i < 24;i ++){
            AtomicInteger hour = new AtomicInteger(i);
            shortLinkStatsByHour.stream()
                    .filter(item -> Objects.equals(item.getHour(),hour.get()))
                    .findFirst()
                    .ifPresentOrElse(
                            (item) -> {
                                hourStats.add(item.getPv());
                            },() -> {
                                hourStats.add(0);
                            }
                    );
        }
        //根据指定日期,查询 top5 ip以及点击次数
        List<HashMap<String,Object>> shortLinkAccessLogsByIp = shortLinkAccessLogsMapper.selectTopIpStats(start,end,gid,fullShortUrl);
        List<ShortLinkIpStatsRespDTO> topIpStats = new ArrayList<>();
        shortLinkAccessLogsByIp.stream().forEach(item -> {
            ShortLinkIpStatsRespDTO shortLinkIpStatsRespDTO = ShortLinkIpStatsRespDTO.builder()
                    .ip(item.get("ip").toString())
                    .cnt(Integer.parseInt(item.get("count").toString()))
                    .build();
            topIpStats.add(shortLinkIpStatsRespDTO);
        });
        //查询一周的点击
        List<ShortLinkStatsDO> shortLinkStatsByWeekday = shortLinkStatsMapper.selectListByWeekday(start, end, gid, fullShortUrl);
        List<Integer> weekdayStats = new ArrayList<>();
        for(int i = 1;i <= 7;i ++){
            AtomicInteger weekday = new AtomicInteger(i);
            shortLinkStatsByWeekday.stream()
                    .filter(item -> Objects.equals(item.getWeekday(),weekday.get()))
                    .findFirst()
                    .ifPresentOrElse(
                            (item) -> {
                                weekdayStats.add(item.getPv());
                            },() -> {
                                weekdayStats.add(0);
                            }
                    );
        }
        //根据浏览器类型分类查询信息
        List<ShortLinkBrowserStatsDO> shortLinkByBrowser = shortLinkBrowserStatsMapper.selectListByBrowser(start, end, gid, fullShortUrl);
        Integer browserSum = shortLinkByBrowser.stream().mapToInt(ShortLinkBrowserStatsDO::getCnt).sum();
        List<ShortLinkBrowserStatsRespDTO> browserStats = new ArrayList<>();
        shortLinkByBrowser.forEach(
                item -> {
                    double radio = item.getCnt() / (browserSum * 1.00);
                    double actualRadio = Math.round(radio * 100.0) / 100.0;
                    ShortLinkBrowserStatsRespDTO shortLinkBrowserStatsRespDTO = ShortLinkBrowserStatsRespDTO.builder()
                            .cnt(item.getCnt())
                            .browser(item.getBrowser())
                            .radio(actualRadio)
                            .build();
                    browserStats.add(shortLinkBrowserStatsRespDTO);
                }
        );
        //按操作系统分类

        //按新老用户分
        HashMap<String,Object> shortLinkAccessLogsByUvType = shortLinkAccessLogsMapper.selectUvTypeStats(start, end, gid, fullShortUrl);
        List<ShortLinkUvTypeStatsRespDTO> uvTypeStats = new ArrayList<>();
        Integer oldUserCnt = Integer.parseInt(
                Optional.ofNullable(shortLinkAccessLogsByUvType)
                        .map(item -> item.get("old_user"))
                        .map(Object::toString)
                        .orElse("0")
        );
        Integer newUserCnt = Integer.parseInt(
                Optional.ofNullable(shortLinkAccessLogsByUvType)
                        .map(item -> item.get("new_user"))
                        .map(Object::toString)
                        .orElse("0")
        );
        int uvSum = oldUserCnt + newUserCnt;
        double oldRatio = (double) oldUserCnt / uvSum;
        double actualOldRatio = Math.round(oldRatio * 100.0) / 100.0;
        double newRatio = (double) newUserCnt / uvSum;
        double actualNewRatio = Math.round(newRatio * 100.0) / 100.0;

        ShortLinkUvTypeStatsRespDTO newUserStats = ShortLinkUvTypeStatsRespDTO.builder()
                .uvType("newUser")
                .cnt(newUserCnt)
                .radio(actualNewRatio)
                .build();
        uvTypeStats.add(newUserStats);
        ShortLinkUvTypeStatsRespDTO oldUserStats = ShortLinkUvTypeStatsRespDTO.builder()
                .uvType("oldUser")
                .cnt(oldUserCnt)
                .radio(actualOldRatio)
                .build();
        uvTypeStats.add(oldUserStats);
        // 访问网络类型详情
        List<ShortLinkNetworkStatsRespDTO> networkStats = new ArrayList<>();
        List<ShortLinkNetworkStatsDO> listNetworkStatsByShortLink = shortLinkNetworkStatsMapper.selectListByNetwork(start,end,gid,fullShortUrl);
        int networkSum = listNetworkStatsByShortLink.stream()
                .mapToInt(ShortLinkNetworkStatsDO::getCnt)
                .sum();
        listNetworkStatsByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / networkSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkNetworkStatsRespDTO networkRespDTO = ShortLinkNetworkStatsRespDTO.builder()
                    .cnt(each.getCnt())
                    .network(each.getNetwork())
                    .ratio(actualRatio)
                    .build();
            networkStats.add(networkRespDTO);
        });
        //根据设备类型
        List<ShortLinkDeviceStatsRespDTO> deviceStats = new ArrayList<>();
        List<ShortLinkDeviceStatsDO> listDeviceStatsByShortLink = shortLinkDeviceStatsMapper.selectListByDevice(start,end,gid,fullShortUrl);
        int deviceSum = listDeviceStatsByShortLink.stream()
                .mapToInt(ShortLinkDeviceStatsDO::getCnt)
                .sum();
        listDeviceStatsByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkDeviceStatsRespDTO deviceRespDTO = ShortLinkDeviceStatsRespDTO.builder()
                    .cnt(each.getCnt())
                    .device(each.getDevice())
                    .ratio(actualRatio)
                    .build();
            deviceStats.add(deviceRespDTO);
        });

        return ShortLinkStatsRespDTO.builder()
                .pv(totalPv)
                .uv(totalUv)
                .uip(totalUip)
                .daily(daily)
                .localeStats(localeStats)
                .hourStats(hourStats)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .uvTypeStats(uvTypeStats)
                .networkStats(networkStats)
                .deviceStats(deviceStats)
                .build();
        }

    @Override
    public IPage<ShortLinkAccessLogsRespDTO> showOneUrlLogs(ShortLinkAccessLogsReqDTO requestParams) {
        String gid = requestParams.getGid();
        String fullShortUrl = requestParams.getFullShortUrl();
        DateTime start = DateUtil.parse(requestParams.getStartDate());
        DateTime end = DateUtil.parse(requestParams.getEndDate());
        //查询指定日期内的访问日志
        LambdaQueryWrapper<ShortLinkAccessLogsDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkAccessLogsDO.class)
                .eq(ShortLinkAccessLogsDO::getGid, gid)
                .eq(ShortLinkAccessLogsDO::getFullShortUrl, fullShortUrl)
                .eq(ShortLinkAccessLogsDO::getDelFlag, 0)
                .between(ShortLinkAccessLogsDO::getCreateTime, start, end);
        IPage<ShortLinkAccessLogsDO> shortLinkAccessLogsDOPage = shortLinkAccessLogsMapper.selectPage(requestParams, queryWrapper);
        IPage<ShortLinkAccessLogsRespDTO> converted = shortLinkAccessLogsDOPage.convert(item -> BeanUtil.convert(item, ShortLinkAccessLogsRespDTO.class));
        //将指定日志内的访问日志映射为新老访客
        //获取查询到的用户列表
        List<String> userList = converted.getRecords().stream()
                .map(ShortLinkAccessLogsRespDTO::getUser)
                .distinct().toList();
        if(CollUtil.isEmpty(userList)){
            //选中的日期内没有用户访问
            return new Page<>();
        }
        //用户名对应新老访客类型
        /*
         这里需要传之前查到的 userList,如果不传的话结果会多(有的用户没有在指定日期内访问,但是查询是也会判断是新/老访客)
         */
        List<HashMap<String, Object>> uvTypeByUser = shortLinkAccessLogsMapper.selectUvTypeByUser(start, end, gid, fullShortUrl,userList);
        converted.getRecords().stream().forEach(item -> {
            Object uvType = uvTypeByUser.stream().filter(each -> Objects.equals(each.get("user"), item.getUser()))
                    .map(each -> each.get("uvType"))
                    .findFirst()
                    .orElseThrow(() -> new ClientException("用户传参有误"));
            item.setUvType(uvType.toString());
        });
        return converted;
    }
}
