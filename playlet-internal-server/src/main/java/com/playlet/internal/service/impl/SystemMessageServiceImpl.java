package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.SystemMessageItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.message.SystemMessagePublishDao;
import com.playlet.internal.dao.message.UserSystemMessageDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.SystemMessageService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
public class SystemMessageServiceImpl extends BaseApiService implements SystemMessageService {

	private static final String FALLBACK_LANGUE = "zh-cn";

	@Autowired
	private SystemMessagePublishDao systemMessagePublishDao;
	@Autowired
	private UserSystemMessageDao userSystemMessageDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase list(PageQueryHelperEntity page, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (page == null) {
			page = new PageQueryHelperEntity();
		}
		int pageNumber = page.getPageNumber();
		int pageSize = page.getPageSize();

		String langue = resolveLangue(null);
		long cursor = resolveCursor(uid);

		PageHelper.startPage(pageNumber, pageSize);
		List<SystemMessageItemEntity> pageList = userSystemMessageDao.findMergedFeed(uid, langue, cursor);
		if (pageList != null) {
			for (SystemMessageItemEntity item : pageList) {
				if (item != null) {
					item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
				}
			}
		}
		PageInfo<SystemMessageItemEntity> pageInfo = new PageInfo<>(pageList == null ? new ArrayList<>() : pageList);
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase read(Long id, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		userSystemMessageDao.readOne(id, uid);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase readAll(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		userSystemMessageDao.readAll(uid);
		Long maxId = systemMessagePublishDao.maxBroadcastId();
		if (maxId != null && maxId > 0) {
			appAccountDao.updateSysMsgReadCursor(uid, maxId);
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase unreadCount(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		Integer inboxUnread = userSystemMessageDao.countUnread(uid);
		int inbox = inboxUnread == null ? 0 : inboxUnread;
		long cursor = resolveCursor(uid);
		Integer broadcastUnread = systemMessagePublishDao.countBroadcastUnread(cursor);
		int broadcast = broadcastUnread == null ? 0 : broadcastUnread;
		Map<String, Object> data = new HashMap<>();
		data.put("inboxUnread", inbox);
		data.put("broadcastUnread", broadcast);
		data.put("totalUnread", inbox + broadcast);
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 获取用户已读游标
	 *
	 * @param uid 用户id
	 * @return 游标
	 */
	private long resolveCursor(Integer uid) {
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account == null || account.getSysMsgReadPublishId() == null) {
			return 0L;
		}
		return account.getSysMsgReadPublishId();
	}

	/**
	 * 获取语言
	 *
	 * @param prefer 语言
	 * @return 语言
	 */
	private static String resolveLangue(String prefer) {
		if (!StringUtils.isEmpty(prefer)) {
			return prefer;
		}
		String lang = LanguageContext.getLanguage();
		return StringUtils.isEmpty(lang) ? FALLBACK_LANGUE : lang;
	}
}
