package com.playlet.internal.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.*;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.RankBoardGroupConstants;
import com.playlet.internal.constants.TheaterConstants;
import com.playlet.internal.dao.drama.*;
import com.playlet.internal.entity.drama.*;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PlaylistMatchModeEnums;
import com.playlet.internal.enums.RankBoardTypeEnums;
import com.playlet.internal.enums.RecommendedCarouselEnums;
import com.playlet.internal.enums.TheaterHomeTabEnums;
import com.playlet.internal.enums.VerifyStateEnums;
import com.playlet.internal.enums.WelfareActionTypeEnums;
import com.playlet.internal.query.drama.RecommendDramaQuery;
import com.playlet.internal.service.*;
import com.playlet.internal.service.support.UserActiveStatService;
import com.playlet.internal.service.support.WatchPreferMatchSupport;
import com.playlet.internal.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.playlet.internal.constants.RedisKeyConstants.*;

@Slf4j
@RestController
@CrossOrigin
public class TheaterApiServiceImpl extends BaseApiService implements TheaterApiService {

	@Autowired
	private RankBoardDao rankBoardDao;
	@Autowired
	private RankListDao rankListDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private TagDao tagDao;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private MediaUrlService mediaUrlService;
	@Autowired
	private UserWatchHistoryDao userWatchHistoryDao;
	@Autowired
	private DramaRankStatService dramaRankStatService;
	@Autowired
	private WelfareTaskService welfareTaskService;
	@Autowired
	private WatchGiftService watchGiftService;
	@Autowired
	private TheaterHomeCacheHelper theaterHomeCacheHelper;
	@Autowired
	private DramaAssetPlayStatService dramaAssetPlayStatService;
	@Autowired
	private UserActiveStatService userActiveStatService;
	@Autowired
	private WatchPreferMatchSupport watchPreferMatchSupport;
	@Autowired
	private DramaPlaylistDao dramaPlaylistDao;
	@Autowired
	private DramaPlaylistTagDao dramaPlaylistTagDao;

