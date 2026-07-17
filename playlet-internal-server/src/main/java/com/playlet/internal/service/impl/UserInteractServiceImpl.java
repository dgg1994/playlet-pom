package com.playlet.internal.service.impl;

import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.TheaterCollectItemEntity;
import com.playlet.internal.api.response.TheaterLikeItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.UserDramaCollectDao;
import com.playlet.internal.dao.drama.UserDramaLikeDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.UserDramaCollectEntity;
import com.playlet.internal.entity.drama.UserDramaLikeEntity;
import com.playlet.internal.service.UserInteractService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.RedisUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.constants.RedisKeyConstants.COLLECT_SET_UID;
import static com.playlet.internal.constants.RedisKeyConstants.INTERACT_TTL_SEC;
import static com.playlet.internal.constants.RedisKeyConstants.LIKE_DRAMA_SET_UID;
import static com.playlet.internal.constants.RedisKeyConstants.LIKE_EP_SET_UID;
import static com.playlet.internal.constants.RedisKeyConstants.SHARE_CD_SEC;
import static com.playlet.internal.constants.RedisKeyConstants.SHARE_CD_UID_DRAMA;
import static com.playlet.internal.entity.drama.UserDramaLikeEntity.LIKE_TYPE_DRAMA;
import static com.playlet.internal.entity.drama.UserDramaLikeEntity.LIKE_TYPE_EPISODE;

@Slf4j
@RestController
@CrossOrigin
public class UserInteractServiceImpl extends BaseApiService implements UserInteractService {

	@Autowired
	private UserDramaCollectDao userDramaCollectDao;
	@Autowired
	private UserDramaLikeDao userDramaLikeDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private RedisUtil redisUtil;

