package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkBrowserStatsDO;

public interface ShortLinkBrowserStatsMapper extends BaseMapper<ShortLinkBrowserStatsDO> {
    int insertStatsOrUpdate(ShortLinkBrowserStatsDO shortLinkBrowserStatsDO);
}
