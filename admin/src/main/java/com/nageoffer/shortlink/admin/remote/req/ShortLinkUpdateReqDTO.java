package com.nageoffer.shortlink.admin.remote.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ShortLinkUpdateReqDTO {
    private String fullShortUrl;
    /*
    描述
     */
    private String describe;
    /**
     * 原来的分组
     */
    private String originalGid;

    private String targetGid;

    /**
     * 原始链接
     */
    private String originUrl;
    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validDate;
    /**
     * 有效期类型，0：永久有效 1：自定义
     */
    private Integer validDateType;
}