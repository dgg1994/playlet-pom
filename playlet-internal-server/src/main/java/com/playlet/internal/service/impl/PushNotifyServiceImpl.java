package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.JpushReqEntity;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.account.AppPushDeviceDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.account.AppPushDeviceEntity;
import com.playlet.internal.enums.InteractMessageTypeEnums;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.enums.PushTemplateEnums;
import com.playlet.internal.service.PushNotifyService;
import com.playlet.internal.utils.JPushUtils;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.playlet.internal.constants.PushConstants.BIZ_INTERACT;
import static com.playlet.internal.constants.PushConstants.BIZ_MEDAL;
import static com.playlet.internal.constants.PushConstants.JPUSH_REG_BATCH;

@Slf4j
@Service
public class PushNotifyServiceImpl implements PushNotifyService {

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
			AppAccountEntity account = appAccountDao.findByUid(toUid);
			if (!isPushEnabled(account)) {
				log.debug("skip push: user disabled, toUid={}", toUid);
				return;
			}
			String registrationId = resolveRegistrationId(toUid, account);
			if (StringUtils.isEmpty(registrationId)) {
				log.debug("skip push: no registrationId, toUid={}", toUid);
				return;
			}
			sendToRegistrationIds(Collections.singletonList(registrationId), title, content, extras);
		} catch (Exception e) {
			log.warn("notifyUser failed toUid={}: {}", toUid, e.getMessage());
		}
	}

	@Override
	public void notifyAll(String title, String content, Map<String, Object> extras) {
		if (StringUtils.isEmpty(title) && StringUtils.isEmpty(content)) {
			return;
		}
		try {
			List<String> registrationIds = appAccountDao.findEnabledPushRegistrationIds();
			if (registrationIds == null || registrationIds.isEmpty()) {
				log.info("notifyAll skipped: no enabled registrationId");
				return;
			}
			sendToRegistrationIds(registrationIds, title, content, extras);
		} catch (Exception e) {
			log.warn("notifyAll failed: {}", e.getMessage());
		}
	}

	@Override
	public void notifyDevices(List<String> registrationIds, String title, String content,
			Map<String, Object> extras) {
		if (registrationIds == null || registrationIds.isEmpty()) {
			return;
		}
		if (StringUtils.isEmpty(title) && StringUtils.isEmpty(content)) {
			return;
		}
		try {
			sendToRegistrationIds(registrationIds, title, content, extras);
		} catch (Exception e) {
			log.warn("notifyDevices failed: {}", e.getMessage());
		}
	}

	/**
	 * 批量推送
	 * @param registrationIds 极光ID
	 * @param title 标题
	 * @param content  内容
	 * @param extras 扩展参数
	 */
	private void sendToRegistrationIds(List<String> registrationIds, String title, String content,
			Map<String, Object> extras) {
		for (int i = 0; i < registrationIds.size(); i += JPUSH_REG_BATCH) {
			int end = Math.min(i + JPUSH_REG_BATCH, registrationIds.size());
			List<String> batch = new ArrayList<>(registrationIds.subList(i, end));
			JpushReqEntity pushVo = new JpushReqEntity();
			pushVo.setTitle(title == null ? "" : title);
			pushVo.setMsg(content == null ? "" : content);
			pushVo.setBroadcasting(false);
			pushVo.setRegistrationIdList(batch);
			pushVo.setExtrasMap(extras);
			JPushUtils.sendAsync(pushVo);
		}
	}

	/** null / 非0 视为开启（默认开） */
	private static boolean isPushEnabled(AppAccountEntity account) {
		if (account == null) {
			return true;
		}
		return !Integer.valueOf(0).equals(account.getPushEnabled());
	}

	/** 优先账号表，其次设备表（游客先绑、登录后关联） */
	private String resolveRegistrationId(Integer uid, AppAccountEntity account) {
		if (account != null && !StringUtils.isEmpty(account.getRegistrationId())) {
			return account.getRegistrationId();
		}
		AppPushDeviceEntity device = appPushDeviceDao.findLatestByUid(uid);
		if (device != null && !StringUtils.isEmpty(device.getRegistrationId())) {
			return device.getRegistrationId();
		}
		return null;
	}

	/**
	 * 获取推送语言
	 * @param account 账号信息
	 * @return 推送语言
	 */
	private static String resolvePushLangue(AppAccountEntity account) {
		if (account == null || StringUtils.isEmpty(account.getPushLangue())) {
			return LanguageEnums.DEFAULT_LANGUE;
		}
		return LanguageEnums.of(account.getPushLangue()).getName();
	}

	@Override
	public void notifyInteract(Integer toUid, Integer fromUid, String messageType,
			Long messageId, Integer dramaId, String episodeId) {
		if (toUid == null || fromUid == null || toUid.equals(fromUid)) {
			return;
		}
		AppAccountEntity toAccount = appAccountDao.findByUid(toUid);
		String langue = resolvePushLangue(toAccount);
		String fromName = resolveNickname(fromUid);
		String someone = PushTemplateEnums.SOMEONE.format(langue);
		String name = StringUtils.isEmpty(fromName) ? someone : fromName;
		String title = PushTemplateEnums.INTERACT_TITLE.format(langue);
		String content = buildInteractContent(langue, name, messageType);
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
		// 直接发，避免 notifyUser 再查一次账号（开关/设备已在此判定）
		if (!isPushEnabled(toAccount)) {
			log.debug("skip interact push: user disabled, toUid={}", toUid);
			return;
		}
		String registrationId = resolveRegistrationId(toUid, toAccount);
		if (StringUtils.isEmpty(registrationId)) {
			return;
		}
		sendToRegistrationIds(Collections.singletonList(registrationId), title, content, extras);
	}

	@Override
	public void notifyMedalUnlock(Integer toUid, Integer medalId, String medalName) {
		if (toUid == null || medalId == null) {
			return;
		}
		AppAccountEntity toAccount = appAccountDao.findByUid(toUid);
		String langue = resolvePushLangue(toAccount);
		String name = StringUtils.isEmpty(medalName) ? "" : medalName;
		String title = PushTemplateEnums.MEDAL_TITLE.format(langue);
		String content = PushTemplateEnums.MEDAL_UNLOCK.format(langue, name);
		Map<String, Object> extras = new HashMap<>();
		extras.put("bizType", BIZ_MEDAL);
		extras.put("medalId", String.valueOf(medalId));
		if (!isPushEnabled(toAccount)) {
			log.debug("skip medal push: user disabled, toUid={}", toUid);
			return;
		}
		String registrationId = resolveRegistrationId(toUid, toAccount);
		if (StringUtils.isEmpty(registrationId)) {
			return;
		}
		sendToRegistrationIds(Collections.singletonList(registrationId), title, content, extras);
	}

	/**
	 * 构建互动消息内容
	 *
	 * @param langue 语言
	 * @param name 昵称
	 * @param messageType 消息类型
	 * @return  内容
	 */
	private String buildInteractContent(String langue, String name, String messageType) {
		if (InteractMessageTypeEnums.LIKE_DRAMA.getCode().equals(messageType)
				|| InteractMessageTypeEnums.LIKE_COMMENT.getCode().equals(messageType)) {
			return PushTemplateEnums.LIKE.format(langue, name);
		}
		if (InteractMessageTypeEnums.isReply(messageType)) {
			return PushTemplateEnums.REPLY.format(langue, name);
		}
		if (InteractMessageTypeEnums.COMMENT_DRAMA.getCode().equals(messageType)
				|| InteractMessageTypeEnums.COMMENT_VIDEO.getCode().equals(messageType)) {
			return PushTemplateEnums.COMMENT.format(langue, name);
		}
		return PushTemplateEnums.INTERACT_DEFAULT.format(langue, name);
	}

	/**
	 * 获取昵称
	 * @param uid 用户ID
	 * @return 昵称
	 */
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
