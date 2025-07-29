package com.nageoffer.shortlink.project.dto.resp.logs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkAccessLogsRespDTO {

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
    /**
     *  地区:xx-xx-xx
     */
    private String locale;
    /**
     * 设备
     */
    private String device;
    /**
     *  网络类型
     */
    private String network;
    /**
     *  用户类型
     */
    private String uvType;
}
