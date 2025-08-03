package com.nageoffer.shortlink.project.dao.mapper;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShortLinkStatsMapper extends BaseMapper<ShortLinkStatsDO> {


    int insertStatsOrUpdate(ShortLinkStatsDO shortLinkStatsDO);


    List<ShortLinkStatsDO> selectListByDate(
            @Param("start")DateTime start, @Param("end")DateTime end,@Param("gid")String gid,@Param("fullShortUrl") String fullShortUrl
    );

    List<ShortLinkStatsDO> selectListByHour(
            @Param("start")DateTime start, @Param("end")DateTime end,@Param("gid")String gid,@Param("fullShortUrl") String fullShortUrl
    );

    List<ShortLinkStatsDO> selectListByWeekday(
            @Param("start")DateTime start, @Param("end")DateTime end,@Param("gid")String gid,@Param("fullShortUrl") String fullShortUrl
    );
}
