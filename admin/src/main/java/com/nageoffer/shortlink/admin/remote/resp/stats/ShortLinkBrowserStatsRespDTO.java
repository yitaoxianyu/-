package com.nageoffer.shortlink.admin.remote.resp.stats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortLinkBrowserStatsRespDTO {

    private String browser;

    private Integer cnt;

    private Double radio;

}
