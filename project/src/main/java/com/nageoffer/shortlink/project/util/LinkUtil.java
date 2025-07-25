package com.nageoffer.shortlink.project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;

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

    public static String getIp(HttpServletRequest request){
       return request.getRemoteAddr();
    }
    //todo 使用策略模式优化 if-else嵌套
    public static String getOs(HttpServletRequest request){
        String agent = request.getHeader("User-Agent");

        if(agent == null || agent.isEmpty()) {
            return "未知";
        }
        // 解析操作系统信息
        if (agent.contains("Windows")) {
            return "Windows";
        } else if (agent.contains("Mac OS")) {
            return "Mac OS";
        } else if (agent.contains("Linux")) {
            return "Linux";
        } else if (agent.contains("Android")) {
            return "Android";
        } else if (agent.contains("iOS")) {
            return "iOS";
        }

        return "未知";
    }

    public static String getBrowser(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent");

        if(agent == null || agent.isEmpty()) {
            return "未知";
        }
        // 解析浏览器信息
        if (agent.contains("Chrome")) {
            return "Chrome";
        } else if (agent.contains("Firefox")) {
            return "Firefox";
        } else if (agent.contains("Safari")) {
            return "Safari";
        } else if (agent.contains("MSIE") || agent.contains("Trident")) {
            return ("Internet Explorer");
        } else if (agent.contains("Edge")) {
            return "Edge";
        }

        return "未知";
    }
}