	@Override
	public ResponseBase collectAdd(@RequestParam Integer dramaId, HttpServletRequest request) {
		String uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (dramaId == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (dramaDao.findOnlineByDramaId(dramaId) == null && dramaDao.findByDramaId(dramaId) == null) {
			return setResultError(I18nUtil.getMessage("drama_null"));
		}
		if (userDramaCollectDao.findByUidAndDrama(uid, dramaId) != null) {
			cacheCollect(uid, dramaId, true);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}
		try {
			UserDramaCollectEntity row = new UserDramaCollectEntity();
			row.setUid(uid);
			row.setDramaId(dramaId);
			GenericityUtil.setDate(row);
			userDramaCollectDao.insert(row);
			dramaDao.incrCollectScore(dramaId);
			cacheCollect(uid, dramaId, true);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase collectCancel(@RequestParam Integer dramaId, HttpServletRequest request) {
        try {
            String uid = AppTokenUtil.resolveUid(request);
            if (uid == null) {
                return setResultError(I18nUtil.getMessage("login_required"));
            }
            if (dramaId == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            int deleted = userDramaCollectDao.deleteByUidAndDrama(uid, dramaId);
            if (deleted > 0) {
                dramaDao.decrCollectScore(dramaId);
            }
            cacheCollect(uid, dramaId, false);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public ResponseBase collectList(UserDramaCollectEntity entity, HttpServletRequest request) {
		String uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (entity == null) {
			entity = new UserDramaCollectEntity();
		}
		List<UserDramaCollectEntity> rows = userDramaCollectDao.findByUid(uid);
		if (rows == null) {
			rows = new ArrayList<>();
		}
		List<UserDramaCollectEntity> pageRows = GenericityUtil.Page(rows, entity.getPageNumber(), entity.getPageSize());
		List<TheaterCollectItemEntity> items = new ArrayList<>();
		for (UserDramaCollectEntity row : pageRows) {
			TheaterCollectItemEntity item = toCollectItem(row);
			if (item != null) {
				items.add(item);
			}
		}
		PageInfo<TheaterCollectItemEntity> page = new PageInfo<>(items);
		page.setTotal(rows.size());
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase likeDrama(@RequestParam Integer dramaId, HttpServletRequest request) {
		return doLike(dramaId, LIKE_TYPE_DRAMA, "", request);
	}

	@Override
	public ResponseBase likeDramaCancel(@RequestParam Integer dramaId, HttpServletRequest request) {
		return doLikeCancel(dramaId, LIKE_TYPE_DRAMA, "", request);
	}

	@Override
	public ResponseBase likeEpisode(@RequestParam Integer dramaId, @RequestParam String episodeId,
			HttpServletRequest request) {
		if (StringUtils.isEmpty(episodeId) || StringUtils.isEmpty(episodeId.trim())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		return doLike(dramaId, LIKE_TYPE_EPISODE, episodeId.trim(), request);
	}

	@Override
	public ResponseBase likeEpisodeCancel(@RequestParam Integer dramaId, @RequestParam String episodeId,
			HttpServletRequest request) {
		if (StringUtils.isEmpty(episodeId) || StringUtils.isEmpty(episodeId.trim())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		return doLikeCancel(dramaId, LIKE_TYPE_EPISODE, episodeId.trim(), request);
	}

	@Override
	public ResponseBase likeList(UserDramaLikeEntity entity, HttpServletRequest request) {
		String uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (entity == null) {
			entity = new UserDramaLikeEntity();
		}
		List<UserDramaLikeEntity> rows = userDramaLikeDao.findByUid(uid, entity.getLikeType());
		if (rows == null) {
			rows = new ArrayList<>();
		}
		List<UserDramaLikeEntity> pageRows = GenericityUtil.Page(rows, entity.getPageNumber(), entity.getPageSize());
		List<TheaterLikeItemEntity> items = new ArrayList<>();
		for (UserDramaLikeEntity row : pageRows) {
			TheaterLikeItemEntity item = toLikeItem(row);
			if (item != null) {
				items.add(item);
			}
		}
		PageInfo<TheaterLikeItemEntity> page = new PageInfo<>(items);
		page.setTotal(rows.size());
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase shareDrama(@RequestParam Integer dramaId, HttpServletRequest request) {
        try {
            String uid = AppTokenUtil.resolveUid(request);
            if (uid == null) {
                return setResultError(I18nUtil.getMessage("login_required"));
            }
            if (dramaId == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            if (dramaDao.findOnlineByDramaId(dramaId) == null && dramaDao.findByDramaId(dramaId) == null) {
                return setResultError(I18nUtil.getMessage("drama_null"));
            }
            String cdKey = SHARE_CD_UID_DRAMA + uid + ":" + dramaId;
            try {
                if (redisUtil.hasKey(cdKey)) {
                    return setResultSuccess(I18nUtil.getMessage("base_success"));
                }
            } catch (Exception e) {
                log.warn("share cooldown check failed: {}", e.getMessage());
            }
            dramaDao.incrShareScore(dramaId);
            try {
                redisUtil.set(cdKey, "1", SHARE_CD_SEC);
            } catch (Exception e) {
                log.warn("share cooldown set failed: {}", e.getMessage());
            }
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * 喜欢
	 *
	 * @param dramaId 短剧id
	 * @param likeType 喜欢类型
	 * @param episodeId 剧集id
	 * @param request 请求
	 * @return
	 */
	private ResponseBase doLike(Integer dramaId, int likeType, String episodeId, HttpServletRequest request) {
		String uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (dramaId == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (dramaDao.findByDramaId(dramaId) == null) {
			return setResultError(I18nUtil.getMessage("drama_null"));
		}
		String ep = episodeId == null ? "" : episodeId;
		Integer assetId = null;
		if (likeType == LIKE_TYPE_EPISODE) {
			assetId = parseAssetId(ep);
			if (assetId == null || !isEpisodeOfDrama(dramaId, assetId)) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
		}
		if (userDramaLikeDao.findOne(uid, dramaId, likeType, ep) != null) {
			cacheLike(uid, dramaId, likeType, ep, true);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}
		try {
			UserDramaLikeEntity row = new UserDramaLikeEntity();
			row.setUid(uid);
			row.setDramaId(dramaId);
			row.setLikeType(likeType);
			row.setEpisodeId(ep);
			GenericityUtil.setDate(row);
			userDramaLikeDao.insert(row);
			dramaDao.incrLikeScore(dramaId);
			if (assetId != null) {
				dramaAssetDao.incrLikeScore(assetId);
			}
			cacheLike(uid, dramaId, likeType, ep, true);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 取消喜欢
	 *
	 * @param dramaId 短剧id
	 * @param likeType 喜欢类型 1 短剧 2 剧集
	 * @param episodeId 剧集id
	 * @param request 请求
	 * @return
	 */
	private ResponseBase doLikeCancel(Integer dramaId, int likeType, String episodeId, HttpServletRequest request) {
		String uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (dramaId == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		String ep = episodeId == null ? "" : episodeId;
		int deleted = userDramaLikeDao.deleteOne(uid, dramaId, likeType, ep);
		if (deleted > 0) {
			dramaDao.decrLikeScore(dramaId);
			if (likeType == LIKE_TYPE_EPISODE) {
				Integer assetId = parseAssetId(ep);
				if (assetId != null) {
					dramaAssetDao.decrLikeScore(assetId);
				}
			}
		}
		cacheLike(uid, dramaId, likeType, ep, false);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	private Integer parseAssetId(String episodeId) {
		if (StringUtils.isEmpty(episodeId)) {
			return null;
		}
		try {
			return Integer.valueOf(episodeId.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private boolean isEpisodeOfDrama(Integer dramaId, Integer assetId) {
		DramaAssetEntity asset = dramaAssetDao.selectById(assetId);
		return asset != null && dramaId.equals(asset.getDramaId());
	}

	/**
	 * 缓存收藏
	 * @param uid 用户id
	 * @param dramaId 短剧id
	 * @param add 是否添加
	 */
	private void cacheCollect(String uid, Integer dramaId, boolean add) {
		try {
			String key = COLLECT_SET_UID + uid;
			String member = String.valueOf(dramaId);
			if (add) {
				redisUtil.sSetAndTime(key, INTERACT_TTL_SEC, member);
			} else {
				redisUtil.setRemove(key, member);
			}
		} catch (Exception e) {
			log.warn("cacheCollect failed: {}", e.getMessage());
		}
	}

	/**
	 * 缓存喜欢
	 * @param uid 用户id
	 * @param dramaId 短剧id
	 * @param likeType 喜欢类型 1 短剧 2 剧集
	 * @param episodeId 剧集id
	 * @param add 是否添加
	 */
	private void cacheLike(String uid, Integer dramaId, int likeType, String episodeId, boolean add) {
		try {
			if (likeType == LIKE_TYPE_DRAMA) {
				String key = LIKE_DRAMA_SET_UID + uid;
				String member = String.valueOf(dramaId);
				if (add) {
					redisUtil.sSetAndTime(key, INTERACT_TTL_SEC, member);
				} else {
					redisUtil.setRemove(key, member);
				}
			} else {
				String key = LIKE_EP_SET_UID + uid;
				String member = dramaId + ":" + episodeId;
				if (add) {
					redisUtil.sSetAndTime(key, INTERACT_TTL_SEC, member);
				} else {
					redisUtil.setRemove(key, member);
				}
			}
		} catch (Exception e) {
			log.warn("cacheLike failed: {}", e.getMessage());
		}
	}

	/**
	 * 转换收藏
	 * @param row 收藏
	 * @return
	 */
	private TheaterCollectItemEntity toCollectItem(UserDramaCollectEntity row) {
		if (row == null || row.getDramaId() == null) {
			return null;
		}
		DramaEntity drama = dramaDao.findByDramaId(row.getDramaId());
		if (drama == null) {
			return null;
		}
		TheaterCollectItemEntity item = new TheaterCollectItemEntity();
		item.setDramaId(row.getDramaId());
		item.setTitle(drama.getDramaTitle());
		item.setCoverUrl(drama.getCoverUrl());
		item.setTotalEpisodes(drama.getTotalEpisodes());
		item.setFinished(drama.getFinishedState());
		item.setSetTime(row.getSetTime());
		return item;
	}

	/**
	 * 转换喜欢
	 * @param row 喜欢
	 * @return
	 */
	private TheaterLikeItemEntity toLikeItem(UserDramaLikeEntity row) {
		if (row == null || row.getDramaId() == null) {
			return null;
		}
		DramaEntity drama = dramaDao.findByDramaId(row.getDramaId());
		if (drama == null) {
			return null;
		}
		TheaterLikeItemEntity item = new TheaterLikeItemEntity();
		item.setDramaId(row.getDramaId());
		item.setTitle(drama.getDramaTitle());
		item.setCoverUrl(drama.getCoverUrl());
		item.setTotalEpisodes(drama.getTotalEpisodes());
		item.setFinished(drama.getFinishedState());
		item.setLikeType(row.getLikeType());
		item.setEpisodeId(StringUtils.isEmpty(row.getEpisodeId()) ? null : row.getEpisodeId());
		item.setSetTime(row.getSetTime());
		return item;
	}
}
