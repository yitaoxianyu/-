package com.nageoffer.shortlink.project.dao.mapper;


import cn.hutool.core.date.DateTime;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDeviceStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShortLinkDeviceStatsMapper {


    int insertStatsOrUpdate(ShortLinkDeviceStatsDO shortLinkDeviceStatsDO);


    List<ShortLinkDeviceStatsDO> selectListByDevice(
            @Param("start") DateTime start, @Param("end") DateTime end, @Param("gid") String gid, @Param("fullShortUrl") String fullShortUrl
    );
}
