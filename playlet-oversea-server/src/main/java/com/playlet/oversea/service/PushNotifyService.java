package com.playlet.oversea.service;

import java.util.List;
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
	 * 向所有「已开启推送」用户推同一套文案（单语言）
	 */
	void notifyAll(String title, String content, Map<String, Object> extras);

	/**
	 * 向指定 registrationId 列表推送（已按语言分好组时用）
	 */
	void notifyDevices(List<String> registrationIds, String title, String content,
			Map<String, Object> extras);

	/**
	 * 互动消息推送（文案按接收人 push_langue + PushTemplateEnums）
	 */
	void notifyInteract(Integer toUid, Integer fromUid, String messageType,
			Long messageId, Integer dramaId, String episodeId);

	/**
	 * 勋章解锁推送（文案按接收人 push_langue + PushTemplateEnums）
	 */
	void notifyMedalUnlock(Integer toUid, Integer medalId, String medalName);
}
