package com.playlet.internal.service.impl;

import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.SystemMessageItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.message.SystemMessagePublishDao;
import com.playlet.internal.dao.message.SystemMessagePublishI18nDao;
import com.playlet.internal.dao.message.UserSystemMessageDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.message.SystemMessagePublishEntity;
import com.playlet.internal.entity.message.SystemMessagePublishI18nEntity;
import com.playlet.internal.entity.message.UserSystemMessageEntity;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Transactional
@CrossOrigin
public class SystemMessageServiceImpl extends BaseApiService implements SystemMessageService {

	private static final String FALLBACK_LANGUE = "zh-cn";
	private static final String SOURCE_BROADCAST = "BROADCAST";
	private static final String SOURCE_INBOX = "INBOX";

	@Autowired
	private SystemMessagePublishDao systemMessagePublishDao;
	@Autowired
	private SystemMessagePublishI18nDao systemMessagePublishI18nDao;
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
		int pageNumber = page.getPageNumber() == null || page.getPageNumber() < 1 ? 1 : page.getPageNumber();
		int pageSize = page.getPageSize() == null || page.getPageSize() < 1 ? Constants.PAGESIZE : page.getPageSize();

		String langue = resolveLangue(null);
		long cursor = resolveCursor(uid);

		List<SystemMessageItemEntity> merged = new ArrayList<>();
		List<SystemMessagePublishEntity> broadcasts = systemMessagePublishDao.findActiveBroadcastList();
		if (broadcasts != null && !broadcasts.isEmpty()) {
			Map<Long, SystemMessagePublishI18nEntity> i18nMap = loadI18nMap(broadcasts, langue);
			for (SystemMessagePublishEntity pub : broadcasts) {
				SystemMessagePublishI18nEntity i18n = i18nMap.get(pub.getId());
				if (i18n == null) {
					continue;
				}
				SystemMessageItemEntity item = new SystemMessageItemEntity();
				item.setSource(SOURCE_BROADCAST);
				item.setId(pub.getId());
				item.setPublishId(pub.getId());
				item.setMessageType(pub.getMessageType());
				item.setTitle(i18n.getTitle());
				item.setContent(i18n.getContent());
				item.setCoverUrl(mediaUrlService.sign(i18n.getCoverUrl()));
				item.setDramaId(pub.getDramaId());
				item.setJumpType(pub.getJumpType());
				item.setJumpParam(StringUtils.isEmpty(i18n.getJumpParam()) ? pub.getJumpParam() : i18n.getJumpParam());
				item.setPriority(pub.getPriority() == null ? 0 : pub.getPriority());
				item.setIsRead(pub.getId() != null && pub.getId() <= cursor ? 1 : 0);
				item.setSetTime(pub.getSetTime());
				merged.add(item);
			}
		}

		List<UserSystemMessageEntity> inbox = userSystemMessageDao.findByToUid(uid);
		if (inbox != null) {
			for (UserSystemMessageEntity row : inbox) {
				SystemMessageItemEntity item = new SystemMessageItemEntity();
				item.setSource(SOURCE_INBOX);
				item.setId(row.getId());
				item.setInboxId(row.getId());
				item.setPublishId(row.getPublishId());
				item.setMessageType(row.getMessageType());
				item.setTitle(row.getTitle());
				item.setContent(row.getContent());
				item.setCoverUrl(mediaUrlService.sign(row.getCoverUrl()));
				item.setDramaId(row.getDramaId());
				item.setJumpType(row.getJumpType());
				item.setJumpParam(row.getJumpParam());
				item.setPriority(0);
				item.setIsRead(row.getIsRead() == null ? 0 : row.getIsRead());
				item.setSetTime(row.getSetTime());
				merged.add(item);
			}
		}

		merged.sort(Comparator
				.comparing((SystemMessageItemEntity i) -> i.getPriority() == null ? 0 : i.getPriority()).reversed()
				.thenComparing(SystemMessageItemEntity::getSetTime, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(SystemMessageItemEntity::getId, Comparator.nullsLast(Comparator.reverseOrder())));

		int total = merged.size();
		int from = Math.min((pageNumber - 1) * pageSize, total);
		int to = Math.min(from + pageSize, total);
		List<SystemMessageItemEntity> pageList = merged.subList(from, to);

		PageInfo<SystemMessageItemEntity> pageInfo = new PageInfo<>(pageList);
		pageInfo.setTotal(total);
		pageInfo.setPageNum(pageNumber);
		pageInfo.setPageSize(pageSize);
		pageInfo.setPages(pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize);
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

	private Map<Long, SystemMessagePublishI18nEntity> loadI18nMap(List<SystemMessagePublishEntity> pubs,
			String langue) {
		Map<Long, SystemMessagePublishI18nEntity> map = new HashMap<>();
		List<Long> ids = new ArrayList<>();
		for (SystemMessagePublishEntity p : pubs) {
			if (p.getId() != null) {
				ids.add(p.getId());
			}
		}
		if (ids.isEmpty()) {
			return map;
		}
		List<SystemMessagePublishI18nEntity> rows = systemMessagePublishI18nDao.findByPublishIds(ids);
		if (rows == null) {
			return map;
		}
		Map<Long, List<SystemMessagePublishI18nEntity>> byPub = new HashMap<>();
		for (SystemMessagePublishI18nEntity row : rows) {
			byPub.computeIfAbsent(row.getPublishId(), k -> new ArrayList<>()).add(row);
		}
		for (SystemMessagePublishEntity pub : pubs) {
			List<SystemMessagePublishI18nEntity> list = byPub.get(pub.getId());
			if (list == null || list.isEmpty()) {
				continue;
			}
			SystemMessagePublishI18nEntity hit = pickI18n(list, langue, pub.getDefaultLangue());
			if (hit != null) {
				map.put(pub.getId(), hit);
			}
		}
		return map;
	}

	private static SystemMessagePublishI18nEntity pickI18n(List<SystemMessagePublishI18nEntity> list,
			String langue, String defaultLangue) {
		SystemMessagePublishI18nEntity fallbackDef = null;
		SystemMessagePublishI18nEntity any = list.get(0);
		for (SystemMessagePublishI18nEntity row : list) {
			if (langue != null && langue.equalsIgnoreCase(row.getLangue())) {
				return row;
			}
			if (defaultLangue != null && defaultLangue.equalsIgnoreCase(row.getLangue())) {
				fallbackDef = row;
			}
			if (FALLBACK_LANGUE.equalsIgnoreCase(row.getLangue())) {
				any = row;
			}
		}
		return fallbackDef != null ? fallbackDef : any;
	}

	private long resolveCursor(Integer uid) {
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account == null || account.getSysMsgReadPublishId() == null) {
			return 0L;
		}
		return account.getSysMsgReadPublishId();
	}

	private static String resolveLangue(String prefer) {
		if (!StringUtils.isEmpty(prefer)) {
			return prefer;
		}
		String lang = LanguageContext.getLanguage();
		return StringUtils.isEmpty(lang) ? FALLBACK_LANGUE : lang;
	}
}
