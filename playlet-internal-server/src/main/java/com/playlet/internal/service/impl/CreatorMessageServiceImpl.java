package com.playlet.internal.service.impl;

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
import com.playlet.internal.enums.CreatorSystemMessageTypeEnums;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.CreatorMessageService;
import com.playlet.internal.service.MediaUrlService;
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
	public ResponseBase findList(@RequestBody PageQueryHelperEntity page, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (page == null) {
			page = new PageQueryHelperEntity();
		}
		String langue = resolveLangue();
		long cursor = resolveCursor(account);
		PageHelper.startPage(page.getPageNumber(), page.getPageSize());
		List<CreatorMessageItemRespEntity> pageList = creatorSystemMessageDao.findMergedFeed(
				account.getId(), langue, cursor,
				CreatorSystemMessageTypeEnums.SITE.getCode(),
				CreatorConstants.MSG_SOURCE_INBOX,
				CreatorConstants.MSG_SOURCE_BROADCAST,
				CreatorConstants.MSG_STATUS_VALID,
				LanguageEnums.DEFAULT_LANGUE);
		if (pageList != null) {
			for (CreatorMessageItemRespEntity item : pageList) {
				if (item != null) {
					item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
				}
			}
		}
		PageInfo<CreatorMessageItemRespEntity> pageInfo =
				new PageInfo<>(pageList == null ? new ArrayList<>() : pageList);
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase read(Long id, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		try {
			creatorSystemMessageDao.readOne(id, account.getId(),
					CreatorConstants.MSG_READ, CreatorConstants.MSG_STATUS_VALID);
		} catch (BaseException e) {
			log.error("creator message read biz error creatorId={} id={}", account.getId(), id, e);
			throw e;
		} catch (Exception e) {
			log.error("creator message read failed creatorId={} id={}", account.getId(), id, e);
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
		} catch (BaseException e) {
			log.error("creator message readAll biz error creatorId={}", account.getId(), e);
			throw e;
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
}
