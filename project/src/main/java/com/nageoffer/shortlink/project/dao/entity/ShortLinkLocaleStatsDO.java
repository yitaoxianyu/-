package com.nageoffer.shortlink.project.dao.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_link_locale_stats")
public class ShortLinkLocaleStatsDO extends DatabaseDO implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     *  ID
     */
    private Long id;

    /**
     *  完整短链接
     */
    private String fullShortUrl;

    /**
     *  分组标识
     */
    private String gid;

    /**
     *  日期
     */
    private Date date;

    /**
     *  访问量
     */
    private Integer cnt;

    /**
     *  省份名称
     */
    private String province;

    /**
     *  市名称
     */
    private String city;

    /**
     *  城市编码
     */
    private String adcode;

    /**
     *  国家标识
     */
    private String country;


}
