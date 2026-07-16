package com.playlet.internal.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.TheaterHomeRespEntity;
import com.playlet.internal.api.response.TheaterRankBlockEntity;
import com.playlet.internal.api.response.TheaterRankPageRespEntity;
import com.playlet.internal.api.response.TheaterSearchHistoryRespEntity;
import com.playlet.internal.api.response.TheaterSearchItemEntity;
import com.playlet.internal.api.response.TheaterWatchHistoryItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.RankBoardDao;
import com.playlet.internal.dao.drama.RankListDao;
import com.playlet.internal.dao.drama.TagDao;
import com.playlet.internal.dao.drama.UserWatchHistoryDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.RankBoardEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import com.playlet.internal.entity.drama.TagEntity;
import com.playlet.internal.entity.drama.UserWatchHistoryEntity;
import com.playlet.internal.service.TheaterService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.RedisUtil;
import com.playlet.internal.utils.StringUtils;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playlet.internal.constants.RedisKeyConstants.*;

@Slf4j
@RestController
@CrossOrigin
public class TheaterServiceImpl extends BaseApiService implements TheaterService {

	@Autowired
	private RankBoardDao rankBoardDao;
	@Autowired
	private RankListDao rankListDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private TagDao tagDao;
	@Autowired
	private UserWatchHistoryDao userWatchHistoryDao;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private AppAccountDao appAccountDao;

