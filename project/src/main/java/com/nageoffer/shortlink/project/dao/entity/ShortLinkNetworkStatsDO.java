package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("t_link_network_stats")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkNetworkStatsDO extends DatabaseDO implements Serializable {


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
     *  访问网络
     */
    private String network;


}
