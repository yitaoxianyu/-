package com.nageoffer.shortlink.project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static com.nageoffer.shortlink.project.common.constants.ShortLinkConstants.DEFAULT_CACHE_VALID_TIME;

/**
 * 根据日期计算出时间
 */
public class LinkUtil {

    public static long getLinkCacheValidTime(Date validDate) {
        return Optional.ofNullable(validDate)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.SECOND))
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }
}
