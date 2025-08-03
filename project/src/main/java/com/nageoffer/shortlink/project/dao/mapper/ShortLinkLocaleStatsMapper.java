package com.nageoffer.shortlink.project.dao.mapper;


import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkLocaleStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShortLinkLocaleStatsMapper extends BaseMapper<ShortLinkLocaleStatsDO> {

    int insertStatsOrUpdate(ShortLinkLocaleStatsDO shortLinkLocaleStats);


    List<ShortLinkLocaleStatsDO> selectListByProvince(
            @Param("start") DateTime start, @Param("end")DateTime end, @Param("gid")String gid, @Param("fullShortUrl") String fullShortUrl
    );
}
