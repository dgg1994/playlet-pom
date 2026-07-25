package com.playlet.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.RankListItemEntity;
import com.playlet.internal.api.response.TheaterHomeRespEntity;
import com.playlet.internal.api.response.TheaterRankBlockEntity;
import com.playlet.internal.api.response.TheaterRankPageRespEntity;
import com.playlet.internal.api.response.TheaterSearchItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.RankBoardDao;
import com.playlet.internal.dao.drama.RankListDao;
import com.playlet.internal.dao.drama.TagDao;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.RankBoardEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import com.playlet.internal.entity.drama.TagEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.RecommendedCarouselEnums;
import com.playlet.internal.enums.VerifyStateEnums;
import com.playlet.internal.service.TheaterApiService;
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

	private static final int HOME_RANK_PREVIEW_MAX = 10;

	@Override
	public ResponseBase home() {
		TheaterHomeRespEntity resp = new TheaterHomeRespEntity();
		String langue = LanguageContext.getLanguage();

		// 轮播
		List<DramaEntity> carouselDramas = dramaDao.selectList(new QueryWrapper<DramaEntity>()
				.eq("delete_state", DeleteStateEnum.NORMAL.getIndex())
				.eq("recommended_carousel", RecommendedCarouselEnums.RECOMMENDED.getIndex())
				.eq("verify_status", VerifyStateEnums.AVAILABLE_NOW.getIndex())
				.last("limit 5"));
		resp.setCarousels(carouselDramas);

		List<RankBoardEntity> boards = rankBoardDao.findEnabledList(langue);
		if (boards == null) {
			boards = Collections.emptyList();
		}
		Integer verifyStatus = VerifyStateEnums.AVAILABLE_NOW.getIndex();
		Integer deleteState = DeleteStateEnum.NORMAL.getIndex();
		for (RankBoardEntity board : boards) {
			int limit = board.getTopN() == null ? HOME_RANK_PREVIEW_MAX
					: Math.min(HOME_RANK_PREVIEW_MAX, board.getTopN());
			List<RankListItemEntity> preview = rankListDao.findEnabledWithDramaLimit(
					board.getGroupId(), verifyStatus, deleteState, limit);
			if (preview == null) {
				preview = Collections.emptyList();
			}
			TheaterRankBlockEntity block = new TheaterRankBlockEntity();
			block.setGroupId(board.getGroupId());
			block.setBoardName(board.getBoardName());
			block.setBoardType(board.getBoardType());
			block.setItems(preview);
			resp.getBlocks().add(block);
		}
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
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
	public ResponseBase rank(@RequestParam(required = false) String groupId,
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
		}
		List<DramaEntity> page = GenericityUtil.Page(dramaEntities, entity.getPageNumber(), entity.getPageSize());
		PageInfo<DramaEntity> dramaEntityPageInfo = PageInfo.of(page);
		dramaEntityPageInfo.setTotal(dramaEntities.size());
		TheaterRankPageRespEntity resp = new TheaterRankPageRespEntity();
		resp.setGroupId(board.getGroupId());
		resp.setBoardName(board.getBoardName());
		resp.setPage(dramaEntityPageInfo);
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
		// 关联表存的是 tag_group_id，前端常传标签主键 tagId，先解析成 groupId
		if (StringUtils.isEmpty(entity.getTagGroupId()) && entity.getTagId() != null) {
			TagEntity tag = tagDao.selectById(entity.getTagId());
			if (tag == null || StringUtils.isEmpty(tag.getGroupId())) {
				PageInfo<TheaterSearchItemEntity> empty = new PageInfo<>(new ArrayList<>());
				empty.setTotal(0);
				return setResultSuccess(empty, I18nUtil.getMessage("base_success"));
			}
			entity.setTagGroupId(tag.getGroupId());
		}
		List<DramaEntity> dramaEntities = dramaDao.searchOnline(entity);
		if (dramaEntities == null) {
			dramaEntities = new ArrayList<>();
		}
		log.info("theater search title={}, tagId={}, tagGroupId={}, hit={}",
				entity.getDramaTitle(), entity.getTagId(), entity.getTagGroupId(), dramaEntities.size());
		List<DramaEntity> pageDramas = GenericityUtil.Page(dramaEntities, entity.getPageNumber(), entity.getPageSize());

		String langue = LanguageContext.getLanguage();
		List<TheaterSearchItemEntity> items = new ArrayList<>();
		for (DramaEntity d : pageDramas) {
			items.add(toSearchItem(d, langue));
		}

		PageInfo<TheaterSearchItemEntity> page = new PageInfo<>(items);
		page.setTotal(dramaEntities.size());
		if (StringUtils.isNotEmpty(entity.getDramaTitle())) {
			saveSearchHistory(request, entity.getDramaTitle());
		}
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	private TheaterSearchItemEntity toSearchItem(DramaEntity d, String langue) {
		TheaterSearchItemEntity item = new TheaterSearchItemEntity();
		item.setDramaId(d.getId());
		item.setTitle(d.getDramaTitle());
		item.setCoverUrl(d.getCoverUrl());
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
