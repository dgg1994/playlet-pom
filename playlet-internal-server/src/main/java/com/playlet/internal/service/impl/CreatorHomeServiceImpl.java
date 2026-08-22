package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.CreatorHomeFeedRespEntity;
import com.playlet.internal.api.response.CreatorHomeNoticeRespEntity;
import com.playlet.internal.api.response.CreatorHomeRankItemRespEntity;
import com.playlet.internal.api.response.CreatorHomeStatsRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.dao.creator.CreatorCoinLedgerDao;
import com.playlet.internal.dao.creator.CreatorHomeDao;
import com.playlet.internal.dao.message.SystemMessagePublishDao;
import com.playlet.internal.dao.message.SystemMessagePublishI18nDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.message.SystemMessagePublishEntity;
import com.playlet.internal.entity.message.SystemMessagePublishI18nEntity;
import com.playlet.internal.enums.CreatorHomeRankTypeEnums;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.service.CreatorHomeService;
import com.playlet.internal.utils.CreatorBizUtils;
import com.playlet.internal.utils.CreatorHomeFeedHelper;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作家首页：stats / feed / notices / rank。
 */
@Slf4j
@RestController
@CrossOrigin
public class CreatorHomeServiceImpl extends BaseApiService implements CreatorHomeService {

	@Autowired
	private CreatorHomeDao creatorHomeDao;
	@Autowired
	private CreatorCoinLedgerDao creatorCoinLedgerDao;
	@Autowired
	private SystemMessagePublishDao systemMessagePublishDao;
	@Autowired
	private SystemMessagePublishI18nDao systemMessagePublishI18nDao;
	@Autowired
	private CreatorHomeFeedHelper creatorHomeFeedHelper;

