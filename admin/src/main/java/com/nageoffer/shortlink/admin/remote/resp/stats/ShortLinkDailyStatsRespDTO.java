package com.nageoffer.shortlink.admin.remote.resp.stats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkDailyStatsRespDTO {

    /**
     *  日期,这里默认是 utc,我们要指定东八区
     */
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "Asia/Shanghai")
    private Date date;

    /**
     *  访问量
     */
    private Integer pv;

    /**
     *  独立访问数
     */
    private Integer uv;

    /**
     *  独立IP数
     */
    private Integer uip;
}
