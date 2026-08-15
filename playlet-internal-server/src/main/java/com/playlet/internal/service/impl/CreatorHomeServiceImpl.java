package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.CreatorHomeFeedRespEntity;
import com.playlet.internal.api.response.CreatorHomeHotDramaRespEntity;
import com.playlet.internal.api.response.CreatorHomeHotTagAggRow;
import com.playlet.internal.api.response.CreatorHomeHotTagRespEntity;
import com.playlet.internal.api.response.CreatorHomeNoticeRespEntity;
import com.playlet.internal.api.response.CreatorHomeRankAggRow;
import com.playlet.internal.api.response.CreatorHomeRankItemRespEntity;
import com.playlet.internal.api.response.CreatorHomeStatsRespEntity;
import com.playlet.internal.api.response.DramaRankAggRow;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.constants.RankBoardGroupConstants;
import com.playlet.internal.dao.creator.CreatorCoinLedgerDao;
import com.playlet.internal.dao.creator.CreatorHomeDao;
import com.playlet.internal.dao.drama.DramaRankStatDailyDao;
import com.playlet.internal.dao.message.SystemMessagePublishDao;
import com.playlet.internal.dao.message.SystemMessagePublishI18nDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.message.SystemMessagePublishEntity;
import com.playlet.internal.entity.message.SystemMessagePublishI18nEntity;
import com.playlet.internal.enums.CreatorHomeRankTypeEnums;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.service.CreatorHomeService;
import com.playlet.internal.service.MediaUrlService;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final ZoneId ZONE = ZoneId.of(RankBoardGroupConstants.TIMEZONE);

	@Autowired
	private CreatorHomeDao creatorHomeDao;
	@Autowired
	private CreatorCoinLedgerDao creatorCoinLedgerDao;
	@Autowired
	private DramaRankStatDailyDao dramaRankStatDailyDao;
	@Autowired
	private SystemMessagePublishDao systemMessagePublishDao;
	@Autowired
	private SystemMessagePublishI18nDao systemMessagePublishI18nDao;
	@Autowired
	private MediaUrlService mediaUrlService;

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
		CreatorHomeFeedRespEntity resp = new CreatorHomeFeedRespEntity();
		resp.setHotDramas(buildHotDramas());
		resp.setHotTags(buildHotTags());
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
		List<CreatorHomeRankItemRespEntity> list = buildRank(rankType);
		Map<String, Object> data = new HashMap<>(4);
		data.put("rankType", rankType.getCode());
		data.put("rankList", list);
		log.info("creator home rank creatorId={} type={} size={}", account.getId(), rankType.getCode(), list.size());
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	/** 顶部概览：流水日收益 + 账号余额/累计 + 日播放 + 在播 */
	private CreatorHomeStatsRespEntity buildStats(CreatorAccountEntity account) {
		LocalDate today = LocalDate.now(ZONE);
		String todayStr = today.format(DATE_FMT);
		String yesterdayStr = today.minusDays(1).format(DATE_FMT);

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
		stats.setTodayIncomeYuan(coinToYuan(todayIncomeCoin));
		stats.setYesterdayIncomeYuan(coinToYuan(yesterdayIncomeCoin));
		stats.setBalanceYuan(coinToYuan(available));
		stats.setTotalIncomeYuan(coinToYuan(totalIncome));
		stats.setTodayPlayCount(todayPlay == null ? 0L : todayPlay);
		stats.setYesterdayPlayCount(yesterdayPlay == null ? 0L : yesterdayPlay);
		stats.setOnAirDramaCount(onAirDrama == null ? 0 : onAirDrama);
		stats.setOnAirEpisodeCount(onAirEpisode == null ? 0 : onAirEpisode);
		return stats;
	}

	/** 全站近期热点剧（与 C 端热播同源） */
	private List<CreatorHomeHotDramaRespEntity> buildHotDramas() {
		String fromDate = windowFromDate(CreatorConstants.INFLUENCE_WINDOW_DAYS);
		List<DramaRankAggRow> rows = dramaRankStatDailyDao.findHotPlayCandidates(fromDate, null,
				CreatorConstants.HOME_HOT_DRAMA_LIMIT);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<CreatorHomeHotDramaRespEntity> list = new ArrayList<>(rows.size());
		for (DramaRankAggRow row : rows) {
			CreatorHomeHotDramaRespEntity item = new CreatorHomeHotDramaRespEntity();
			item.setDramaId(row.getDramaId());
			item.setDramaTitle(row.getDramaTitle());
			item.setCoverUrl(mediaUrlService.sign(row.getCoverUrl()));
			list.add(item);
		}
		return list;
	}

	/** 近窗有播放剧的标签气泡；命中数达阈值打火 */
	private List<CreatorHomeHotTagRespEntity> buildHotTags() {
		String langue = resolveLangue();
		String fromDate = windowFromDate(CreatorConstants.INFLUENCE_WINDOW_DAYS);
		List<CreatorHomeHotTagAggRow> rows = creatorHomeDao.findHotTags(fromDate, langue,
				CreatorConstants.HOME_HOT_TAG_LIMIT);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<CreatorHomeHotTagRespEntity> list = new ArrayList<>(rows.size());
		for (CreatorHomeHotTagAggRow row : rows) {
			CreatorHomeHotTagRespEntity item = new CreatorHomeHotTagRespEntity();
			item.setGroupId(row.getGroupId());
			item.setTagName(row.getTagName());
			long hit = row.getHitCnt() == null ? 0L : row.getHitCnt();
			item.setHotFlag(hit >= CreatorConstants.HOT_TAG_FIRE_MIN_CNT ? 1 : 0);
			list.add(item);
		}
		return list;
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

	private List<CreatorHomeRankItemRespEntity> buildRank(CreatorHomeRankTypeEnums rankType) {
		List<CreatorHomeRankAggRow> rows;
		if (CreatorHomeRankTypeEnums.GROWTH.equals(rankType)) {
			rows = queryGrowthRows();
		} else {
			String fromDate = windowFromDate(CreatorConstants.INFLUENCE_WINDOW_DAYS);
			rows = creatorHomeDao.findInfluenceRank(fromDate, CreatorConstants.HOME_RANK_LIMIT);
		}
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<CreatorHomeRankItemRespEntity> list = new ArrayList<>(rows.size());
		int rankNo = 1;
		for (CreatorHomeRankAggRow row : rows) {
			CreatorHomeRankItemRespEntity item = new CreatorHomeRankItemRespEntity();
			item.setRankNo(rankNo++);
			item.setCreatorId(row.getCreatorId());
			item.setNickname(row.getNickname());
			item.setScore(row.getScore() == null ? 0L : row.getScore());
			list.add(item);
		}
		return list;
	}

	private List<CreatorHomeRankAggRow> queryGrowthRows() {
		LocalDate today = LocalDate.now(ZONE);
		int window = CreatorConstants.GROWTH_WINDOW_DAYS;
		String todayStr = today.format(DATE_FMT);
		String recentFrom = today.minusDays(window - 1L).format(DATE_FMT);
		String prevTo = today.minusDays(window).format(DATE_FMT);
		String prevFrom = today.minusDays(window * 2L - 1L).format(DATE_FMT);
		return creatorHomeDao.findGrowthRank(todayStr, recentFrom, prevFrom, prevTo,
				CreatorConstants.GROWTH_MIN_RECENT_SECONDS, CreatorConstants.HOME_RANK_LIMIT);
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

	private static BigDecimal coinToYuan(Long coin) {
		long value = coin == null ? 0L : coin;
		return BigDecimal.valueOf(value)
				.divide(BigDecimal.valueOf(CreatorConstants.COIN_PER_YUAN), 2, RoundingMode.HALF_UP);
	}

	private static String windowFromDate(int windowDays) {
		return LocalDate.now(ZONE).minusDays(Math.max(windowDays - 1, 0)).format(DATE_FMT);
	}

	private static String resolveLangue() {
		String langue = LanguageContext.getLanguage();
		if (StringUtils.isEmpty(langue)) {
			return LanguageEnums.DEFAULT_LANGUE;
		}
		return langue;
	}
}