	@Override
	public ResponseBase stats(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorHomeStatsRespEntity stats = buildStats(account);
		log.info("creator home stats creatorId={}", account.getId());
		return setResultSuccess(stats, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase feed(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorHomeFeedRespEntity resp = creatorHomeFeedHelper.buildFeed();
		log.info("creator home feed creatorId={} dramas={} tags={}", account.getId(),
				resp.getHotDramas().size(), resp.getHotTags().size());
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase notices(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		List<CreatorHomeNoticeRespEntity> list = buildNotices();
		log.info("creator home notices creatorId={} size={}", account.getId(), list.size());
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase rank(@RequestParam(value = "type", required = false) Integer type,
			HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorHomeRankTypeEnums rankType = CreatorHomeRankTypeEnums.fromCode(type);
		if (rankType == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		List<CreatorHomeRankItemRespEntity> list = creatorHomeFeedHelper.buildRank(rankType);
		Map<String, Object> data = new HashMap<>(4);
		data.put("rankType", rankType.getCode());
		data.put("rankList", list);
		log.info("creator home rank creatorId={} type={} size={}", account.getId(), rankType.getCode(), list.size());
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	/** 顶部概览：流水日收益 + 账号余额/累计 + 日播放 + 在播 */
	private CreatorHomeStatsRespEntity buildStats(CreatorAccountEntity account) {
		LocalDate today = CreatorBizUtils.today();
		String todayStr = CreatorBizUtils.formatDate(today);
		String yesterdayStr = CreatorBizUtils.formatDate(today.minusDays(1));

		Long todayIncomeCoin = creatorCoinLedgerDao.sumPositiveIncomeByDate(account.getId(), todayStr);
		Long yesterdayIncomeCoin = creatorCoinLedgerDao.sumPositiveIncomeByDate(account.getId(), yesterdayStr);
		long coinBalance = account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		long frozen = account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
		long available = Math.max(coinBalance - frozen, 0L);
		long totalIncome = account.getTotalIncomeCoin() == null ? 0L : account.getTotalIncomeCoin();

		Long todayPlay = creatorHomeDao.sumPlayPvByCreatorAndDate(account.getId(), todayStr);
		Long yesterdayPlay = creatorHomeDao.sumPlayPvByCreatorAndDate(account.getId(), yesterdayStr);
		Integer onAirDrama = creatorHomeDao.countOnAirDrama(account.getId());
		Integer onAirEpisode = creatorHomeDao.countOnAirEpisode(account.getId());

		CreatorHomeStatsRespEntity stats = new CreatorHomeStatsRespEntity();
		// 字段名保持 *Yuan，数值直接为金币（不做比例兑换）
		stats.setTodayIncomeYuan(BigDecimal.valueOf(todayIncomeCoin == null ? 0L : todayIncomeCoin));
		stats.setYesterdayIncomeYuan(BigDecimal.valueOf(yesterdayIncomeCoin == null ? 0L : yesterdayIncomeCoin));
		stats.setBalanceYuan(BigDecimal.valueOf(available));
		stats.setTotalIncomeYuan(BigDecimal.valueOf(totalIncome));
		stats.setTodayPlayCount(todayPlay == null ? 0L : todayPlay);
		stats.setYesterdayPlayCount(yesterdayPlay == null ? 0L : yesterdayPlay);
		stats.setOnAirDramaCount(onAirDrama == null ? 0 : onAirDrama);
		stats.setOnAirEpisodeCount(onAirEpisode == null ? 0 : onAirEpisode);
		return stats;
	}

	/** 已发布广播公告摘要（一期复用 C 端 system_message） */
	private List<CreatorHomeNoticeRespEntity> buildNotices() {
		List<SystemMessagePublishEntity> publishes = systemMessagePublishDao.findActiveBroadcastList();
		if (publishes == null || publishes.isEmpty()) {
			return Collections.emptyList();
		}
		int limit = Math.min(publishes.size(), CreatorConstants.HOME_NOTICE_LIMIT);
		List<SystemMessagePublishEntity> slice = publishes.subList(0, limit);
		List<Long> ids = new ArrayList<>(slice.size());
		for (SystemMessagePublishEntity pub : slice) {
			ids.add(pub.getId());
		}
		Map<Long, SystemMessagePublishI18nEntity> i18nMap = loadNoticeI18n(ids);
		List<CreatorHomeNoticeRespEntity> list = new ArrayList<>(slice.size());
		for (SystemMessagePublishEntity pub : slice) {
			CreatorHomeNoticeRespEntity item = new CreatorHomeNoticeRespEntity();
			item.setId(pub.getId());
			item.setSetTime(pub.getSetTime());
			item.setTitle(resolveNoticeTitle(pub, i18nMap.get(pub.getId())));
			list.add(item);
		}
		return list;
	}

	private Map<Long, SystemMessagePublishI18nEntity> loadNoticeI18n(List<Long> publishIds) {
		List<SystemMessagePublishI18nEntity> i18nList = systemMessagePublishI18nDao.findByPublishIds(publishIds);
		if (i18nList == null || i18nList.isEmpty()) {
			return Collections.emptyMap();
		}
		String langue = resolveLangue();
		String fallback = LanguageEnums.DEFAULT_LANGUE;
		Map<Long, SystemMessagePublishI18nEntity> preferred = new HashMap<>();
		Map<Long, SystemMessagePublishI18nEntity> fallbackMap = new HashMap<>();
		for (SystemMessagePublishI18nEntity i18n : i18nList) {
			if (i18n == null || i18n.getPublishId() == null) {
				continue;
			}
			if (langue.equals(i18n.getLangue())) {
				preferred.put(i18n.getPublishId(), i18n);
			} else if (fallback.equals(i18n.getLangue())) {
				fallbackMap.put(i18n.getPublishId(), i18n);
			}
		}
		Map<Long, SystemMessagePublishI18nEntity> result = new HashMap<>(fallbackMap);
		result.putAll(preferred);
		return result;
	}

	private String resolveNoticeTitle(SystemMessagePublishEntity pub, SystemMessagePublishI18nEntity i18n) {
		if (i18n != null && StringUtils.isNotEmpty(i18n.getTitle())) {
			return i18n.getTitle();
		}
		if (pub != null && StringUtils.isNotEmpty(pub.getTitle())) {
			return pub.getTitle();
		}
		return "";
	}

	private static String resolveLangue() {
		String langue = LanguageContext.getLanguage();
		if (StringUtils.isEmpty(langue)) {
			return LanguageEnums.DEFAULT_LANGUE;
		}
		return langue;
	}
}
