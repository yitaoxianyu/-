package com.nageoffer.shortlink.admin.remote.resp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortLinkCreateRespDTO {

    private String gid;

    private String originUrl;

    private String fullShortUrl;

}