	@Override
	public ResponseBase home(@RequestParam(value = "tab", required = false) String tab,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "seed", required = false) String seed,
			HttpServletRequest request) {
		TheaterHomeTabEnums tabEnum = TheaterHomeTabEnums.ofCode(tab);
		if (tabEnum == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		String langue = LanguageContext.getLanguage();
		TheaterHomeRespEntity resp;
		if (TheaterHomeTabEnums.PLAYLIST.equals(tabEnum)) {
			resp = buildPlaylistHome(langue);
		} else if (TheaterHomeTabEnums.HOT.equals(tabEnum)) {
			resp = buildHotHome(langue);
		} else {
			resp = buildRecommendHome(pageNumber, pageSize, seed, request);
		}
		resp.setTab(tabEnum.getCode());
		if (resp.getPreferApplied() == null) {
			resp.setPreferApplied(Boolean.FALSE);
		}
		log.info("theater home tab={} preferApplied={}", tabEnum.getCode(), resp.getPreferApplied());
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/** 热榜：轮播 + 算法/人工榜预览（不含剧单、不含猜你喜欢）。 */
	private TheaterHomeRespEntity buildHotHome(String langue) {
		TheaterHomeRespEntity cached = theaterHomeCacheHelper.get(langue);
		if (cached != null) {
			return copyHome(cached);
		}
		TheaterHomeRespEntity resp = new TheaterHomeRespEntity();
		List<DramaEntity> carouselDramas = dramaDao.selectList(new QueryWrapper<DramaEntity>()
				.eq("delete_state", DeleteStateEnum.NORMAL.getIndex())
				.eq("recommended_carousel", RecommendedCarouselEnums.RECOMMENDED.getIndex())
				.eq("verify_status", VerifyStateEnums.AVAILABLE_NOW.getIndex())
				.last("limit " + TheaterConstants.HOME_CAROUSEL_LIMIT));
		if (carouselDramas != null) {
			for (DramaEntity drama : carouselDramas) {
				drama.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
			}
		}
		resp.setCarousels(carouselDramas);

		List<RankBoardEntity> boards = rankBoardDao.findEnabledList(langue);
		if (boards == null) {
			boards = Collections.emptyList();
		}
		Integer verifyStatus = VerifyStateEnums.AVAILABLE_NOW.getIndex();
		Integer deleteState = DeleteStateEnum.NORMAL.getIndex();
		for (RankBoardEntity board : boards) {
			if (RankBoardTypeEnums.isPlaylist(board.getBoardType())) {
				continue;
			}
			int limit = previewLimit(board.getTopN());
			List<RankListItemEntity> preview = rankListDao.findEnabledWithDramaLimit(
					board.getGroupId(), verifyStatus, deleteState, limit);
			if (preview == null) {
				preview = Collections.emptyList();
			}
			for (RankListItemEntity item : preview) {
				item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
			}
			resp.getBlocks().add(toBlock(board, preview));
		}
		theaterHomeCacheHelper.put(langue, resp);
		return copyHome(resp);
	}

	/** 剧单：读 drama_playlist + drama_playlist_tag，按 match_mode 捞在架剧。 */
	private TheaterHomeRespEntity buildPlaylistHome(String langue) {
		TheaterHomeRespEntity resp = new TheaterHomeRespEntity();
		List<DramaPlaylistEntity> playlists = dramaPlaylistDao.findEnabledByLangue(langue);
		if (playlists == null || playlists.isEmpty()) {
			return resp;
		}
		for (DramaPlaylistEntity playlist : playlists) {
			List<String> tagGroupIds = dramaPlaylistTagDao.findTagGroupIds(playlist.getGroupId());
			List<DramaEntity> dramas = Collections.emptyList();
			if (tagGroupIds != null && !tagGroupIds.isEmpty()) {
				int limit = previewLimit(playlist.getTopN());
				// AND 须全部命中；默认 OR 命中任一标签
				if (PlaylistMatchModeEnums.isAnd(playlist.getMatchMode())) {
					dramas = dramaDao.findOnlineByAllTags(tagGroupIds, limit);
				} else {
					dramas = dramaDao.findOnlineByPreferTags(tagGroupIds, limit);
				}
			}
			resp.getBlocks().add(toPlaylistBlock(playlist, toRankItems(playlist.getGroupId(), dramas)));
		}
		return resp;
	}

	/** 推荐：有偏好映射则命中优先，否则 seed 打散。 */
	private TheaterHomeRespEntity buildRecommendHome(Integer pageNumber, Integer pageSize, String seed,
			HttpServletRequest request) {
		RecommendDramaQuery query = new RecommendDramaQuery();
		query.setPageNumber(pageNumber == null ? Constants.PAGENUMBER : pageNumber);
		query.setPageSize(pageSize == null ? Constants.PAGESIZE : pageSize);
		query.setSeed(seed);
		ensureRecommendSeed(query);
		query.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
		query.setVerifyStatus(VerifyStateEnums.AVAILABLE_NOW.getIndex());

		Integer uid = AppTokenUtil.resolveUid(request);
		List<String> preferTagGroupIds = watchPreferMatchSupport.resolveDramaTagGroupIds(uid);
		boolean preferApplied = preferTagGroupIds != null && !preferTagGroupIds.isEmpty();
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<RecommendDramaRespEntity> list = dramaDao.recommendList(query, preferTagGroupIds);
		if (list != null) {
			for (RecommendDramaRespEntity item : list) {
				item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
			}
		}
		RecommendPageRespEntity feed = new RecommendPageRespEntity();
		feed.setSeed(query.getSeed());
		feed.setPage(new PageInfo<>(list == null ? Collections.emptyList() : list));

		TheaterHomeRespEntity resp = new TheaterHomeRespEntity();
		resp.setPreferApplied(preferApplied);
		resp.setFeed(feed);
		return resp;
	}

	private static void ensureRecommendSeed(RecommendDramaQuery entity) {
		String seed = entity.getSeed() == null ? null : entity.getSeed().trim();
		if (StringUtils.isEmpty(seed)) {
			entity.setSeed(UUID.randomUUID().toString().replace("-", ""));
			return;
		}
		if (seed.length() > 64) {
			seed = seed.substring(0, 64);
		}
		entity.setSeed(seed);
	}

	private static int previewLimit(Integer topN) {
		if (topN == null) {
			return TheaterConstants.HOME_RANK_PREVIEW_MAX;
		}
		return Math.min(TheaterConstants.HOME_RANK_PREVIEW_MAX, topN);
	}

	private static TheaterRankBlockEntity toBlock(RankBoardEntity board, List<RankListItemEntity> items) {
		TheaterRankBlockEntity block = new TheaterRankBlockEntity();
		block.setGroupId(board.getGroupId());
		block.setBoardName(board.getBoardName());
		block.setBoardType(board.getBoardType());
		block.setItems(items == null ? new ArrayList<RankListItemEntity>() : items);
		return block;
	}

	/** 剧单分块：boardType 仍回 3，兼容客户端既有枚举。 */
	private static TheaterRankBlockEntity toPlaylistBlock(DramaPlaylistEntity playlist,
			List<RankListItemEntity> items) {
		TheaterRankBlockEntity block = new TheaterRankBlockEntity();
		block.setGroupId(playlist.getGroupId());
		block.setBoardName(playlist.getPlaylistName());
		block.setBoardType(RankBoardTypeEnums.PLAYLIST.getIndex());
		block.setItems(items == null ? new ArrayList<RankListItemEntity>() : items);
		return block;
	}

	private List<RankListItemEntity> toRankItems(String boardGroupId, List<DramaEntity> dramas) {
		List<RankListItemEntity> items = new ArrayList<>();
		if (dramas == null) {
			return items;
		}
		int rankNo = 1;
		for (DramaEntity drama : dramas) {
			RankListItemEntity item = new RankListItemEntity();
			item.setBoardGroupId(boardGroupId);
			item.setRankNo(rankNo++);
			item.setDramaId(drama.getId() == null ? null : String.valueOf(drama.getId()));
			item.setTitle(drama.getDramaTitle());
			item.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
			item.setHotScore(drama.getHotScore());
			item.setHotScoreText(drama.getHotScoreText());
			item.setTotalEpisodes(drama.getTotalEpisodes());
			item.setFinished(drama.getFinishedState());
			item.setStatus(1);
			items.add(item);
		}
		return items;
	}

	/** 热榜缓存对象不可被调用方改 tab 字段污染。 */
	private static TheaterHomeRespEntity copyHome(TheaterHomeRespEntity src) {
		TheaterHomeRespEntity resp = new TheaterHomeRespEntity();
		resp.setCarousels(src.getCarousels());
		List<TheaterRankBlockEntity> blocks = new ArrayList<>();
		if (src.getBlocks() != null) {
			blocks.addAll(src.getBlocks());
		}
		resp.setBlocks(blocks);
		return resp;
	}

	@Override
	public ResponseBase boardFindList(@RequestBody RankBoardEntity entity) {
		if (entity == null) {
			entity = new RankBoardEntity();
		}
		if (StringUtils.isEmpty(entity.getLangue())) {
			entity.setLangue(LanguageContext.getLanguage());
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		return setResultSuccess(new PageInfo<>(rankBoardDao.findAdminList(entity)),
				I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase rankList() {
		List<RankBoardEntity> rankBoardEntities = rankBoardDao.findEnabledList(LanguageContext.getLanguage());
		return setResultSuccess(rankBoardEntities, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase rank(@RequestParam(value = "groupId", required = false) String groupId,
			RankListEntity entity) {
		if (StringUtils.isEmpty(groupId)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		groupId = groupId.trim();
		String langue = LanguageContext.getLanguage();
		RankBoardEntity board = rankBoardDao.findByGroupIdAndLangue(groupId, langue);
		if (board == null) {
			board = rankBoardDao.findOneByGroupId(groupId);
		}
		if (board == null || board.getStatus() == null || board.getStatus() != 1) {
			return setResultError(I18nUtil.getMessage("rank_board_null"));
		}
		if (entity == null) {
			entity = new RankListEntity();
		}
		entity.setBoardGroupId(groupId);
		entity.setStatus(1);
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<DramaEntity> dramaEntities = dramaDao.selectListDramas(
				groupId,
				VerifyStateEnums.AVAILABLE_NOW.getIndex(),
				DeleteStateEnum.NORMAL.getIndex());
		if (dramaEntities == null) {
			dramaEntities = new ArrayList<>();
		}
		for (DramaEntity dramaEntity : dramaEntities) {
			List<TagEntity> tagEntities = tagDao.selectListTagByDramaId(dramaEntity.getId(), langue);
			dramaEntity.setTagList(tagEntities);
			dramaEntity.setCoverUrl(mediaUrlService.sign(dramaEntity.getCoverUrl()));
		}
		PageInfo<DramaEntity> info = new PageInfo<>(dramaEntities);
		TheaterRankPageRespEntity resp = new TheaterRankPageRespEntity();
		resp.setGroupId(board.getGroupId());
		resp.setBoardName(board.getBoardName());
		resp.setPage(info);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/** 剧单二级页：与热榜 /rank 对称，按标签规则分页。 */
	@Override
	public ResponseBase playlist(@RequestParam(value = "groupId", required = false) String groupId,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		if (StringUtils.isEmpty(groupId)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		groupId = groupId.trim();
		String langue = LanguageContext.getLanguage();
		DramaPlaylistEntity playlist = dramaPlaylistDao.findEnabledByGroupIdAndLangue(groupId, langue);
		if (playlist == null) {
			playlist = dramaPlaylistDao.findOneEnabledByGroupId(groupId);
		}
		if (playlist == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		List<String> tagGroupIds = dramaPlaylistTagDao.findTagGroupIds(groupId);
		int pn = pageNumber == null || pageNumber < 1 ? Constants.PAGENUMBER : pageNumber;
		int ps = pageSize == null || pageSize < 1 ? Constants.PAGESIZE : Math.min(pageSize, Constants.MAX_PAGESIZE);

		List<DramaEntity> dramas;
		if (tagGroupIds == null || tagGroupIds.isEmpty()) {
			dramas = Collections.emptyList();
		} else {
			PageHelper.startPage(pn, ps);
			if (PlaylistMatchModeEnums.isAnd(playlist.getMatchMode())) {
				dramas = dramaDao.pageOnlineByAllTags(tagGroupIds);
			} else {
				dramas = dramaDao.pageOnlineByPreferTags(tagGroupIds);
			}
		}
		if (dramas == null) {
			dramas = Collections.emptyList();
		}
		for (DramaEntity drama : dramas) {
			List<TagEntity> tags = tagDao.selectListTagByDramaId(drama.getId(), langue);
			drama.setTagList(tags);
			drama.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
		}
		TheaterPlaylistPageRespEntity resp = new TheaterPlaylistPageRespEntity();
		resp.setGroupId(playlist.getGroupId());
		resp.setPlaylistName(playlist.getPlaylistName());
		resp.setMatchMode(playlist.getMatchMode());
		resp.setCoverUrl(mediaUrlService.sign(playlist.getCoverUrl()));
		resp.setPage(new PageInfo<>(dramas));
		log.info("theater playlist page groupId={} pageNumber={} size={}", groupId, pn, dramas.size());
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase search(@RequestBody DramaEntity entity, HttpServletRequest request) {
		if (entity == null) {
			entity = new DramaEntity();
		}
		if (StringUtils.isNotEmpty(entity.getDramaTitle())) {
			entity.setDramaTitle(entity.getDramaTitle().trim());
		}
		// 登录用户偏好标签：搜索 soft boost（无映射则与旧逻辑一致）
		Integer uid = AppTokenUtil.resolveUid(request);
		List<String> preferTagGroupIds = watchPreferMatchSupport.resolveDramaTagGroupIds(uid);
		// SQL 层分页，避免 GenericityUtil.Page 导致 hasNextPage/pages 元数据不准确
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<DramaEntity> dramaEntities = dramaDao.searchOnline(entity, preferTagGroupIds);
		if (dramaEntities == null) {
			dramaEntities = new ArrayList<>();
		}
		PageInfo<DramaEntity> basePage = new PageInfo<>(dramaEntities);
		log.info("theater search title={}, tagId={}, tagGroupId={}, preferTags={}, hit={}",
				entity.getDramaTitle(), entity.getTagId(), entity.getTagGroupId(),
				preferTagGroupIds.size(), dramaEntities.size());
		// 仅标题搜索首页计入热搜；标签筛选/翻页不记，避免刷榜
		if (StringUtils.isNotEmpty(entity.getDramaTitle()) && !dramaEntities.isEmpty()
				&& (entity.getPageNumber() == null || entity.getPageNumber() <= 1)) {
			pushRankSearchStat(dramaEntities);
		}

		String langue = LanguageContext.getLanguage();
		List<TheaterSearchItemEntity> items = new ArrayList<>();
		for (DramaEntity d : dramaEntities) {
			items.add(toSearchItem(d, langue));
		}

		PageInfo<TheaterSearchItemEntity> page = new PageInfo<>(items);
		// 继承 PageHelper 计算出来的分页元数据
		page.setTotal(basePage.getTotal());
		page.setPageNum(basePage.getPageNum());
		page.setPageSize(basePage.getPageSize());
		page.setPages(basePage.getPages());
		page.setHasNextPage(basePage.isHasNextPage());
		if (StringUtils.isNotEmpty(entity.getDramaTitle())) {
			saveSearchHistory(request, entity.getDramaTitle());
		}
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase reportWatch(@RequestBody UserWatchHistoryEntity entity, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			if (uid == null) {
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			if (entity == null || entity.getDramaId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			Integer dramaId = entity.getDramaId();
			DramaEntity drama = dramaDao.findByDramaId(dramaId);
			if (drama == null) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}

			// 添加观看历史
			UserWatchHistoryEntity row = new UserWatchHistoryEntity();
			row.setUid(uid);
			row.setDramaId(dramaId);
			row.setEpisodeId(StringUtils.isEmpty(entity.getEpisodeId()) ? null : entity.getEpisodeId().trim());
			row.setWatchProgress(entity.getWatchProgress() == null ? 0 : Math.max(0, entity.getWatchProgress()));
			GenericityUtil.setDate(row);
			userWatchHistoryDao.upsert(row);
			cacheWatchAfterWrite(uid, dramaId, row);

			// 观看上报
			int deltaSec = normalizeDeltaSeconds(entity.getDeltaSeconds());
			// 单集时长
			int episodeDurationSec = resolveEpisodeDurationSec(entity.getEpisodeProgress());

			pushWelfareWatch(uid, dramaId, row.getEpisodeId());
			if (deltaSec > 0) {
				// 观看礼
				pushWatchGift(uid, dramaId, row, deltaSec);
				// 任务
				pushWelfareWatch(uid, dramaId, row.getEpisodeId());
				// 热度
				incrHotScoreByWatch(dramaId, deltaSec, episodeDurationSec);
			}
			// 每次上报记 1 次 pv；有效秒数用裁剪后的 delta
			pushRankWatchStat(dramaId, deltaSec);
			// 作家端剧集曝光/完播（Redis 去重后异步落库）
			dramaAssetPlayStatService.onWatchReport(uid, row.getEpisodeId(), row.getWatchProgress(),
					entity.getEpisodeProgress());
			// 日活/播放时长（在线人数由 /api/appUser/heartbeat 单独维护）
			if (deltaSec > 0) {
				userActiveStatService.addPlaySeconds(uid, deltaSec);
			} else {
				userActiveStatService.markActive(uid);
			}

			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 上报有效秒数裁剪：小于 0 的视作 0。
	 */
	private int normalizeDeltaSeconds(Integer deltaSeconds) {
		if (deltaSeconds == null || deltaSeconds <= 0) {
			return 0;
		}
		return Math.min(deltaSeconds, TheaterConstants.MAX_DELTA_SEC_PER_REPORT);
	}

	/**
	 * 单集时长裁剪：小于 10 秒的视作 10 秒，大于 1 小时的视作 1 小时。
	 */
	private int resolveEpisodeDurationSec(Integer clientDurationSec) {
		if (clientDurationSec == null || clientDurationSec <= 0) {
			return TheaterConstants.DEFAULT_EPISODE_DURATION_SEC;
		}
		return Math.min(TheaterConstants.MAX_EPISODE_DURATION_SEC,
				Math.max(TheaterConstants.MIN_EPISODE_DURATION_SEC, clientDurationSec));
	}

	/**
	 * 热度：每有效观看「单集时长 / HOT_SCORE_DURATION_DIVISOR」+1。
	 * 热度增量 add = floor(本次有效观看秒数 × 3 ÷ 单集总时长)
	 * 每看满「单集时长的 1/3」，热度 +1。
	 */
	private void incrHotScoreByWatch(Integer dramaId, int deltaSec, int episodeDurationSec) {
		if (dramaId == null || deltaSec <= 0 || episodeDurationSec <= 0) {
			return;
		}
		long add = (long) deltaSec * TheaterConstants.HOT_SCORE_DURATION_DIVISOR / episodeDurationSec;
		if (add <= 0) {
			return;
		}
		try {
			dramaDao.incrHotScore(dramaId, add);
		} catch (Exception e) {
			log.warn("incr hot score failed dramaId={} delta={} duration={}: {}",
					dramaId, deltaSec, episodeDurationSec, e.getMessage());
		}
	}

	/**
	 * 观看福利：每有效观看「单集时长」+1。
	 * @param uid 用户 uid
	 * @param dramaId dramaId
	 * @param episodeId episodeId
	 */
	private void pushWelfareWatch(Integer uid, Integer dramaId, String episodeId) {
		if (StringUtils.isEmpty(episodeId)) {
			return;
		}
		try {
			JSONObject ext = new JSONObject();
			ext.put("dramaId", dramaId);
			ext.put("episodeId", episodeId);
			welfareTaskService.onAction(uid, WelfareActionTypeEnums.WATCH, 1, ext.toJSONString());
		} catch (Exception e) {
			log.warn("welfare watch progress failed: {}", e.getMessage());
		}
	}

	/**
	 * 观看礼物：每有效观看「单集时长」+1。
	 */
	private void pushWatchGift(Integer uid, Integer dramaId, UserWatchHistoryEntity row, int deltaSec) {
		try {
			JSONObject ext = new JSONObject();
			ext.put("dramaId", dramaId);
			ext.put("episodeId", row.getEpisodeId());
			ext.put("watchProgress", row.getWatchProgress());
			watchGiftService.addWatchSeconds(uid, deltaSec, ext.toJSONString());
		} catch (Exception e) {
			log.warn("watch gift seconds failed: {}", e.getMessage());
		}
	}

	/**
	 * 观看排行榜：每有效观看「单集时长」+1。
	 */
	private void pushRankWatchStat(Integer dramaId, int deltaSec) {
		try {
			dramaRankStatService.onWatch(dramaId, Math.max(0, deltaSec));
		} catch (Exception e) {
			log.warn("rank stat watch failed: {}", e.getMessage());
		}
	}

	/** 标题搜索命中记 search_cnt，最多前 K 条 */
	private void pushRankSearchStat(List<DramaEntity> hits) {
		int cap = Math.min(hits.size(), RankBoardGroupConstants.HOT_SEARCH_HIT_CAP);
		for (int i = 0; i < cap; i++) {
			DramaEntity d = hits.get(i);
			if (d == null || d.getId() == null) {
				continue;
			}
			try {
				dramaRankStatService.onSearch(d.getId(), 1);
			} catch (Exception e) {
				log.warn("rank stat search failed dramaId={}: {}", d.getId(), e.getMessage());
			}
		}
	}

	/**
	 * 写 MySQL 后同步 Redis：List 保序 + Hash 存进度
	 */
	private void cacheWatchAfterWrite(Integer uid, Integer dramaId, UserWatchHistoryEntity row) {
		try {
			redisUtil.del(VIEW_EMPTY_KEY + uid);
			String listKey = VIEW_LIST_KEY + uid;
			String metaKey = VIEW_META_KEY + uid;
			if (!redisUtil.hasKey(listKey)) {
				rebuildViewCache(uid);
				return;
			}
			redisUtil.lRemove(listKey, 0, dramaId);
			redisUtil.lLeftPush(listKey, dramaId);
			redisUtil.lTrim(listKey, 0, VIEW_HISTORY_MAX - 1);
			redisUtil.hset(metaKey, dramaId.toString(), toMetaJson(row));
			redisUtil.expire(listKey, VIEW_HISTORY_TTL_SEC);
			redisUtil.expire(metaKey, VIEW_HISTORY_TTL_SEC);
		} catch (Exception e) {
			log.warn("cacheWatchAfterWrite failed: {}", e.getMessage());
		}
	}

	private void rebuildViewCache(Integer uid) {
		String listKey = VIEW_LIST_KEY + uid;
		String metaKey = VIEW_META_KEY + uid;
		String emptyKey = VIEW_EMPTY_KEY + uid;
		try {
			redisUtil.del(listKey, metaKey, emptyKey);
			List<UserWatchHistoryEntity> rows = userWatchHistoryDao.findByUidLimit(uid, VIEW_HISTORY_MAX);
			if (rows == null || rows.isEmpty()) {
				redisUtil.set(emptyKey, "1", 60);
				return;
			}
			// rows 已按 gmtModified desc；从旧到新 leftPush，最新在队头
			for (int i = rows.size() - 1; i >= 0; i--) {
				UserWatchHistoryEntity row = rows.get(i);
				if (row == null || row.getDramaId() == null) {
					continue;
				}
				redisUtil.lLeftPush(listKey, row.getDramaId());
				redisUtil.hset(metaKey, row.getDramaId().toString(), toMetaJson(row));
			}
			redisUtil.expire(listKey, VIEW_HISTORY_TTL_SEC);
			redisUtil.expire(metaKey, VIEW_HISTORY_TTL_SEC);
		} catch (Exception e) {
			log.warn("rebuildViewCache failed: {}", e.getMessage());
		}
	}

	private String toMetaJson(UserWatchHistoryEntity row) {
		JSONObject meta = new JSONObject();
		meta.put("episodeId", row.getEpisodeId());
		meta.put("watchProgress", row.getWatchProgress() == null ? 0 : row.getWatchProgress());
		meta.put("gmtModified", row.getGmtModified() == null ? null : row.getGmtModified().getTime());
		return meta.toJSONString();
	}

	private TheaterSearchItemEntity toSearchItem(DramaEntity d, String langue) {
		TheaterSearchItemEntity item = new TheaterSearchItemEntity();
		item.setDramaId(d.getId());
		item.setTitle(d.getDramaTitle());
		item.setCoverUrl(mediaUrlService.sign(d.getCoverUrl()));
		item.setHotScore(d.getHotScore());
		item.setHotScoreText(d.getHotScoreText());
		item.setTotalEpisodes(d.getTotalEpisodes());
		item.setFinished(d.getFinishedState());
		item.setDescription(d.getDescriptionInfo());
		List<TagEntity> tags;
		if (StringUtils.isNotEmpty(langue)) {
			tags = tagDao.findGroupLang(langue, d.getId());
		} else {
			tags = tagDao.findByDramaId(d.getId());
		}
		if (tags != null) {
			item.setTags(tags);
		}
		return item;
	}

	private void saveSearchHistory(HttpServletRequest request, String keyword) {
		String key = historyRedisKey(request);
		if (key == null || StringUtils.isEmpty(keyword)) {
			return;
		}
		try {
			redisUtil.lRemove(key, 0, keyword);
			redisUtil.lLeftPush(key, keyword);
			redisUtil.lTrim(key, 0, HISTORY_MAX - 1);
			redisUtil.expire(key, HISTORY_TTL_SEC);
		} catch (Exception e) {
			log.warn("saveSearchHistory failed: {}", e.getMessage());
		}
	}

	/** 仅登录用户：按 uid 存 Redis */
	private String historyRedisKey(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid != null) {
			return HISTORY_KEY_UID + uid;
		}
		return null;
	}
}
