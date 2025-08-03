package com.nageoffer.shortlink.project.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkOsStatsDO;

public interface ShortLinkOsStatsMapper extends BaseMapper<ShortLinkOsStatsDO> {

    int insertStatsOrUpdate(ShortLinkOsStatsDO shortLinkOsStatsDO);
}
