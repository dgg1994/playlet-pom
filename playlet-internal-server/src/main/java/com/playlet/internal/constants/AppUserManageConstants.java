package com.playlet.internal.constants;

/**
 * 管理端 App 用户管理常量。
 */
public final class AppUserManageConstants {

	/** 会员状态筛选：普通（无会员记录） */
	public static final int MEMBER_FILTER_NORMAL = 0;
	/** 会员状态筛选：有效 */
	public static final int MEMBER_FILTER_ACTIVE = 1;
	/** 会员状态筛选：已过期 */
	public static final int MEMBER_FILTER_EXPIRED = 2;

	/** 列表展示：普通 */
	public static final String MEMBER_LABEL_NORMAL = "普通";
	/** 列表展示：有效 */
	public static final String MEMBER_LABEL_ACTIVE = "有效";
	/** 列表展示：已过期 */
	public static final String MEMBER_LABEL_EXPIRED = "已过期";

	/** 状态流水：系统操作人 */
	public static final String OPERATOR_SYSTEM = "系统";
	/** 状态流水：管理员默认名 */
	public static final String OPERATOR_ADMIN = "管理员";

	/** 站内通知 messageType */
	public static final String MSG_TYPE_ADMIN_NOTIFY = "ADMIN_NOTIFY";

	/** 导出单次上限 */
	public static final int EXPORT_MAX_ROWS = 5000;

	/** 重置密码默认长度 */
	public static final int RESET_PWD_LEN = 8;

	private AppUserManageConstants() {
	}
}
