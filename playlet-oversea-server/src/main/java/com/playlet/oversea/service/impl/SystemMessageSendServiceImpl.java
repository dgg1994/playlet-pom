package com.playlet.oversea.service.impl;

import com.playlet.oversea.constants.PushConstants;
import com.playlet.oversea.dao.message.UserSystemMessageDao;
import com.playlet.oversea.entity.message.UserSystemMessageEntity;
import com.playlet.oversea.service.PushNotifyService;
import com.playlet.oversea.service.SystemMessageSendService;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.StringUtils;
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
		// 落库成功后一律推送；push 参数保留兼容，false 时也推（产品要求：新增系统消息需通知栏提醒）
		Map<String, Object> extras = new HashMap<>();
		extras.put("bizType", PushConstants.BIZ_SYSTEM);
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
		String body = content == null ? "" : content.trim();
		if (body.length() > 120) {
			body = body.substring(0, 120) + "...";
		}
		pushNotifyService.notifyUser(toUid, title, body, extras);
		return true;
	}
}
