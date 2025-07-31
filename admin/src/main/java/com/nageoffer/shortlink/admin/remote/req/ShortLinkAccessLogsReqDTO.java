package com.nageoffer.shortlink.admin.remote.req;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.remote.entity.ShortLinkAccessLogsDO;
import lombok.Data;

@Data
public class ShortLinkAccessLogsReqDTO extends Page<ShortLinkAccessLogsDO> {

    private String fullShortUrl;

    private String gid;

    private String startDate;

    private String endDate;


}
