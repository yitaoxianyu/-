package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkQueryCountDTO;

import java.util.List;

public interface ShortLinkService {
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParams);

    IPage<ShortLinkPageRespDTO> pageQuery(ShortLinkPageReqDTO requestParams);

    List<ShortLinkQueryCountDTO> queryShortLinkCount(List<String> requestParams);
}
