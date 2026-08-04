package com.playlet.internal.service;

/**
 * 系统消息写入（业务直发 / 指定投递复用）
 */
public interface SystemMessageSendService {

/**
 * 业务一对一写入收件箱（幂等 bizId）；落库成功后会极光推送。
 *
 * @param push 历史兼容参数；当前实现落库成功后一律推送
 * @return true=新写入；false=已存在或参数非法
 */
boolean sendToUser(Integer toUid, String messageType, String langue,
		String title, String content, String coverUrl, Integer dramaId,
		String bizId, String jumpType, String jumpParam, boolean push);
}
