package com.playlet.internal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.account.AppPushDeviceDao;
import com.playlet.internal.dao.message.SystemMessagePublishDao;
import com.playlet.internal.dao.message.SystemMessagePublishI18nDao;
import com.playlet.internal.dao.message.UserSystemMessageDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.message.SystemMessagePublishEntity;
import com.playlet.internal.entity.message.SystemMessagePublishI18nEntity;
import com.playlet.internal.entity.message.UserSystemMessageEntity;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.enums.SystemMessageAudienceTypeEnums;
import com.playlet.internal.enums.SystemMessagePublishStatusEnums;
import com.playlet.internal.constants.PushConstants;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.PushNotifyService;
import com.playlet.internal.service.SystemMessageManageService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.HtmlSanitizeUtils;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.TransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class SystemMessageManageServiceImpl implements SystemMessageManageService {

	private static final String FALLBACK_LANGUE = "zh-cn";

	@Autowired
	private SystemMessagePublishDao systemMessagePublishDao;
	@Autowired
	private SystemMessagePublishI18nDao systemMessagePublishI18nDao;
	@Autowired
	private UserSystemMessageDao userSystemMessageDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private AppPushDeviceDao appPushDeviceDao;
	@Autowired
	private PushNotifyService pushNotifyService;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	@SysLogAnnotation(module = "系统消息管理", type = "POST", remark = "发布单列表")
	public ResponseBase findList(@RequestBody SystemMessagePublishEntity entity) {
		if (entity == null) {
			entity = new SystemMessagePublishEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<SystemMessagePublishEntity> list = systemMessagePublishDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "系统消息管理", type = "POST", remark = "发布单详情")
	public ResponseBase detail(@RequestBody SystemMessagePublishEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		SystemMessagePublishEntity row = systemMessagePublishDao.selectById(entity.getId());
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		List<SystemMessagePublishI18nEntity> i18nList = systemMessagePublishI18nDao.findByPublishId(row.getId());
		if (i18nList != null) {
			for (SystemMessagePublishI18nEntity i18n : i18nList) {
				i18n.setCoverUrl(mediaUrlService.sign(i18n.getCoverUrl()));
			}
			row.setI18nList(i18nList);
		}
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "系统消息管理", type = "POST", remark = "新增发布单")
	public ResponseBase save(@RequestBody SystemMessagePublishEntity entity) {
		String err = validatePublish(entity, true);
		if (err != null) {
			return setResultError(err);
		}
		fillDefaults(entity);
		entity.setPublishStatus(SystemMessagePublishStatusEnums.DRAFT.getCode());
		entity.setStatus(1);
		try {
			GenericityUtil.setDate(entity);
			systemMessagePublishDao.insert(entity);
			saveI18nList(entity.getId(), entity.getI18nList(), true);
		} catch (Exception e) {
			log.error("save system message failed", e);
			TransactionUtils.markRollbackOnly();
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		return setResultSuccess(entity.getId(), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "系统消息管理", type = "POST", remark = "编辑发布单")
	public ResponseBase update(@RequestBody SystemMessagePublishEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		SystemMessagePublishEntity exist = systemMessagePublishDao.selectById(entity.getId());
		if (exist == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		String err = validatePublish(entity, false);
		if (err != null) {
			return setResultError(err);
		}
		fillDefaults(entity);
		entity.setPublishStatus(exist.getPublishStatus());
		try {
			GenericityUtil.updateDate(entity);
			systemMessagePublishDao.updateById(entity);
			saveI18nList(entity.getId(), entity.getI18nList(), true);
		} catch (Exception e) {
			log.error("update system message failed id={}", entity.getId(), e);
			TransactionUtils.markRollbackOnly();
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "系统消息管理", type = "POST", remark = "发布")
	public ResponseBase publish(@RequestBody SystemMessagePublishEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		SystemMessagePublishEntity row = systemMessagePublishDao.selectById(entity.getId());
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		List<SystemMessagePublishI18nEntity> i18nList = systemMessagePublishI18nDao.findByPublishId(row.getId());
		if (i18nList == null || i18nList.isEmpty()) {
			return setResultError(I18nUtil.getMessage("sysmsg.i18n_required"));
		}
		if (row.getScheduleTime() != null && row.getScheduleTime().after(new Date())) {
			// 定时：先标已发布，C端列表按 schedule/valid_start 过滤；当前精简版 valid_start 可承担定时展示
			if (row.getValidStart() == null) {
				row.setValidStart(row.getScheduleTime());
				try {
					GenericityUtil.updateDate(row);
					systemMessagePublishDao.updateById(row);
				} catch (Exception ignore) {
				}
			}
		}
		systemMessagePublishDao.updatePublishStatus(row.getId(),
				SystemMessagePublishStatusEnums.PUBLISHED.getCode());

		// 定时未到点：只入库展示，不立刻推送（避免用户提前收到）
		boolean dueNow = row.getScheduleTime() == null || !row.getScheduleTime().after(new Date());
		if (!dueNow) {
			log.info("system message published id={} scheduleTime={} skip immediate push",
					row.getId(), row.getScheduleTime());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}

		if (Integer.valueOf(SystemMessageAudienceTypeEnums.UID_LIST.getCode()).equals(row.getAudienceType())) {
			fanoutToInbox(row, i18nList);
		} else {
			// 全员读扩散：列表由 C 端合并广播；推送走极光广播
			pushBroadcast(row, i18nList);
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "系统消息管理", type = "POST", remark = "取消发布")
	public ResponseBase cancel(@RequestBody SystemMessagePublishEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		systemMessagePublishDao.updatePublishStatus(entity.getId(),
				SystemMessagePublishStatusEnums.CANCELLED.getCode());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "系统消息管理", type = "POST", remark = "上下架")
	public ResponseBase changeStatus(@RequestBody SystemMessagePublishEntity entity) {
		if (entity == null || entity.getId() == null || entity.getStatus() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		systemMessagePublishDao.updateStatus(entity.getId(), entity.getStatus());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 指定用户投递 + 按用户语言推送
	 */
	private void fanoutToInbox(SystemMessagePublishEntity row, List<SystemMessagePublishI18nEntity> i18nList) {
		List<Integer> uids = parseUidList(row.getAudienceJson());
		if (uids.isEmpty()) {
			return;
		}
		SystemMessagePublishI18nEntity def = pickI18n(i18nList, row.getDefaultLangue());
		if (def == null) {
			def = i18nList.get(0);
		}
		Map<Integer, AppAccountEntity> accountMap = loadAccountMap(uids);
		for (Integer uid : uids) {
			if (uid == null) {
				continue;
			}
			String bizId = "publish:" + row.getId() + ":" + uid;
			if (userSystemMessageDao.findByBiz(uid, bizId) != null) {
				continue;
			}
			AppAccountEntity account = accountMap.get(uid);
			String userLangue = account == null || StringUtils.isEmpty(account.getPushLangue())
					? LanguageEnums.DEFAULT_LANGUE : LanguageEnums.of(account.getPushLangue()).getName();
			SystemMessagePublishI18nEntity i18n = pickI18n(i18nList, userLangue);
			if (i18n == null) {
				i18n = def;
			}
			UserSystemMessageEntity msg = new UserSystemMessageEntity();
			msg.setToUid(uid);
			msg.setPublishId(row.getId());
			msg.setMessageType(row.getMessageType());
			msg.setLangue(i18n.getLangue());
			msg.setTitle(i18n.getTitle());
			msg.setContent(i18n.getContent());
			msg.setCoverUrl(i18n.getCoverUrl());
			msg.setDramaId(row.getDramaId());
			msg.setBizId(bizId);
			msg.setJumpType(StringUtils.isEmpty(row.getJumpType()) ? "none" : row.getJumpType());
			msg.setJumpParam(StringUtils.isEmpty(i18n.getJumpParam()) ? row.getJumpParam() : i18n.getJumpParam());
			msg.setIsRead(0);
			msg.setStatus(1);
			try {
				GenericityUtil.setDate(msg);
				userSystemMessageDao.insert(msg);
			} catch (DuplicateKeyException e) {
				continue;
			} catch (Exception e) {
				log.warn("fanout insert failed publishId={} uid={}: {}", row.getId(), uid, e.getMessage());
				continue;
			}
			Map<String, Object> extras = buildSystemExtras(row, msg.getId());
			pushNotifyService.notifyUser(uid, i18n.getTitle(), truncatePushBody(i18n.getContent()), extras);
		}
	}

	/**
	 * 全员广播：按用户 push_langue 分组，推对应 i18n 文案
	 */
	private void pushBroadcast(SystemMessagePublishEntity row, List<SystemMessagePublishI18nEntity> i18nList) {
		SystemMessagePublishI18nEntity def = pickI18n(i18nList, row.getDefaultLangue());
		if (def == null && i18nList != null && !i18nList.isEmpty()) {
			def = i18nList.get(0);
		}
		if (def == null || StringUtils.isEmpty(def.getTitle())) {
			log.warn("skip broadcast push: no i18n title, publishId={}", row.getId());
			return;
		}
		List<AppAccountEntity> targets = appPushDeviceDao.findEnabledPushTargets();
		if (targets == null || targets.isEmpty()) {
			log.info("broadcast push skipped: no enabled targets, publishId={}", row.getId());
			return;
		}
		Map<String, List<String>> regByLangue = new HashMap<>();
		for (AppAccountEntity t : targets) {
			if (t == null || StringUtils.isEmpty(t.getRegistrationId())) {
				continue;
			}
			String langue = LanguageEnums.of(t.getPushLangue()).getName();
			regByLangue.computeIfAbsent(langue, k -> new ArrayList<>()).add(t.getRegistrationId());
		}
		Map<String, Object> extras = buildSystemExtras(row, null);
		for (Map.Entry<String, List<String>> e : regByLangue.entrySet()) {
			SystemMessagePublishI18nEntity i18n = pickI18n(i18nList, e.getKey());
			if (i18n == null) {
				i18n = def;
			}
			pushNotifyService.notifyDevices(e.getValue(), i18n.getTitle(),
					truncatePushBody(i18n.getContent()), extras);
		}
		log.info("broadcast push sent publishId={} langues={}", row.getId(), regByLangue.keySet());
	}

	private Map<Integer, AppAccountEntity> loadAccountMap(List<Integer> uids) {
		if (uids == null || uids.isEmpty()) {
			return java.util.Collections.emptyMap();
		}
		List<Integer> uniq = new ArrayList<>(new java.util.HashSet<>(uids));
		List<AppAccountEntity> list = appAccountDao.findByUids(uniq);
		if (list == null || list.isEmpty()) {
			return java.util.Collections.emptyMap();
		}
		Map<Integer, AppAccountEntity> map = new HashMap<>(list.size());
		for (AppAccountEntity a : list) {
			if (a != null && a.getId() != null) {
				map.put(a.getId(), a);
			}
		}
		return map;
	}

	/**
	 * 构建系统消息推送参数
	 * @param row
	 * @param inboxId
	 * @return
	 */
	private static Map<String, Object> buildSystemExtras(SystemMessagePublishEntity row, Long inboxId) {
		Map<String, Object> extras = new HashMap<>();
		extras.put("bizType", PushConstants.BIZ_SYSTEM);
		extras.put("messageType", row.getMessageType());
		if (row.getId() != null) {
			extras.put("publishId", String.valueOf(row.getId()));
		}
		if (inboxId != null) {
			extras.put("messageId", String.valueOf(inboxId));
		}
		if (row.getDramaId() != null) {
			extras.put("dramaId", String.valueOf(row.getDramaId()));
		}
		if (!StringUtils.isEmpty(row.getJumpType())) {
			extras.put("jumpType", row.getJumpType());
		}
		if (!StringUtils.isEmpty(row.getJumpParam())) {
			extras.put("jumpParam", row.getJumpParam());
		}
		return extras;
	}

	/**
	 * 截取推送内容
	 * @param content
	 * @return
	 */
	private static String truncatePushBody(String content) {
		if (content == null) {
			return "";
		}
		String text = content.trim();
		if (text.length() <= 120) {
			return text;
		}
		return text.substring(0, 120) + "...";
	}

	/**
	 * 保存多语言
	 * @param publishId 发布单id
	 * @param i18nList 多语言
	 * @param replace 是否替换
	 * @throws Exception
	 */
	private void saveI18nList(Long publishId, List<SystemMessagePublishI18nEntity> i18nList, boolean replace)
			throws Exception {
		if (replace) {
			systemMessagePublishI18nDao.deleteByPublishId(publishId);
		}
		if (i18nList == null || i18nList.isEmpty()) {
			return;
		}
		for (SystemMessagePublishI18nEntity i18n : i18nList) {
			if (i18n == null || StringUtils.isEmpty(i18n.getLangue())
					|| StringUtils.isEmpty(i18n.getTitle()) || StringUtils.isEmpty(i18n.getContent())) {
				continue;
			}
			i18n.setId(null);
			i18n.setPublishId(publishId);
			i18n.setTitle(HtmlSanitizeUtils.plain(i18n.getTitle()));
			i18n.setContent(HtmlSanitizeUtils.rich(i18n.getContent()));
			GenericityUtil.setDate(i18n);
			systemMessagePublishI18nDao.insert(i18n);
		}
	}

	/**
	 * 验证发布单
	 * @param entity 发布单
	 * @param creating 创建
	 * @return
	 */
	private static String validatePublish(SystemMessagePublishEntity entity, boolean creating) {
		if (entity == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (StringUtils.isEmpty(entity.getMessageType())) {
			return I18nUtil.getMessage("sysmsg.type_required");
		}
		if (entity.getAudienceType() == null) {
			entity.setAudienceType(SystemMessageAudienceTypeEnums.ALL.getCode());
		}
		if (Integer.valueOf(SystemMessageAudienceTypeEnums.UID_LIST.getCode()).equals(entity.getAudienceType())
				&& StringUtils.isEmpty(entity.getAudienceJson())) {
			return I18nUtil.getMessage("sysmsg.audience_required");
		}
		if (creating && (entity.getI18nList() == null || entity.getI18nList().isEmpty())) {
			return I18nUtil.getMessage("sysmsg.i18n_required");
		}
		if (entity.getI18nList() != null) {
			for (SystemMessagePublishI18nEntity i18n : entity.getI18nList()) {
				if (i18n == null || StringUtils.isEmpty(i18n.getLangue())
						|| StringUtils.isEmpty(i18n.getTitle()) || StringUtils.isEmpty(i18n.getContent())) {
					return I18nUtil.getMessage("sysmsg.i18n_required");
				}
			}
		}
		return null;
	}

	/**
	 * 填充默认值
	 * @param entity
	 */
	private static void fillDefaults(SystemMessagePublishEntity entity) {
		if (StringUtils.isEmpty(entity.getDefaultLangue())) {
			String lang = LanguageContext.getLanguage();
			entity.setDefaultLangue(StringUtils.isEmpty(lang) ? FALLBACK_LANGUE : lang);
		}
		if (StringUtils.isEmpty(entity.getJumpType())) {
			entity.setJumpType("none");
		}
		if (entity.getPriority() == null) {
			entity.setPriority(0);
		}
		if (entity.getPushFlag() == null) {
			entity.setPushFlag(0);
		}
		if (entity.getAudienceType() == null) {
			entity.setAudienceType(SystemMessageAudienceTypeEnums.ALL.getCode());
		}
	}

	/**
	 * 获取多语言
	 * @param list
	 * @param defaultLangue
	 * @return
	 */
	private static SystemMessagePublishI18nEntity pickI18n(List<SystemMessagePublishI18nEntity> list,
			String defaultLangue) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		for (SystemMessagePublishI18nEntity row : list) {
			if (defaultLangue != null && defaultLangue.equalsIgnoreCase(row.getLangue())) {
				return row;
			}
		}
		for (SystemMessagePublishI18nEntity row : list) {
			if (FALLBACK_LANGUE.equalsIgnoreCase(row.getLangue())) {
				return row;
			}
		}
		return list.get(0);
	}

	/**
	 * 解析用户id列表
	 * @param audienceJson
	 * @return
	 */
	private static List<Integer> parseUidList(String audienceJson) {
		List<Integer> uids = new ArrayList<>();
		if (StringUtils.isEmpty(audienceJson)) {
			return uids;
		}
		try {
			JSONArray arr = JSON.parseArray(audienceJson);
			if (arr == null) {
				return uids;
			}
			for (int i = 0; i < arr.size(); i++) {
				uids.add(arr.getInteger(i));
			}
		} catch (Exception e) {
			log.warn("parse audience_json failed: {}", e.getMessage());
		}
		return uids;
	}
}
