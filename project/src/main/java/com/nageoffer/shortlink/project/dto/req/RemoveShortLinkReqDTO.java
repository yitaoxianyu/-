package com.nageoffer.shortlink.project.dto.req;

import lombok.Data;

@Data
public class RemoveShortLinkReqDTO {

    private String gid;

    private String fullShortUrl;
}
