package com.nageoffer.shortlink.admin.remote.resp;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
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


        /**
         * 描述信息
         */
        @ExcelProperty("标题")
        @ColumnWidth(40)
        private String describe;

        /**
         * 短链接
         */
        @ExcelProperty("短链接")
        @ColumnWidth(40)
        private String fullShortUrl;

        /**
         * 原始链接
         */
        @ExcelProperty("原始链接")
        @ColumnWidth(80)
        private String originUrl;

    }

}


