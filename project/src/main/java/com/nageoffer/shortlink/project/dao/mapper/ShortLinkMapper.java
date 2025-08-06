package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.apache.ibatis.annotations.Param;

public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {
    IPage<ShortLinkDO> pageQuery(ShortLinkPageReqDTO requestParams);

    void recordUvPvUip(@Param(value = "fullShortUrl") String fullShortUrl, @Param(value = "gid")String gid,@Param(value = "pv")int pv,@Param(value = "uv")int uv,@Param(value = "uip") int uip);
}
