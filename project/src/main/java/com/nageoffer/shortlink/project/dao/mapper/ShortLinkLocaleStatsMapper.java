package com.nageoffer.shortlink.project.dao.mapper;


import cn.hutool.core.date.DateTime;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkLocaleStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShortLinkLocaleStatsMapper {

    int insertStatsOrUpdate(ShortLinkLocaleStatsDO shortLinkLocaleStats);


    List<ShortLinkLocaleStatsDO> selectListByProvince(
            @Param("start") DateTime start, @Param("end")DateTime end, @Param("gid")String gid, @Param("fullShortUrl") String fullShortUrl
    );
}
