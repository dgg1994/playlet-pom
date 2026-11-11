package com.playlet.internal.service.creator.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.CreatorMessageItemRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.message.CreatorSystemMessageDao;
import com.playlet.internal.dao.message.SystemMessagePublishDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.enums.CreatorMessageCategoryEnums;
import com.playlet.internal.enums.CreatorSystemMessageTypeEnums;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.creator.CreatorMessageQuery;
import com.playlet.internal.service.creator.CreatorMessageService;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作家端消息中心：列表 / 已读 / 未读数。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorMessageServiceImpl extends BaseApiService implements CreatorMessageService {

	@Autowired
	private CreatorSystemMessageDao creatorSystemMessageDao;
	@Autowired
	private SystemMessagePublishDao systemMessagePublishDao;
	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase findList(@RequestBody CreatorMessageQuery page, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (page == null) {
			page = new CreatorMessageQuery();
		}
		String langue = resolveLangue();
		long cursor = resolveCursor(account);
		List<String> types = CreatorMessageCategoryEnums.typesOf(page.getCategory());
		// 数组入参，避免 OGNL 对 List 反射调用 size()
		String[] typeArr = types == null ? null : types.toArray(new String[0]);
		boolean includeBroadcast = types == null
				|| types.contains(CreatorSystemMessageTypeEnums.SITE.getCode());
		PageHelper.startPage(page.getPageNumber(), page.getPageSize());
		List<CreatorMessageItemRespEntity> pageList = creatorSystemMessageDao.findMergedFeed(
				account.getId(), langue, cursor,
				CreatorSystemMessageTypeEnums.SITE.getCode(),
				CreatorConstants.MSG_SOURCE_INBOX,
				CreatorConstants.MSG_SOURCE_BROADCAST,
				CreatorConstants.MSG_STATUS_VALID,
				LanguageEnums.DEFAULT_LANGUE, typeArr, includeBroadcast,
				page.getStartTime(), page.getEndTime());
		if (pageList != null) {
			for (CreatorMessageItemRespEntity item : pageList) {
				if (item != null) {
					item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
					item.setCategoryName(CreatorSystemMessageTypeEnums.displayName(item.getMessageType()));
					item.setTimeText(formatTime(item.getSetTime()));
				}
			}
		}
		PageInfo<CreatorMessageItemRespEntity> pageInfo =
				new PageInfo<>(pageList == null ? new ArrayList<>() : pageList);
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase read(Long id, Long publishId, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (id == null && publishId == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		try {
			if (id != null) {
				creatorSystemMessageDao.readOne(id, account.getId(),
						CreatorConstants.MSG_READ, CreatorConstants.MSG_STATUS_VALID);
			}
			// 站务广播：游标只前进不后退
			if (publishId != null && publishId > resolveCursor(account)) {
				creatorAccountDao.updateSysMsgReadCursor(account.getId(), publishId);
			}
		} catch (Exception e) {
			log.error("creator message read failed creatorId={} id={} publishId={}", account.getId(), id, publishId, e);
			throw new BaseException("操作失败", e);
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase readAll(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		try {
			creatorSystemMessageDao.readAll(account.getId(), CreatorConstants.MSG_READ,
					CreatorConstants.MSG_STATUS_VALID, CreatorConstants.MSG_UNREAD);
			Long maxId = systemMessagePublishDao.maxBroadcastId();
			if (maxId != null && maxId > 0) {
				creatorAccountDao.updateSysMsgReadCursor(account.getId(), maxId);
			}
		} catch (Exception e) {
			log.error("creator message readAll failed creatorId={}", account.getId(), e);
			throw new BaseException("操作失败", e);
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase unreadCount(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		Integer inboxUnread = creatorSystemMessageDao.countUnread(account.getId(),
				CreatorConstants.MSG_STATUS_VALID, CreatorConstants.MSG_UNREAD);
		int inbox = inboxUnread == null ? 0 : inboxUnread;
		long cursor = resolveCursor(account);
		Integer broadcastUnread = systemMessagePublishDao.countBroadcastUnread(cursor);
		int broadcast = broadcastUnread == null ? 0 : broadcastUnread;
		Map<String, Object> data = new HashMap<>();
		data.put("inboxUnread", inbox);
		data.put("broadcastUnread", broadcast);
		data.put("totalUnread", inbox + broadcast);
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	private long resolveCursor(CreatorAccountEntity account) {
		if (account == null || account.getSysMsgReadPublishId() == null) {
			return 0L;
		}
		return account.getSysMsgReadPublishId();
	}

	private static String resolveLangue() {
		String lang = LanguageContext.getLanguage();
		return StringUtils.isEmpty(lang) ? LanguageEnums.DEFAULT_LANGUE : lang;
	}

	private static String formatTime(java.util.Date time) {
		return time == null ? "" : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
	}
}
