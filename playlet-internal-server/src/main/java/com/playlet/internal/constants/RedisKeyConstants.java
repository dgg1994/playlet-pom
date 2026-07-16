package com.playlet.internal.constants;

/**
 * 类描述：redis key 常量类
 *
 * @author GeminiSun
 * @date 2026/07/16 10:20
 */
public class RedisKeyConstants {

    public static final int KEYWORD_MAX_LEN = 50;
    public static final int HISTORY_MAX = 20;
    /** 搜索历史 TTL：90 天 */
    public static final long HISTORY_TTL_SEC = 90L * 24 * 60 * 60;
    public static final String HISTORY_KEY_UID = "theater:search:hist:uid:";

    /** 浏览历史缓存条数上限 */
    public static final int VIEW_HISTORY_MAX = 100;

    /** 浏览历史 Redis TTL：7 天（MySQL 为权威数据） */
    public static final long VIEW_HISTORY_TTL_SEC = 7L * 24 * 60 * 60;
    public static final String VIEW_LIST_KEY = "theater:view:list:uid:";
    public static final String VIEW_META_KEY = "theater:view:meta:uid:";
    public static final String VIEW_EMPTY_KEY = "theater:view:empty:uid:";
}