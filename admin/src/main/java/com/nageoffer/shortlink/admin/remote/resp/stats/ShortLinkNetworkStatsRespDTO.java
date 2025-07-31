package com.nageoffer.shortlink.admin.remote.resp.stats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortLinkNetworkStatsRespDTO {

    private Double ratio;

    private String network;

    private Integer cnt;

}
