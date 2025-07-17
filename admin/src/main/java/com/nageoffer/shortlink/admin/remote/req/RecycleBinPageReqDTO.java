package com.nageoffer.shortlink.admin.remote.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.remote.entity.ShortLinkDO;
import lombok.Data;

import java.util.List;

@Data
public class RecycleBinPageReqDTO extends Page<ShortLinkDO> {

    /*
    根据用户名传过来的 gids
     */
    List<String> gids;
}
