package com.nageoffer.shortlink.admin.remote.req;


import lombok.Data;

@Data
public class ShortLinkStatsReqDTO {

    private String fullShortUrl;

    private String gid;

    private String startDate;

    private String endDate;


}
