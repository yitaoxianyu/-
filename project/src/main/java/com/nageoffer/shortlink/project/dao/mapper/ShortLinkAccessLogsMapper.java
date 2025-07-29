package com.nageoffer.shortlink.project.dao.mapper;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkAccessLogsDO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

public interface ShortLinkAccessLogsMapper extends BaseMapper<ShortLinkAccessLogsDO> {

    List<HashMap<String, Object>> selectTopIpStats(
            @Param("start") DateTime start, @Param("end")DateTime end, @Param("gid")String gid, @Param("fullShortUrl") String fullShortUrl
    );
    //根据传来的日期范围给出一个是新访客/老访客的list
    HashMap<String,Object> selectUvTypeStats(
            @Param("start") DateTime start, @Param("end")DateTime end, @Param("gid")String gid, @Param("fullShortUrl") String fullShortUrl
    );

    List<HashMap<String,Object>> selectUvTypeByUser(
            @Param("start") DateTime start, @Param("end")DateTime end, @Param("gid")String gid, @Param("fullShortUrl") String fullShortUrl,
            @Param("userList") List<String> userList
    );
}
