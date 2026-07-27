package com.playlet.internal.service;

import com.playlet.internal.enums.WelfareActionTypeEnums;

/**
 * 勋章进度推进（内部调用，非 HTTP）
 */
public interface MedalProgressService {

	/**
	 * 按行为推进匹配勋章进度；达标则解锁并写流水。
	 * 失败仅记日志，不抛给主业务。
	 *
	 * @param uid        用户ID
	 * @param action     行为类型（复用 WelfareActionTypeEnums）
	 * @param delta      增加进度，通常为 1
	 * @param triggerRef 业务引用，如 dramaId / commentId；WATCH 建议 dramaId:episodeId:yyyy-MM-dd
	 */
	void onAction(Integer uid, WelfareActionTypeEnums action, int delta, String triggerRef);
}
