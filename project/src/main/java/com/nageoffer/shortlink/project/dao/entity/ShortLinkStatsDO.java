package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 *  
 *
 * @author 
 * @since 2025-07-19 19:47:09
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_link_access_stats")
public class ShortLinkStatsDO extends DatabaseDO implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  ID
     */
    private Long id;

    /**
     *  分组标识
     */
    private String gid;

    /**
     *  完整短链接
     */
    private String fullShortUrl;

    /**
     *  日期
     */
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

    /**
     *  小时
     */
    private Integer hour;

    /**
     *  星期
     */
    private Integer weekday;

}