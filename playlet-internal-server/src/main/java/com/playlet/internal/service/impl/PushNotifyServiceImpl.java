package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.JpushReqEntity;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.account.AppPushDeviceDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.account.AppPushDeviceEntity;
import com.playlet.internal.enums.InteractMessageTypeEnums;
import com.playlet.internal.service.PushNotifyService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.JPushUtils;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PushNotifyServiceImpl implements PushNotifyService {

	public static final String BIZ_INTERACT = "INTERACT";
	public static final String BIZ_MEDAL = "MEDAL";

	@Autowired
	private AppAccountDao appAccountDao;

	@Autowired
	private AppPushDeviceDao appPushDeviceDao;

	@Override
	public void notifyUser(Integer toUid, String title, String content, Map<String, Object> extras) {
		if (toUid == null) {
			return;
		}
		if (StringUtils.isEmpty(title) && StringUtils.isEmpty(content)) {
			return;
		}
		try {
			String registrationId = resolveRegistrationId(toUid);
			if (StringUtils.isEmpty(registrationId)) {
				log.debug("skip push: no registrationId, toUid={}", toUid);
				return;
			}
			JpushReqEntity pushVo = new JpushReqEntity();
			pushVo.setTitle(title == null ? "" : title);
			pushVo.setMsg(content == null ? "" : content);
			pushVo.setBroadcasting(false);
			pushVo.setRegistrationIdList(Collections.singletonList(registrationId));
			pushVo.setExtrasMap(extras);
			JPushUtils.sendAsync(pushVo);
		} catch (Exception e) {
			log.warn("notifyUser failed toUid={}: {}", toUid, e.getMessage());
		}
	}

	/** 优先账号表，其次设备表（游客先绑、登录后关联） */
	private String resolveRegistrationId(Integer uid) {
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account != null && !StringUtils.isEmpty(account.getRegistrationId())) {
			return account.getRegistrationId();
		}
		AppPushDeviceEntity device = appPushDeviceDao.findLatestByUid(uid);
		if (device != null && !StringUtils.isEmpty(device.getRegistrationId())) {
			return device.getRegistrationId();
		}
		return null;
	}

	@Override
	public void notifyInteract(Integer toUid, Integer fromUid, String messageType,
			Long messageId, Integer dramaId, String episodeId) {
		if (toUid == null || fromUid == null || toUid.equals(fromUid)) {
			return;
		}
		String fromName = resolveNickname(fromUid);
		String title = I18nUtil.getMessage("push.interact_title");
		String content = buildInteractContent(fromName, messageType);
		Map<String, Object> extras = new HashMap<>();
		extras.put("bizType", BIZ_INTERACT);
		extras.put("messageType", messageType);
		if (messageId != null) {
			extras.put("messageId", String.valueOf(messageId));
		}
		if (dramaId != null) {
			extras.put("dramaId", String.valueOf(dramaId));
		}
		if (!StringUtils.isEmpty(episodeId)) {
			extras.put("episodeId", episodeId);
		}
		notifyUser(toUid, title, content, extras);
	}

	@Override
	public void notifyMedalUnlock(Integer toUid, Integer medalId, String medalName) {
		if (toUid == null || medalId == null) {
			return;
		}
		String name = StringUtils.isEmpty(medalName) ? "" : medalName;
		String title = I18nUtil.getMessage("push.medal_title");
		String content = I18nUtil.getMessage("push.medal_unlock", name);
		Map<String, Object> extras = new HashMap<>();
		extras.put("bizType", BIZ_MEDAL);
		extras.put("medalId", String.valueOf(medalId));
		notifyUser(toUid, title, content, extras);
	}

	private String buildInteractContent(String fromName, String messageType) {
		String name = StringUtils.isEmpty(fromName) ? I18nUtil.getMessage("push.someone") : fromName;
		if (InteractMessageTypeEnums.LIKE_DRAMA.getCode().equals(messageType)
				|| InteractMessageTypeEnums.LIKE_COMMENT.getCode().equals(messageType)) {
			return I18nUtil.getMessage("push.like", name);
		}
		if (InteractMessageTypeEnums.isReply(messageType)) {
			return I18nUtil.getMessage("push.reply", name);
		}
		if (InteractMessageTypeEnums.COMMENT_DRAMA.getCode().equals(messageType)
				|| InteractMessageTypeEnums.COMMENT_VIDEO.getCode().equals(messageType)) {
			return I18nUtil.getMessage("push.comment", name);
		}
		return I18nUtil.getMessage("push.interact_default", name);
	}

	private String resolveNickname(Integer uid) {
		try {
			AppAccountEntity account = appAccountDao.findByUid(uid);
			if (account == null) {
				return null;
			}
			if (!StringUtils.isEmpty(account.getNickname())) {
				return account.getNickname();
			}
			return account.getUserAccount();
		} catch (Exception e) {
			return null;
		}
	}
}
