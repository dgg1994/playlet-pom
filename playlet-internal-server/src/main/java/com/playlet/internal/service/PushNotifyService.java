package com.playlet.internal.service;

import java.util.Map;

/**
 * App 消息推送（极光）
 */
public interface PushNotifyService {

	/**
	 * 按用户 uid 异步推送（查 registration_id）
	 *
	 * @param toUid   接收人
	 * @param title   标题
	 * @param content 正文
	 * @param extras  附加字段（客户端跳转）
	 */
	void notifyUser(Integer toUid, String title, String content, Map<String, Object> extras);

	/**
	 * 全员广播推送（极光 audience=all）
	 */
	void notifyAll(String title, String content, Map<String, Object> extras);

	/**
	 * 互动消息推送
	 */
	void notifyInteract(Integer toUid, Integer fromUid, String messageType,
			Long messageId, Integer dramaId, String episodeId);

	/**
	 * 勋章解锁推送
	 */
	void notifyMedalUnlock(Integer toUid, Integer medalId, String medalName);
}
