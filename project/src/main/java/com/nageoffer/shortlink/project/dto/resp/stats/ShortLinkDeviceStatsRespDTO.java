package com.nageoffer.shortlink.project.dto.resp.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkDeviceStatsRespDTO {

    private String device;

    private Integer cnt;

    private Double ratio;
}
