package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@TableName("t_link_access_logs")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortLinkAccessLogsDO extends DatabaseDO implements Serializable {

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
     *  用户信息
     */
    private String user;

    /**
     *  浏览器
     */
    private String browser;

    /**
     *  操作系统
     */
    private String os;

    /**
     *  IP
     */
    private String ip;

}
