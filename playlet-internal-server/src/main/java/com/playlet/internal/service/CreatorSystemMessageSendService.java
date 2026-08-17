package com.playlet.internal.service;

/**
 * 作家站内信写入（评审等业务直发）。
 */
public interface CreatorSystemMessageSendService {

	/**
	 * 一对一写入作家收件箱；uk(to_creator_id, biz_id) 幂等。
	 *
	 * @return true=新写入；false=已存在或参数非法
	 */
	boolean sendToCreator(Integer toCreatorId, String messageType, String langue,
			String title, String content, String coverUrl, Integer dramaId, Integer assetId,
			String bizId, String jumpType, String jumpParam);
}
