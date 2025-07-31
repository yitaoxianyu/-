package com.nageoffer.shortlink.admin.remote.resp.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkStatsRespDTO {

    private Integer pv;

    private Integer uv;

    private Integer uip;

    private List<ShortLinkDailyStatsRespDTO> daily;

    private List<ShortLinkLocaleStatsRespDTO> localeStats;

    private List<Integer> hourStats;

    private List<ShortLinkIpStatsRespDTO> topIpStats;

    private List<Integer> weekdayStats;

    private List<ShortLinkBrowserStatsRespDTO> browserStats;

    private List<ShortLinkUvTypeStatsRespDTO> uvTypeStats;

    private List<ShortLinkNetworkStatsRespDTO> networkStats;

    private List<ShortLinkDeviceStatsRespDTO> deviceStats;

}
