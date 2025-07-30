package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;

public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {
    IPage<ShortLinkDO> pageQuery(ShortLinkPageReqDTO requestParams);
}
