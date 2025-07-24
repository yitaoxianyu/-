package com.nageoffer.shortlink.project.common.constants;

public class ShortLinkConstants {

    /*
    %s为 fullShortUrl
     */
    public static final String SHORT_LINK_GOTO_KEY = "short-link:goto:%s";

    //用来更新上面 key 缓存的锁的键
    public static final String LOCK_SHORT_LINK_GOTO_KEY = "short-link:goto:lock:%s";

    //单位是秒
    public static final long DEFAULT_CACHE_VALID_TIME = 2628000;

    public static final String SHORT_LINK_UV_KEY = "short-link:stats:uv:";
    //设置一个cookie 有效期为一个月
    public static final long DEFAULT_UV_VALID_TIME = 60 * 60 * 24 * 30;

    public static final String SHORT_LINK_IP_KEY = "short-link:stats:ip";

}
