package com.nageoffer.shortlink.project.dao.mapper;

import cn.hutool.core.date.DateTime;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkNetworkStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShortLinkNetworkStatsMapper {


    int insertStatsOrUpdate(ShortLinkNetworkStatsDO shortLinkNetworkStatsDO);

    List<ShortLinkNetworkStatsDO> selectListByNetwork
            (
                    @Param("start") DateTime start, @Param("end")DateTime end, @Param("gid")String gid, @Param("fullShortUrl") String fullShortUrl
    );
}
