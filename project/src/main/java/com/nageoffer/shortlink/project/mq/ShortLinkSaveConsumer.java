package com.nageoffer.shortlink.project.mq;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.resp.stats.ShortLinkStatsRecordDTO;
import com.nageoffer.shortlink.project.util.IdempotentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RocketMQMessageListener(topic = "${rocketmq.producer.topic}",consumerGroup = "${rocketmq.consumer.group}")
@RequiredArgsConstructor
@Component
@Slf4j
public class ShortLinkSaveConsumer implements RocketMQListener<Map<String,String>> {

    @Value("${short-link.api.locale-stats.key}")
    private String localeStatsKey;

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

    private final IdempotentUtil idempotentUtil;


    @Override
    public void onMessage(Map<String, String> map) {
        String keys = map.get("keys");
        if(idempotentUtil.isProcessed(keys)){
            if(idempotentUtil.isAccomplish(keys)) return ;
            throw new ServiceException("消息消费失败,需要重新投递");
        }
        String gid = map.get("gid");
        String fullShortUrl = map.get("fullShortUrl");
        ShortLinkStatsRecordDTO shortLinkStatsRecordDTO = JSONObject.parseObject(map.get("shortLinkStatsRecordDTO"), new TypeReference<>() {
        });
        try { //填充 fullShortUrl
            if (StrUtil.isBlank(fullShortUrl)) {
                fullShortUrl = shortLinkStatsRecordDTO.getFullShortUrl();
            }
            String originIp = shortLinkStatsRecordDTO.getRemoteAddr();
            //填充 gid 字段
            if (StrUtil.isBlank(gid)) {
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
            shortLinkMapper.recordUvPvUip(fullShortUrl, gid, 1, uvFlag ? 1 : 0, ipFlag ? 1 : 0);
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
            idempotentUtil.del(keys);
            log.error("消息处理失败",e);
            throw e;
        }
        idempotentUtil.setAccomplish(keys);
    }
}
