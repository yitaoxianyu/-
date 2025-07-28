package com.nageoffer.shortlink.project.dao.mapper;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkBrowserStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShortLinkBrowserStatsMapper extends BaseMapper<ShortLinkBrowserStatsDO> {
    int insertStatsOrUpdate(ShortLinkBrowserStatsDO shortLinkBrowserStatsDO);

    List<ShortLinkBrowserStatsDO> selectListByBrowser(
            @Param("start") DateTime start, @Param("end")DateTime end, @Param("gid")String gid, @Param("fullShortUrl") String fullShortUrl
    );

}
