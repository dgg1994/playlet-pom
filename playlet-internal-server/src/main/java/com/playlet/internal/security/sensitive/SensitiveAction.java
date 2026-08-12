package com.playlet.internal.security.sensitive;

/**
 * 敏感词处置动作。
 * <ul>
 *   <li>PASS — 通过</li>
 *   <li>MASK — 警告级：打码后正常发布</li>
 *   <li>HIDE — 审核级：入库但隐藏（delete_state=删除），待人工处理</li>
 *   <li>REJECT — 禁止级：拒绝发布</li>
 * </ul>
 */
public enum SensitiveAction {
	PASS,
	MASK,
	HIDE,
	REJECT
}
