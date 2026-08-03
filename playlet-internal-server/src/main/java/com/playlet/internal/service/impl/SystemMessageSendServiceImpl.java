package com.playlet.internal.service.impl;

import com.playlet.internal.dao.message.UserSystemMessageDao;
import com.playlet.internal.entity.message.UserSystemMessageEntity;
import com.playlet.internal.service.PushNotifyService;
import com.playlet.internal.service.SystemMessageSendService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SystemMessageSendServiceImpl implements SystemMessageSendService {

	public static final String BIZ_SYSTEM = "SYSTEM";
	private static final String FALLBACK_LANGUE = "zh-cn";

	@Autowired
	private UserSystemMessageDao userSystemMessageDao;
	@Autowired
	private PushNotifyService pushNotifyService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean sendToUser(Integer toUid, String messageType, String langue,
			String title, String content, String coverUrl, Integer dramaId,
			String bizId, String jumpType, String jumpParam, boolean push) {
		if (toUid == null || StringUtils.isEmpty(bizId) || StringUtils.isEmpty(title)
				|| StringUtils.isEmpty(content) || StringUtils.isEmpty(messageType)) {
			return false;
		}
		if (userSystemMessageDao.findByBiz(toUid, bizId) != null) {
			return false;
		}
		UserSystemMessageEntity row = new UserSystemMessageEntity();
		row.setToUid(toUid);
		row.setPublishId(null);
		row.setMessageType(messageType);
		row.setLangue(StringUtils.isEmpty(langue) ? FALLBACK_LANGUE : langue);
		row.setTitle(title);
		row.setContent(content);
		row.setCoverUrl(coverUrl);
		row.setDramaId(dramaId);
		row.setBizId(bizId);
		row.setJumpType(StringUtils.isEmpty(jumpType) ? "none" : jumpType);
		row.setJumpParam(jumpParam);
		row.setIsRead(0);
		row.setStatus(1);
		try {
			GenericityUtil.setDate(row);
			userSystemMessageDao.insert(row);
		} catch (DuplicateKeyException e) {
			return false;
		} catch (Exception e) {
			log.warn("sendToUser insert failed uid={} bizId={}: {}", toUid, bizId, e.getMessage());
			return false;
		}
		if (push) {
			Map<String, Object> extras = new HashMap<>();
			extras.put("bizType", BIZ_SYSTEM);
			extras.put("messageType", messageType);
			extras.put("messageId", String.valueOf(row.getId()));
			if (dramaId != null) {
				extras.put("dramaId", String.valueOf(dramaId));
			}
			if (!StringUtils.isEmpty(jumpType)) {
				extras.put("jumpType", jumpType);
			}
			if (!StringUtils.isEmpty(jumpParam)) {
				extras.put("jumpParam", jumpParam);
			}
			pushNotifyService.notifyUser(toUid, title, content, extras);
		}
		return true;
	}
}
