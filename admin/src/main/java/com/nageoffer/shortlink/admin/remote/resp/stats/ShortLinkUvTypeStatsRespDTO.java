package com.nageoffer.shortlink.admin.remote.resp.stats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortLinkUvTypeStatsRespDTO {

    private Integer cnt;

    private String uvType;

    private Double radio;

}
