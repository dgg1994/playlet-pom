package com.playlet.oversea.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.playlet.oversea.api.request.WatchPreferSaveItemRequest;
import com.playlet.oversea.api.request.WatchPreferSaveRequest;
import com.playlet.oversea.api.response.WatchPreferRespEntity;
import com.playlet.oversea.api.response.WatchPreferTagRespEntity;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.constants.WatchPreferConstants;
import com.playlet.oversea.dao.prefer.UserWatchPreferenceDao;
import com.playlet.oversea.dao.prefer.WatchPreferCategoryDao;
import com.playlet.oversea.dao.prefer.WatchPreferTagDao;
import com.playlet.oversea.entity.prefer.UserWatchPreferenceEntity;
import com.playlet.oversea.entity.prefer.WatchPreferCategoryEntity;
import com.playlet.oversea.entity.prefer.WatchPreferTagEntity;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.UserWatchPrefService;
import com.playlet.oversea.utils.AppTokenUtil;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 类描述：设置/修改用户偏好
 *
 * @author GeminiSun
 * @date 2026/10/10 14:49
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class UserWatchPrefServiceImpl implements UserWatchPrefService {

	@Autowired
	private WatchPreferCategoryDao watchPreferCategoryDao;

	@Autowired
	private WatchPreferTagDao watchPreferTagDao;

	@Autowired
	private UserWatchPreferenceDao userWatchPreferenceDao;

	@Override
	public ResponseBase save(@RequestBody WatchPreferSaveRequest watchPreferSaveRequest, HttpServletRequest request) {
		// 首次设置：允许尚无偏好记录
		return overwritePrefer(watchPreferSaveRequest, request, false);
	}

	@Override
	public ResponseBase update(@RequestBody WatchPreferSaveRequest watchPreferSaveRequest, HttpServletRequest request) {
		// 修改：须已设置过，再全量覆盖
		return overwritePrefer(watchPreferSaveRequest, request, true);
	}

	@Override
	public ResponseBase mine(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}

		String langue = LanguageContext.getLanguage();
		List<WatchPreferCategoryEntity> categories = watchPreferCategoryDao.findByLangueAndStatus(langue,
				WatchPreferConstants.STATUS_ENABLED);
		if (categories == null) {
			categories = Collections.emptyList();
		}

		// 用户已选 groupId
		List<UserWatchPreferenceEntity> prefs = userWatchPreferenceDao.findByUid(uid);
		Set<String> selectedGroupIds = new HashSet<>();
		if (prefs != null) {
			for (UserWatchPreferenceEntity pref : prefs) {
				if (pref != null && StringUtils.isNotBlank(pref.getPreferGroupId())) {
					selectedGroupIds.add(pref.getPreferGroupId());
				}
			}
		}

		List<WatchPreferTagEntity> enabledTags = watchPreferTagDao.findByLangueAndStatus(langue,
				WatchPreferConstants.STATUS_ENABLED);
		Map<String, List<WatchPreferTagEntity>> tagsByCategory = new HashMap<>();
		if (enabledTags != null) {
			for (WatchPreferTagEntity tag : enabledTags) {
				if (tag == null || StringUtils.isBlank(tag.getCategoryCode())) {
					continue;
				}
				tagsByCategory.computeIfAbsent(tag.getCategoryCode(), k -> new ArrayList<>()).add(tag);
			}
		}

		List<WatchPreferRespEntity> list = new ArrayList<>();
		for (WatchPreferCategoryEntity category : categories) {
			WatchPreferRespEntity resp = new WatchPreferRespEntity();
			resp.setCategoryCode(category.getCategoryCode());
			resp.setCategoryName(category.getCategoryName());
			List<WatchPreferTagEntity> tags = tagsByCategory.getOrDefault(category.getCategoryCode(),
					Collections.emptyList());
			if (!tags.isEmpty()) {
				List<WatchPreferTagRespEntity> tagResps = new ArrayList<>();
				for (WatchPreferTagEntity tag : tags) {
					WatchPreferTagRespEntity tagResp = new WatchPreferTagRespEntity();
					tagResp.setGroupId(tag.getGroupId());
					tagResp.setTagName(tag.getTagName());
					tagResp.setSelected(selectedGroupIds.contains(tag.getGroupId()));
					tagResps.add(tagResp);
				}
				resp.setWatchPreferTagRespEntities(tagResps);
			}
			list.add(resp);
		}
		log.info("watch prefer mine uid={} selectedCount={}", uid, selectedGroupIds.size());
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 全量覆盖写入用户偏好。
	 *
	 * @param requireExisting true=修改（须已有记录）；false=首次设置
	 */
	private ResponseBase overwritePrefer(WatchPreferSaveRequest watchPreferSaveRequest, HttpServletRequest request,
			boolean requireExisting) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (watchPreferSaveRequest == null || CollectionUtils.isEmpty(watchPreferSaveRequest.getItems())) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		// 修改场景：尚未设置过则不允许走 update
		if (requireExisting && userWatchPreferenceDao.countByUid(uid) <= 0) {
			log.info("watch prefer update but empty uid={}", uid);
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}

		String langue = LanguageContext.getLanguage();
		List<WatchPreferCategoryEntity> categories = watchPreferCategoryDao.findByLangueAndStatus(langue,
				WatchPreferConstants.STATUS_ENABLED);
		if (CollectionUtils.isEmpty(categories)) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}

		Map<String, List<String>> itemMap;
		try {
			itemMap = buildItemMap(watchPreferSaveRequest.getItems());
		} catch (BaseException e) {
			return setResultError(e.getMessage());
		}

		Set<String> validCodes = categories.stream()
				.map(WatchPreferCategoryEntity::getCategoryCode)
				.collect(Collectors.toSet());
		for (String code : itemMap.keySet()) {
			if (!validCodes.contains(code)) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
		}

		List<WatchPreferTagEntity> enabledTags = watchPreferTagDao.findByLangueAndStatus(langue,
				WatchPreferConstants.STATUS_ENABLED);
		Map<String, String> groupToCategory = new HashMap<>();
		if (enabledTags != null) {
			for (WatchPreferTagEntity tag : enabledTags) {
				if (tag != null && StringUtils.isNotBlank(tag.getGroupId())) {
					groupToCategory.put(tag.getGroupId(), tag.getCategoryCode());
				}
			}
		}

		List<UserWatchPreferenceEntity> toSave = new ArrayList<>();
		for (WatchPreferCategoryEntity category : categories) {
			String code = category.getCategoryCode();
			List<String> selected = itemMap.getOrDefault(code, Collections.emptyList());
			List<String> distinctIds = new ArrayList<>(new LinkedHashSet<>(selected));

			int minSelect = category.getMinSelect() == null ? 0 : category.getMinSelect();
			int maxSelect = category.getMaxSelect() == null ? WatchPreferConstants.MAX_SELECT_UNLIMITED
					: category.getMaxSelect();
			if (distinctIds.size() < minSelect) {
				log.info("watch prefer min_select fail uid={} category={} size={} min={}",
						uid, code, distinctIds.size(), minSelect);
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			if (maxSelect > WatchPreferConstants.MAX_SELECT_UNLIMITED && distinctIds.size() > maxSelect) {
				log.info("watch prefer max_select fail uid={} category={} size={} max={}",
						uid, code, distinctIds.size(), maxSelect);
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}

			for (String groupId : distinctIds) {
				if (StringUtils.isBlank(groupId)) {
					return setResultError(I18nUtil.getMessage("param_not_null"));
				}
				String belongCategory = groupToCategory.get(groupId);
				if (belongCategory == null || !belongCategory.equals(code)) {
					log.info("watch prefer group invalid uid={} category={} groupId={}", uid, code, groupId);
					return setResultError(I18nUtil.getMessage("param_not_null"));
				}
				UserWatchPreferenceEntity row = new UserWatchPreferenceEntity();
				row.setUid(uid);
				row.setCategoryCode(code);
				row.setPreferGroupId(groupId);
				toSave.add(row);
			}
		}
		int sortWeight = toSave.size();
		for (UserWatchPreferenceEntity row : toSave) {
			row.setSortWeight(sortWeight--);
		}

		try {
			// 先删后插：修改与首次设置共用全量覆盖
			userWatchPreferenceDao.deleteByUid(uid);
			for (UserWatchPreferenceEntity row : toSave) {
				GenericityUtil.setDate(row);
				userWatchPreferenceDao.insertOne(row);
			}
			log.info("watch prefer overwrite ok uid={} count={} requireExisting={}",
					uid, toSave.size(), requireExisting);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("watch prefer overwrite biz error uid={}", uid, e);
			throw e;
		} catch (Exception e) {
			log.error("watch prefer overwrite failed uid={}", uid, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/**
	 * 将入参 items 转为 categoryCode -> preferGroupIds；非法项抛业务异常。
	 */
	private Map<String, List<String>> buildItemMap(List<WatchPreferSaveItemRequest> items) {
		Map<String, List<String>> itemMap = new HashMap<>();
		Set<String> seenCodes = new HashSet<>();
		for (WatchPreferSaveItemRequest item : items) {
			if (item == null || StringUtils.isBlank(item.getCategoryCode())) {
				throw new BaseException(I18nUtil.getMessage("param_not_null"));
			}
			String code = item.getCategoryCode().trim();
			if (!seenCodes.add(code)) {
				throw new BaseException(I18nUtil.getMessage("param_not_null"));
			}
			List<String> groupIds = item.getPreferGroupIds() == null
					? Collections.emptyList() : item.getPreferGroupIds();
			itemMap.put(code, groupIds);
		}
		return itemMap;
	}
}
