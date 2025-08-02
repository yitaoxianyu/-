package com.nageoffer.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ShortLinkBatchCreateRespDTO {

    private String gid;

    private Integer total;

    private List<ShortLinkInfo> shortLinkInfoList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShortLinkInfo {
        private String describe;

        private String originUrl;

        private String fullShortUrl;

    }

}


