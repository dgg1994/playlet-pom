package com.playlet.oversea.security.sensitive;

import com.playlet.oversea.base.SensitiveCheckResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 敏感词检测后的业务决策。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveDecision {

	private SensitiveAction action;

	private SensitiveCheckResult check;

	/** 最终应写入的文案（可能已打码） */
	private String content;

	public boolean shouldRecord() {
		return check != null && Boolean.FALSE.equals(check.getPass());
	}

	public boolean isReject() {
		return action == SensitiveAction.REJECT;
	}

	public boolean isHidden() {
		return action == SensitiveAction.HIDE;
	}
}
