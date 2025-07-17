package com.nageoffer.shortlink.admin.remote.req;

import lombok.Data;

@Data
public class SaveShortLinkReqDTO {

    private String gid;

    private String fullShortUrl;

}
