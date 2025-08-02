package com.nageoffer.shortlink.admin.remote.req;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ShortLinkBatchCreateReqDTO {

    /**
     * 域名这里使用默认
     */

    private String domain;

    /**
     * 原始链接
     */

    private List<String> originUrl;

    /**
     * 分组标识
     */

    private String gid;


    /**
     * 创建类型 0：控制台 1：接口
     */

    private Integer createdType;


    /**
     * 有效期类型 0：永久有效 1：用户自定义
     */

    private Integer validDateType;


    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validDate;


    /**
     * 描述
     */

    private List<String> describe;

    private String favicon;

}
