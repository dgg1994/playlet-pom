package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.TheaterHomeRespEntity;
import com.playlet.internal.api.response.TheaterRankBlockEntity;
import com.playlet.internal.api.response.TheaterRankPageRespEntity;
import com.playlet.internal.api.response.TheaterSearchHistoryRespEntity;
import com.playlet.internal.api.response.TheaterSearchItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.RankBoardDao;
import com.playlet.internal.dao.drama.RankListDao;
import com.playlet.internal.dao.drama.TagDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.RankBoardEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import com.playlet.internal.entity.drama.TagEntity;
import com.playlet.internal.service.TheaterService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.RedisUtil;
import com.playlet.internal.utils.StringUtils;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
public class TheaterServiceImpl extends BaseApiService implements TheaterService {

	private static final int KEYWORD_MAX_LEN = 50;
	private static final int HISTORY_MAX = 20;
	/** 搜索历史 TTL：90 天 */
	private static final long HISTORY_TTL_SEC = 90L * 24 * 60 * 60;
	private static final String HISTORY_KEY_UID = "theater:search:hist:uid:";

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
	private AppAccountDao appAccountDao;

	@Override
	public ResponseBase home() {
		TheaterHomeRespEntity resp = new TheaterHomeRespEntity();
		List<RankBoardEntity> boards = rankBoardDao.findEnabledList();
		if (boards != null) {
			for (RankBoardEntity board : boards) {
				List<RankListEntity> all = rankListDao.findEnabledByBoardCode(board.getBoardCode());
				int limit = board.getTopN() == null ? 10 : Math.min(10, board.getTopN());
				List<RankListEntity> preview = new ArrayList<>();
				if (all != null) {
					for (int i = 0; i < all.size() && i < limit; i++) {
						preview.add(all.get(i));
					}
				}
				TheaterRankBlockEntity block = new TheaterRankBlockEntity();
				block.setBoardCode(board.getBoardCode());
				block.setBoardName(board.getBoardName());
				block.setBoardType(board.getBoardType());
				block.setItems(preview);
				resp.getBlocks().add(block);
			}
		}
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase rankList() {
		List<RankBoardEntity> rankBoardEntities = rankBoardDao.selectList(null);
		return setResultSuccess(rankBoardEntities, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase rank(@RequestParam(required = false) String boardCode,
			RankListEntity entity) {
		if (StringUtils.isEmpty(boardCode)) {
			return setResultError("boardCode 不能为空");
		}
		boardCode = boardCode.trim().toUpperCase();
		RankBoardEntity board = rankBoardDao.findByBoardCode(boardCode);
		if (board == null || board.getStatus() == null || board.getStatus() != 1) {
			return setResultError("榜单不存在或已停用");
		}
		if (entity == null) {
			entity = new RankListEntity();
		}
		entity.setBoardCode(boardCode);
		entity.setStatus(1);
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<RankListEntity> list = rankListDao.findAdminList(entity);
		PageInfo<RankListEntity> info = new PageInfo<>(list);
		TheaterRankPageRespEntity resp = new TheaterRankPageRespEntity();
		resp.setBoardCode(board.getBoardCode());
		resp.setBoardName(board.getBoardName());
		resp.setPage(info);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase search(@RequestParam String keyword, DramaEntity entity,
			HttpServletRequest request) {
		if (StringUtils.isEmpty(keyword) || StringUtils.isEmpty(keyword.trim())) {
			return setResultError("请输入关键词");
		}
		keyword = keyword.trim();
		if (keyword.length() > KEYWORD_MAX_LEN) {
			keyword = keyword.substring(0, KEYWORD_MAX_LEN);
		}
		if (entity == null) {
			entity = new DramaEntity();
		}

		List<DramaEntity> dramaEntities = dramaDao.searchOnline(keyword);
		if (dramaEntities == null) {
			dramaEntities = new ArrayList<>();
		}
		List<DramaEntity> pageDramas = GenericityUtil.Page(dramaEntities, entity.getPageNumber(), entity.getPageSize());

		List<TheaterSearchItemEntity> items = new ArrayList<>();
		for (DramaEntity d : pageDramas) {
			items.add(toSearchItem(d));
		}

		PageInfo<TheaterSearchItemEntity> page = new PageInfo<>(items);
		page.setTotal(dramaEntities.size());
		// 保存搜索历史
		saveSearchHistory(request, keyword);
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	private TheaterSearchItemEntity toSearchItem(DramaEntity d) {
		TheaterSearchItemEntity item = new TheaterSearchItemEntity();
		item.setDramaId(d.getId());
		item.setTitle(d.getDramaTitle());
		item.setCoverUrl(d.getCoverUrl());
		item.setHotScore(d.getHotScore());
		item.setHotScoreText(d.getHotScoreText());
		item.setTotalEpisodes(d.getTotalEpisodes());
		item.setFinished(d.getFinishedState());
		item.setDescription(d.getDescriptionInfo());
		List<TagEntity> tags = tagDao.findByDramaId(d.getId());
		if (tags != null) {
			item.setTags(tags);
		}
		return item;
	}

	@Override
	public ResponseBase searchHistory(HttpServletRequest request) {
		String key = historyRedisKey(request);
		if (key == null) {
			return setResultError("请登录");
		}
		TheaterSearchHistoryRespEntity resp = new TheaterSearchHistoryRespEntity();
		List<Object> raw = redisUtil.lGet(key, 0, HISTORY_MAX - 1);
		if (raw != null) {
			for (Object o : raw) {
				if (o != null && StringUtils.isNotEmpty(o.toString())) {
					resp.getKeywords().add(o.toString());
				}
			}
		}
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase deleteSearchHistory(@RequestParam String keyword, HttpServletRequest request) {
		String key = historyRedisKey(request);
		if (key == null) {
			return setResultError("请登录");
		}
		if (StringUtils.isEmpty(keyword) || StringUtils.isEmpty(keyword.trim())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		redisUtil.lRemove(key, 0, keyword.trim());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase clearSearchHistory(HttpServletRequest request) {
		String key = historyRedisKey(request);
		if (key == null) {
			return setResultError("请登录");
		}
		redisUtil.del(key);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
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
		String uid = resolveUid(request);
		if (StringUtils.isNotEmpty(uid)) {
			return HISTORY_KEY_UID + uid;
		}
		return null;
	}

	private String resolveUid(HttpServletRequest request) {
		String header = request.getHeader(Constants.HEADER_AUTH);
		if (StringUtils.isEmpty(header) || !header.startsWith(Constants.AUTH_HEADER_START_WITH)) {
			return null;
		}
		try {
			String subject = Jwts.parser()
					.setSigningKey(Constants.SIGNING_KEY)
					.parseClaimsJws(header.replace(Constants.AUTH_HEADER_START_WITH, ""))
					.getBody()
					.getSubject();
			if (StringUtils.isEmpty(subject)) {
				return null;
			}
			AppAccountEntity byUid = appAccountDao.findByUid(subject);
			if (byUid != null) {
				return byUid.getUid();
			}
			AppAccountEntity byAccount = appAccountDao.findByAccount(subject);
			if (byAccount != null) {
				return byAccount.getUid();
			}
			return subject;
		} catch (Exception e) {
			log.debug("resolveUid skip: {}", e.getMessage());
			return null;
		}
	}
}
