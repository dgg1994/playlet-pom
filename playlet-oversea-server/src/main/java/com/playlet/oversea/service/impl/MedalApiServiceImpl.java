package com.playlet.oversea.service.impl;

import com.playlet.oversea.api.request.MedalAckNotifyRequest;
import com.playlet.oversea.api.response.MedalApiResponse;
import com.playlet.oversea.api.response.MedalDetailEntity;
import com.playlet.oversea.api.response.MedalNotifyEntity;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.dao.medal.MedalConfigDao;
import com.playlet.oversea.dao.medal.MedalConfigI18nDao;
import com.playlet.oversea.dao.medal.UserMedalDao;
import com.playlet.oversea.entity.medal.MedalConfigEntity;
import com.playlet.oversea.entity.medal.MedalConfigI18nEntity;
import com.playlet.oversea.entity.medal.UserMedalEntity;
import com.playlet.oversea.service.MedalApiService;
import com.playlet.oversea.service.MediaUrlService;
import com.playlet.oversea.utils.AppTokenUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * C端勋章管理实现
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class MedalApiServiceImpl implements MedalApiService {

	private static final String FALLBACK_LANGUE = "zh-cn";

	@Autowired
	private MedalConfigDao medalConfigDao;
	@Autowired
	private MedalConfigI18nDao medalConfigI18nDao;
	@Autowired
	private UserMedalDao userMedalDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase findMedalList(HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			String language = LanguageContext.getLanguage();
			List<MedalConfigEntity> list;
			if (uid == null) {
				list = medalConfigDao.selectLogoList(language);
				if (list == null) {
					list = new ArrayList<>();
				}
				list.forEach(item -> item.setLogo(mediaUrlService.sign(item.getIconLockedKey())));
			} else {
				list = medalConfigDao.selectLogoListByUid(uid, language);
				if (list == null) {
					list = new ArrayList<>();
				}
				list.forEach(item -> item.setLogo(mediaUrlService.sign(item.getLogo())));
			}
			return setResultSuccess(list, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase findMedalDetail(@RequestParam("id") Integer id, HttpServletRequest request) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		MedalConfigEntity config = medalConfigDao.selectById(id);
		if (config == null || (config.getIsDeleted() != null && config.getIsDeleted() == 1)) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		if (config.getStatus() != null && config.getStatus() == 0) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}

		Integer uid = AppTokenUtil.resolveUid(request);
		UserMedalEntity userMedal = null;
		if (uid != null) {
			userMedal = userMedalDao.findByUidAndMedalId(uid.longValue(), id);
		}

		boolean unlocked = userMedal != null && userMedal.getUnlocked() != null && userMedal.getUnlocked() == 1;
		MedalConfigI18nEntity i18n = resolveI18n(id);

		MedalDetailEntity detail = new MedalDetailEntity();
		detail.setMedalId(config.getId());
		detail.setMedalCode(config.getMedalCode());
		detail.setActionType(config.getActionType());
		detail.setSortWeight(config.getSortWeight());
		detail.setRewardCoin(config.getRewardCoin() == null ? 0 : config.getRewardCoin());
		if (i18n != null) {
			detail.setMedalName(i18n.getMedalName());
			detail.setSlogan(i18n.getSlogan());
			detail.setConditionText(i18n.getConditionText());
			detail.setShareTitle(i18n.getShareTitle());
			detail.setShareDesc(i18n.getShareDesc());
		}
		detail.setIconUrl(mediaUrlService.sign(unlocked ? config.getIconKey() : config.getIconLockedKey()));
		detail.setShareBgUrl(mediaUrlService.sign(config.getShareBgKey()));

		if (userMedal == null) {
			detail.setProgress(0);
			detail.setTargetCount(config.getTargetCount() == null ? 1 : config.getTargetCount());
			detail.setUnlocked(0);
			detail.setUnlockTime(null);
		} else {
			detail.setProgress(userMedal.getProgress() == null ? 0 : userMedal.getProgress());
			detail.setTargetCount(userMedal.getTargetCount() == null
					? (config.getTargetCount() == null ? 1 : config.getTargetCount())
					: userMedal.getTargetCount());
			detail.setUnlocked(unlocked ? 1 : 0);
			detail.setUnlockTime(unlocked ? userMedal.getUnlockTime() : null);
		}
		return setResultSuccess(detail, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase ackNotify(@RequestBody MedalAckNotifyRequest request, HttpServletRequest httpRequest) {
		Integer uid = AppTokenUtil.resolveUid(httpRequest);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (request == null || request.getMedalIds() == null || request.getMedalIds().isEmpty()) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		Set<Integer> medalIdSet = new LinkedHashSet<>();
		for (Integer medalId : request.getMedalIds()) {
			if (medalId != null && medalId > 0) {
				medalIdSet.add(medalId);
			}
		}
		if (medalIdSet.isEmpty()) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		List<Integer> medalIds = new ArrayList<>(medalIdSet);
		userMedalDao.markNotified(uid.longValue(), medalIds);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase pendingNotify(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		List<UserMedalEntity> pending = userMedalDao.findPendingNotify(uid.longValue());
		if (pending == null) {
			pending = new ArrayList<>();
		}
		List<MedalNotifyEntity> items = new ArrayList<>();
		for (UserMedalEntity row : pending) {
			MedalConfigEntity config = medalConfigDao.selectById(row.getMedalId());
			if (config == null || (config.getIsDeleted() != null && config.getIsDeleted() == 1)) {
				continue;
			}
			MedalConfigI18nEntity i18n = resolveI18n(row.getMedalId());
			MedalNotifyEntity item = new MedalNotifyEntity();
			item.setMedalId(row.getMedalId());
			item.setMedalCode(config.getMedalCode());
			item.setIconUrl(mediaUrlService.sign(config.getIconKey()));
			item.setUnlockTime(row.getUnlockTime());
			item.setRewardCoin(config.getRewardCoin() == null ? 0 : config.getRewardCoin());
			if (i18n != null) {
				item.setMedalName(i18n.getMedalName());
				item.setSlogan(i18n.getSlogan());
			}
			items.add(item);
		}
		return setResultSuccess(items, I18nUtil.getMessage("base_success"));
	}

	private MedalConfigI18nEntity resolveI18n(Integer medalId) {
		String language = LanguageContext.getLanguage();
		MedalConfigI18nEntity i18n = null;
		if (!StringUtils.isEmpty(language)) {
			i18n = medalConfigI18nDao.findByMedalIdAndLangue(medalId, language);
		}
		if (i18n == null && !FALLBACK_LANGUE.equalsIgnoreCase(language)) {
			i18n = medalConfigI18nDao.findByMedalIdAndLangue(medalId, FALLBACK_LANGUE);
		}
		return i18n;
	}
}
