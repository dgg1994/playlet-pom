package com.playlet.internal.constants;

/**
 * 作家端账号常量。
 */
public final class CreatorConstants {

	private CreatorConstants() {
	}

	/** 历史 JWT subject 前缀（新签发已改为纯邮箱，解析时仍剥离） */
	public static final String JWT_SUBJECT_PREFIX = "creator:";

	/** 展示昵称最大字数（原型：不超过九个字；注册自动生成不受此限） */
	public static final int NICKNAME_MAX_LEN = 9;

	/** 注册默认昵称前缀，与 C 端 AppUserServiceImpl 一致 */
	public static final String NICKNAME_AUTO_PREFIX = "creator_";

	/** 注册默认昵称时间戳格式 */
	public static final String NICKNAME_AUTO_TIME_PATTERN = "yyMMddHHmmss";

	/** 注册默认昵称随机数区间（含 origin，不含 bound，同 ThreadLocalRandom.nextInt） */
	public static final int NICKNAME_AUTO_RANDOM_ORIGIN = 100;
	public static final int NICKNAME_AUTO_RANDOM_BOUND = 999;

	/** 登录邮箱格式 */
	public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

	/** 首页展示：金币→元，100 金币 = 1.00 元 */
	public static final int COIN_PER_YUAN = 100;

	/** 首页热点剧条数 */
	public static final int HOME_HOT_DRAMA_LIMIT = 10;

	/** 首页热点题材条数 */
	public static final int HOME_HOT_TAG_LIMIT = 20;

	/** 首页公告摘要条数 */
	public static final int HOME_NOTICE_LIMIT = 8;

	/** 首页榜单条数 */
	public static final int HOME_RANK_LIMIT = 20;

	/** 影响力：近 N 日有效观看秒 */
	public static final int INFLUENCE_WINDOW_DAYS = 7;

	/** 成长力：近 N 日 vs 前 N 日 */
	public static final int GROWTH_WINDOW_DAYS = 3;

	/** 成长力近窗有效秒下限，过滤噪声 */
	public static final int GROWTH_MIN_RECENT_SECONDS = 60;

	/** 热点题材：近窗内出现次数 ≥ 此值打火标 */
	public static final int HOT_TAG_FIRE_MIN_CNT = 3;

	/** 作家身份回复落库的 user_id 占位（真实身份看 from_creator_id） */
	public static final int COMMENT_CREATOR_USER_ID_PLACEHOLDER = 0;

	/** 置顶：是 */
	public static final int COMMENT_PIN_ON = 1;

	/** 置顶：否 */
	public static final int COMMENT_PIN_OFF = 0;

	/** 每部剧同时仅允许一条置顶评论 */
	public static final int COMMENT_PIN_MAX_PER_DRAMA = 1;

	/** 站内信有效 */
	public static final int MSG_STATUS_VALID = 1;

	/** 站内信未读 */
	public static final int MSG_UNREAD = 0;

	/** 站内信已读 */
	public static final int MSG_READ = 1;

	/** 列表来源：个人收件箱 */
	public static final String MSG_SOURCE_INBOX = "INBOX";

	/** 列表来源：站务广播 */
	public static final String MSG_SOURCE_BROADCAST = "BROADCAST";

	/** 跳转：剧详情 */
	public static final String MSG_JUMP_DRAMA = "drama";

	/** 跳转：集详情 */
	public static final String MSG_JUMP_ASSET = "asset";

	/** 评审驳回幂等前缀（后接 drama:{id} 或 asset:{id}） */
	public static final String MSG_BIZ_AUDIT_REJECT_DRAMA = "audit_reject:drama:";

	/** 评审驳回幂等前缀（集） */
	public static final String MSG_BIZ_AUDIT_REJECT_ASSET = "audit_reject:asset:";

	/** 幂等键步骤后缀，避免申诉后再驳回被去重 */
	public static final String MSG_BIZ_STEP = ":step:";

	/** 站内信正文最大长度（对齐表字段） */
	public static final int MSG_CONTENT_MAX_LEN = 2048;
}
