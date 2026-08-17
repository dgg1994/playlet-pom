package com.playlet.internal.service.impl;

import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.dao.message.CreatorSystemMessageDao;
import com.playlet.internal.entity.message.CreatorSystemMessageEntity;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.CreatorSystemMessageSendService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 作家站内信写入。
 */
@Slf4j
@Service
public class CreatorSystemMessageSendServiceImpl implements CreatorSystemMessageSendService {

	@Autowired
	private CreatorSystemMessageDao creatorSystemMessageDao;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean sendToCreator(Integer toCreatorId, String messageType, String langue,
			String title, String content, String coverUrl, Integer dramaId, Integer assetId,
			String bizId, String jumpType, String jumpParam) {
		if (toCreatorId == null || StringUtils.isEmpty(bizId) || StringUtils.isEmpty(title)
				|| StringUtils.isEmpty(content) || StringUtils.isEmpty(messageType)) {
			return false;
		}
		if (creatorSystemMessageDao.findByBiz(toCreatorId, bizId) != null) {
			return false;
		}
		CreatorSystemMessageEntity row = new CreatorSystemMessageEntity();
		row.setToCreatorId(toCreatorId);
		row.setPublishId(null);
		row.setMessageType(messageType);
		row.setLangue(StringUtils.isEmpty(langue) ? LanguageEnums.DEFAULT_LANGUE : langue);
		row.setTitle(title);
		row.setContent(trimContent(content));
		row.setCoverUrl(coverUrl);
		row.setDramaId(dramaId);
		row.setAssetId(assetId);
		row.setBizId(bizId);
		row.setJumpType(StringUtils.isEmpty(jumpType) ? CreatorConstants.MSG_JUMP_DRAMA : jumpType);
		row.setJumpParam(jumpParam);
		row.setIsRead(CreatorConstants.MSG_UNREAD);
		row.setStatus(CreatorConstants.MSG_STATUS_VALID);
		try {
			GenericityUtil.setDate(row);
			creatorSystemMessageDao.insert(row);
		} catch (DuplicateKeyException e) {
			return false;
		} catch (BaseException e) {
			log.error("creator inbox insert biz error creatorId={} bizId={}", toCreatorId, bizId, e);
			throw e;
		} catch (Exception e) {
			log.error("creator inbox insert failed creatorId={} bizId={}", toCreatorId, bizId, e);
			return false;
		}
		log.info("creator inbox sent creatorId={} bizId={} messageType={}", toCreatorId, bizId, messageType);
		return true;
	}

	private static String trimContent(String content) {
		if (content.length() <= CreatorConstants.MSG_CONTENT_MAX_LEN) {
			return content;
		}
		return content.substring(0, CreatorConstants.MSG_CONTENT_MAX_LEN);
	}
}