	@Override
	public ResponseBase home() {
		TheaterHomeRespEntity resp = new TheaterHomeRespEntity();
		List<RankBoardEntity> boards = rankBoardDao.findEnabledList(LanguageContext.getLanguage());
		if (boards != null) {
			for (RankBoardEntity board : boards) {
				List<RankListEntity> all = rankListDao.findEnabledByBoardGroupId(board.getGroupId());
				int limit = board.getTopN() == null ? 10 : Math.min(10, board.getTopN());
				List<RankListEntity> preview = new ArrayList<>();
				if (all != null) {
					for (int i = 0; i < all.size() && i < limit; i++) {
						preview.add(all.get(i));
					}
				}
				TheaterRankBlockEntity block = new TheaterRankBlockEntity();
				block.setGroupId(board.getGroupId());
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
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<RankListEntity> list = rankListDao.findAdminList(entity);
		PageInfo<RankListEntity> info = new PageInfo<>(list);
		TheaterRankPageRespEntity resp = new TheaterRankPageRespEntity();
		resp.setGroupId(board.getGroupId());
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
			return setResultError(I18nUtil.getMessage("login_required"));
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
			return setResultError(I18nUtil.getMessage("login_required"));
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
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		redisUtil.del(key);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase reportWatch(@RequestBody UserWatchHistoryEntity entity, HttpServletRequest request) {
        try {
            String uid = resolveUid(request);
            if (StringUtils.isEmpty(uid)) {
                return setResultError(I18nUtil.getMessage("login_required"));
            }
            if (entity == null || StringUtils.isEmpty(entity.getDramaId())) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            String dramaId = entity.getDramaId().trim();
            DramaEntity drama = dramaDao.findByDramaId(Integer.valueOf(dramaId));
            if (drama == null) {
                return setResultError(I18nUtil.getMessage("drama_null"));
            }

            UserWatchHistoryEntity row = new UserWatchHistoryEntity();
            row.setUid(uid);
            row.setDramaId(dramaId);
            row.setEpisodeId(StringUtils.isEmpty(entity.getEpisodeId()) ? null : entity.getEpisodeId().trim());
            row.setEpisodeNo(entity.getEpisodeNo());
            row.setWatchProgress(entity.getWatchProgress() == null ? 0 : Math.max(0, entity.getWatchProgress()));
            GenericityUtil.setDate(row);
            userWatchHistoryDao.upsert(row);

            cacheWatchAfterWrite(uid, dramaId, row);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
			throw new RuntimeException(e);
        }
    }

	@Override
	public ResponseBase watchHistory(UserWatchHistoryEntity entity, HttpServletRequest request) {
		String uid = resolveUid(request);
		if (StringUtils.isEmpty(uid)) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (entity == null) {
			entity = new UserWatchHistoryEntity();
		}
		List<UserWatchHistoryEntity> rows = userWatchHistoryDao.findByUid(uid);
		if (rows == null) {
			rows = new ArrayList<>();
		}
		List<UserWatchHistoryEntity> pageRows = GenericityUtil.Page(rows, entity.getPageNumber(), entity.getPageSize());
		List<TheaterWatchHistoryItemEntity> items = new ArrayList<>();
		for (UserWatchHistoryEntity row : pageRows) {
			TheaterWatchHistoryItemEntity item = toWatchItem(row);
			if (item != null) {
				items.add(item);
			}
		}
		PageInfo<TheaterWatchHistoryItemEntity> page = new PageInfo<>(items);
		page.setTotal(rows.size());
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase deleteWatchHistory(@RequestParam Integer dramaId, HttpServletRequest request) {
        try {
            String uid = resolveUid(request);
            if (StringUtils.isEmpty(uid)) {
                return setResultError(I18nUtil.getMessage("login_required"));
            }
            if (dramaId == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            userWatchHistoryDao.deleteByUidAndDrama(uid, dramaId);
			// Redis 写入时 dramaId 为 String，删除必须同类型，否则 ClassCastException
			String dramaIdStr = String.valueOf(dramaId);
            redisUtil.lRemove(VIEW_LIST_KEY + uid, 0, dramaIdStr);
            redisUtil.hdel(VIEW_META_KEY + uid, dramaIdStr);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public ResponseBase clearWatchHistory(HttpServletRequest request) {
		String uid = resolveUid(request);
		if (StringUtils.isEmpty(uid)) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		userWatchHistoryDao.deleteByUid(uid);
		redisUtil.del(VIEW_LIST_KEY + uid, VIEW_META_KEY + uid, VIEW_EMPTY_KEY + uid);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** 写 MySQL 后同步 Redis：List 保序 + Hash 存进度 */
	private void cacheWatchAfterWrite(String uid, String dramaId, UserWatchHistoryEntity row) {
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
			redisUtil.hset(metaKey, dramaId, toMetaJson(row));
			redisUtil.expire(listKey, VIEW_HISTORY_TTL_SEC);
			redisUtil.expire(metaKey, VIEW_HISTORY_TTL_SEC);
		} catch (Exception e) {
			log.warn("cacheWatchAfterWrite failed: {}", e.getMessage());
		}
	}

	private void rebuildViewCache(String uid) {
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
				if (row == null || StringUtils.isEmpty(row.getDramaId())) {
					continue;
				}
				redisUtil.lLeftPush(listKey, row.getDramaId());
				redisUtil.hset(metaKey, row.getDramaId(), toMetaJson(row));
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
		meta.put("episodeNo", row.getEpisodeNo());
		meta.put("watchProgress", row.getWatchProgress() == null ? 0 : row.getWatchProgress());
		meta.put("gmtModified", row.getGmtModified() == null ? null : row.getGmtModified().getTime());
		return meta.toJSONString();
	}

	private TheaterWatchHistoryItemEntity toWatchItem(UserWatchHistoryEntity row) {
		if (row == null || StringUtils.isEmpty(row.getDramaId())) {
			return null;
		}
		DramaEntity drama = dramaDao.findByDramaId(Integer.valueOf(row.getDramaId()));
		if (drama == null) {
			return null;
		}
		TheaterWatchHistoryItemEntity item = new TheaterWatchHistoryItemEntity();
		item.setDramaId(row.getDramaId());
		item.setTitle(drama.getDramaTitle());
		item.setCoverUrl(drama.getCoverUrl());
		item.setTotalEpisodes(drama.getTotalEpisodes());
		item.setFinished(drama.getFinishedState());
		item.setEpisodeId(row.getEpisodeId());
		item.setEpisodeNo(row.getEpisodeNo());
		item.setWatchProgress(row.getWatchProgress() == null ? 0 : row.getWatchProgress());
		item.setGmtModified(row.getGmtModified());
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
