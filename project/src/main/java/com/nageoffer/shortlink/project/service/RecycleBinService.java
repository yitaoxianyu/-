package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.req.RecoverShortLinkReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.RemoveShortLinkReqDTO;
import com.nageoffer.shortlink.project.dto.req.SaveShortLinkReqDTO;
import com.nageoffer.shortlink.project.dto.resp.RecycleBinPageRespDTO;

public interface RecycleBinService extends IService<ShortLinkDO> {
    void saveLink2RecycleBin(SaveShortLinkReqDTO saveShortLinkReqDTO);

    IPage<RecycleBinPageRespDTO> pageQueryShortLink(RecycleBinPageReqDTO recycleBinPageReqDTO);

    void recoverShortLink(RecoverShortLinkReqDTO requestParams);

    void removeShortLink(RemoveShortLinkReqDTO requestParams);
}
