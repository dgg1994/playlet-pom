package com.playlet.internal.constants;

/**
 * 类描述：redis key 常量类
 *
 * @author GeminiSun
 * @date 2026/07/16 10:20
 */
public class RedisKeyConstants {

    /** 项目隔离前缀（国内 / 海外共用 Redis 时区分） */
    public static final String PROJECT_PREFIX = "internal:";

    public static final int KEYWORD_MAX_LEN = 50;
    public static final int HISTORY_MAX = 20;
    /** 搜索历史 TTL：90 天 */
    public static final long HISTORY_TTL_SEC = 90L * 24 * 60 * 60;
    public static final String HISTORY_KEY_UID = PROJECT_PREFIX + "theater:search:hist:uid:";

    /** 浏览历史缓存条数上限 */
    public static final int VIEW_HISTORY_MAX = 100;

    /** 浏览历史 Redis TTL：7 天（MySQL 为权威数据） */
    public static final long VIEW_HISTORY_TTL_SEC = 7L * 24 * 60 * 60;
    public static final String VIEW_LIST_KEY = PROJECT_PREFIX + "theater:view:list:uid:";
    public static final String VIEW_META_KEY = PROJECT_PREFIX + "theater:view:meta:uid:";
    public static final String VIEW_EMPTY_KEY = PROJECT_PREFIX + "theater:view:empty:uid:";

    /** 用户收藏 Set：member=dramaId */
    public static final String COLLECT_SET_UID = PROJECT_PREFIX + "theater:collect:uid:";
    /** 用户整剧点赞 Set：member=dramaId */
    public static final String LIKE_DRAMA_SET_UID = PROJECT_PREFIX + "theater:like:drama:uid:";
    /** 用户单集点赞 Set：member=dramaId:episodeId */
    public static final String LIKE_EP_SET_UID = PROJECT_PREFIX + "theater:like:ep:uid:";
    /** 互动状态缓存 TTL：7 天 */
    public static final long INTERACT_TTL_SEC = 7L * 24 * 60 * 60;

    /** 分享计数冷却：同一用户同一剧 */
    public static final String SHARE_CD_UID_DRAMA = PROJECT_PREFIX + "theater:share:cd:uid:";
    public static final long SHARE_CD_SEC = 30L;

    /** 提现提交防重：后缀 userType:uid */
    public static final String WITHDRAW_SUBMIT_LOCK = PROJECT_PREFIX + "wallet:withdraw:lock:";
    public static final long WITHDRAW_SUBMIT_LOCK_SEC = 5L;

    /** 七牛对象存在性缓存：key 后缀为对象 key */
    public static final String QINIU_EXISTS_KEY = PROJECT_PREFIX + "qiniu:exists:";
    /** 存在性缓存 TTL：10 分钟（码率探测热路径） */
    public static final long QINIU_EXISTS_TTL_SEC = 10L * 60;

    /** 剧场首页整页缓存：后缀为 langue */
    public static final String THEATER_HOME_KEY = PROJECT_PREFIX + "theater:home:v1:";
    /** 首页缓存 TTL：60 秒（主动失效为主，TTL 兜底） */
    public static final long THEATER_HOME_TTL_SEC = 60L;

    /** 邮箱验证码：后缀为 email */
    public static final String EMAIL_CODE_KEY = PROJECT_PREFIX + "email:code:";
    /** 作家端邮箱验证码：后缀为登录邮箱 */
    public static final String CREATOR_EMAIL_CODE_KEY = PROJECT_PREFIX + "creator:email:code:";
    /** 接口幂等：后缀为 requestId */
    public static final String IDEMPOTENT_KEY = PROJECT_PREFIX + "idempotent:";
    /** 访问限流：后缀为 ip+url */
    public static final String ACCESS_LIMIT_KEY = PROJECT_PREFIX + "req_limit:";
    /** 后台登录谷歌验证码暂存：后缀为 username */
    public static final String GOOGLE_CODE_KEY = PROJECT_PREFIX + "googleCode:";

    /** 剧集曝光去重：后缀 uid:assetId */
    public static final String PLAY_EXPOSE_DEDUP = PROJECT_PREFIX + "theater:play:expose:";
    /** 剧集完播去重：后缀 uid:assetId */
    public static final String PLAY_COMPLETE_DEDUP = PROJECT_PREFIX + "theater:play:complete:";
}
