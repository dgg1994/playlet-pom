package com.playlet.internal.service.impl;

import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.playlet.internal.api.response.TheaterInteractMessageItemEntity;
import com.playlet.internal.api.response.TheaterCollectItemEntity;
import com.playlet.internal.api.response.TheaterLikeItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaCommentLikeDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.DramaVideoCommentDao;
import com.playlet.internal.dao.drama.UserInteractMessageDao;
import com.playlet.internal.dao.drama.UserDramaCollectDao;
import com.playlet.internal.dao.drama.UserDramaLikeDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.entity.drama.UserInteractMessageEntity;
import com.playlet.internal.entity.drama.UserDramaCollectEntity;
import com.playlet.internal.entity.drama.UserDramaLikeEntity;
import com.playlet.internal.enums.InteractMessageTypeEnums;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.query.drama.InteractMessageQuery;
import com.playlet.internal.service.DramaRankStatService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.MedalProgressService;
import com.playlet.internal.service.PushNotifyService;
import com.playlet.internal.service.UserInteractService;
import com.playlet.internal.service.WelfareTaskService;
import com.playlet.internal.enums.WelfareActionTypeEnums;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private UserInteractMessageDao userInteractMessageDao;
	@Autowired
	private UserDramaLikeDao userDramaLikeDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private WelfareTaskService welfareTaskService;
	@Autowired
	private DramaRankStatService dramaRankStatService;
	@Autowired
	private MediaUrlService mediaUrlService;
	@Autowired
	private MedalProgressService medalProgressService;
	@Autowired
	private PushNotifyService pushNotifyService;
	@Autowired
	private DramaVideoCommentDao dramaVideoCommentDao;
	@Autowired
	private DramaCommentLikeDao dramaCommentLikeDao;

	@Override
	public ResponseBase collectAdd(@RequestParam Integer dramaId, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403,I18nUtil.getMessage("login_required"));
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
			dramaRankStatService.onCollect(dramaId, 1);
			cacheCollect(uid, dramaId, true);
			try {
				medalProgressService.onAction(uid, WelfareActionTypeEnums.COLLECT, 1, String.valueOf(dramaId));
			} catch (Exception e) {
				log.warn("medal collect progress failed: {}", e.getMessage());
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase collectCancel(@RequestParam Integer dramaId, HttpServletRequest request) {
        try {
        	Integer uid = AppTokenUtil.resolveUid(request);
            if (uid == null) {
                return setResultError(Constants.HTTP_RES_CODE_403,I18nUtil.getMessage("login_required"));
            }
            if (dramaId == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            int deleted = userDramaCollectDao.deleteByUidAndDrama(uid, dramaId);
            if (deleted > 0) {
                dramaDao.decrCollectScore(dramaId);
                dramaRankStatService.onCollect(dramaId, -1);
            }
            cacheCollect(uid, dramaId, false);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public ResponseBase collectList(UserDramaCollectEntity entity, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403,I18nUtil.getMessage("login_required"));
		}
		if (entity == null) {
			entity = new UserDramaCollectEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<UserDramaCollectEntity> rows = userDramaCollectDao.findByUid(uid);
		if (rows == null) {
			rows = new ArrayList<>();
		}
		PageInfo<UserDramaCollectEntity> basePage = new PageInfo<>(rows);
		List<TheaterCollectItemEntity> items = new ArrayList<>();
		for (UserDramaCollectEntity row : rows) {
			TheaterCollectItemEntity item = toCollectItem(row);
			if (item != null) {
				items.add(item);
			}
		}
		PageInfo<TheaterCollectItemEntity> page = new PageInfo<>(items);
		page.setTotal(basePage.getTotal());
		page.setPageNum(basePage.getPageNum());
		page.setPageSize(basePage.getPageSize());
		page.setPages(basePage.getPages());
		page.setHasNextPage(basePage.isHasNextPage());
		page.setHasPreviousPage(basePage.isHasPreviousPage());
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
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403,I18nUtil.getMessage("login_required"));
		}
		if (entity == null) {
			entity = new UserDramaLikeEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<UserDramaLikeEntity> rows = userDramaLikeDao.findByUid(uid, entity.getLikeType());
		if (rows == null) {
			rows = new ArrayList<>();
		}
		PageInfo<UserDramaLikeEntity> basePage = new PageInfo<>(rows);
		List<TheaterLikeItemEntity> items = new ArrayList<>();
		for (UserDramaLikeEntity row : rows) {
			TheaterLikeItemEntity item = toLikeItem(row);
			if (item != null) {
				items.add(item);
			}
		}
		PageInfo<TheaterLikeItemEntity> page = new PageInfo<>(items);
		page.setTotal(basePage.getTotal());
		page.setPageNum(basePage.getPageNum());
		page.setPageSize(basePage.getPageSize());
		page.setPages(basePage.getPages());
		page.setHasNextPage(basePage.isHasNextPage());
		page.setHasPreviousPage(basePage.isHasPreviousPage());
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase interactMessageList(InteractMessageQuery entity, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (entity == null) {
			entity = new InteractMessageQuery();
		}
		entity.setToUid(uid);
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<UserInteractMessageEntity> rows = userInteractMessageDao.findByToUid(entity);
		if (rows == null) {
			rows = new ArrayList<>();
		}
		PageInfo<UserInteractMessageEntity> basePage = new PageInfo<>(rows);
		Set<Integer> likedCommentIds = loadLikedCommentIds(uid, rows);
		List<TheaterInteractMessageItemEntity> items = new ArrayList<>();
		for (UserInteractMessageEntity row : rows) {
			TheaterInteractMessageItemEntity item = toInteractMessageItem(row, likedCommentIds);
			if (item != null) {
				items.add(item);
			}
		}
		PageInfo<TheaterInteractMessageItemEntity> page = new PageInfo<>(items);
		page.setTotal(basePage.getTotal());
		page.setPages(basePage.getPages());
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase interactMessageRead(@RequestParam Long id, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		userInteractMessageDao.readOne(id, uid);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase interactMessageReadAll(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		userInteractMessageDao.readAll(uid);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase interactMessageUnreadCount(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		Integer count = userInteractMessageDao.countUnread(uid);
		return setResultSuccess(count == null ? 0 : count, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase shareDrama(@RequestParam Integer dramaId, HttpServletRequest request) {
        try {
        	Integer uid = AppTokenUtil.resolveUid(request);
            if (uid == null) {
                return setResultError(Constants.HTTP_RES_CODE_403,I18nUtil.getMessage("login_required"));
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
            try {
                welfareTaskService.onAction(uid, WelfareActionTypeEnums.SHARE, 1,
                        "{\"dramaId\":" + dramaId + "}");
            } catch (Exception e) {
                log.warn("welfare share progress failed: {}", e.getMessage());
            }
            try {
                medalProgressService.onAction(uid, WelfareActionTypeEnums.SHARE, 1, String.valueOf(dramaId));
            } catch (Exception e) {
                log.warn("medal share progress failed: {}", e.getMessage());
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
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403,I18nUtil.getMessage("login_required"));
		}
		if (dramaId == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		DramaEntity drama = dramaDao.findByDramaId(dramaId);
		if (drama == null) {
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
			dramaRankStatService.onLike(dramaId, 1);
			if (assetId != null) {
				dramaAssetDao.incrLikeScore(assetId);
			}
			pushLikeInteractMessage(uid, drama, likeType, ep);
			cacheLike(uid, dramaId, likeType, ep, true);
			try {
				medalProgressService.onAction(uid, WelfareActionTypeEnums.LIKE, 1,
						dramaId + ":" + likeType + ":" + ep);
			} catch (Exception e) {
				log.warn("medal like progress failed: {}", e.getMessage());
			}
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
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403,I18nUtil.getMessage("login_required"));
		}
		if (dramaId == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		String ep = episodeId == null ? "" : episodeId;
		int deleted = userDramaLikeDao.deleteOne(uid, dramaId, likeType, ep);
		if (deleted > 0) {
			dramaDao.decrLikeScore(dramaId);
			dramaRankStatService.onLike(dramaId, -1);
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
	private void cacheCollect(Integer uid, Integer dramaId, boolean add) {
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
	private void cacheLike(Integer uid, Integer dramaId, int likeType, String episodeId, boolean add) {
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
		item.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
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
		item.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
		item.setTotalEpisodes(drama.getTotalEpisodes());
		item.setFinished(drama.getFinishedState());
		item.setLikeType(row.getLikeType());
		item.setSetNum(dramaAssetDao.selectSetNum(row.getEpisodeId()));
		item.setEpisodeId(StringUtils.isEmpty(row.getEpisodeId()) ? null : row.getEpisodeId());
		item.setSetTime(row.getSetTime());
		return item;
	}

	/**
	 * 批量查当前用户对评论的点赞状态（drama_comment_like，不查 user_drama_like）。
	 */
	private Set<Integer> loadLikedCommentIds(Integer uid, List<UserInteractMessageEntity> rows) {
		if (uid == null || rows == null || rows.isEmpty()) {
			return Collections.emptySet();
		}
		List<Integer> commentIds = new ArrayList<>();
		for (UserInteractMessageEntity row : rows) {
			if (row == null || row.getCommentId() == null || !isActionableType(row.getMessageType())) {
				continue;
			}
			commentIds.add(row.getCommentId());
		}
		if (commentIds.isEmpty()) {
			return Collections.emptySet();
		}
		List<Integer> liked = dramaCommentLikeDao.findLikedCommentIds(uid, commentIds);
		return liked == null || liked.isEmpty() ? Collections.emptySet() : new HashSet<>(liked);
	}

	private TheaterInteractMessageItemEntity toInteractMessageItem(UserInteractMessageEntity row,
			Set<Integer> likedCommentIds) {
		if (row == null) {
			return null;
		}
		TheaterInteractMessageItemEntity item = new TheaterInteractMessageItemEntity();
		item.setId(row.getId());
		item.setMessageType(row.getMessageType());
		item.setToUid(row.getToUid());
		item.setFromUid(row.getFromUid());
		item.setDramaId(row.getDramaId());
		item.setEpisodeId(row.getEpisodeId());
		item.setCommentId(row.getCommentId());
		item.setReplyCommentId(row.getReplyCommentId());
		item.setIsRead(row.getIsRead());
		item.setSetTime(row.getSetTime());
		item.setActionText(resolveActionText(row.getMessageType()));
		boolean actionable = isActionableType(row.getMessageType());
		item.setShowActions(actionable ? PublicEnums.ONE.getIndex() : PublicEnums.ZERO.getIndex());
		// 评论点赞态：仅回复/评论类消息可操作；查 drama_comment_like
		if (actionable && row.getCommentId() != null
				&& likedCommentIds != null && likedCommentIds.contains(row.getCommentId())) {
			item.setIsLiked(PublicEnums.ONE.getIndex());
		} else {
			item.setIsLiked(PublicEnums.ZERO.getIndex());
		}

		if (row.getFromUid() != null) {
			AppAccountEntity account = appAccountDao.findByUid(row.getFromUid());
			if (account != null) {
				item.setFromNickname(StringUtils.isEmpty(account.getNickname())
						? account.getUserAccount() : account.getNickname());
				if (StringUtils.isNotEmpty(account.getAvatar())) {
					item.setFromAvatar(mediaUrlService.sign(account.getAvatar()));
				}
			}
		}

		if (row.getDramaId() != null) {
			DramaEntity drama = dramaDao.findByDramaId(row.getDramaId());
			if (drama == null) {
				drama = dramaDao.selectById(row.getDramaId());
			}
			if (drama != null) {
				item.setDramaTitle(drama.getDramaTitle());
				if (StringUtils.isNotEmpty(drama.getDramaTitle())) {
					item.setSourceText(I18nUtil.getMessage("interact.from_drama", drama.getDramaTitle()));
				}
				if (StringUtils.isNotEmpty(drama.getCoverUrl())) {
					item.setDramaCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
				}
			}
		}

		String type = row.getMessageType();
		// 回复：content=回复正文，refContent=被回复评论正文（均为文案）
		if (InteractMessageTypeEnums.REPLY_COMMENT.getCode().equals(type)) {
			item.setContent(resolveCommentText(row.getCommentId(), row.getContent()));
			item.setRefContent(resolveCommentText(row.getReplyCommentId(), null));
		}
		// 一级评论
		else if (InteractMessageTypeEnums.COMMENT_DRAMA.getCode().equals(type)
				|| InteractMessageTypeEnums.COMMENT_VIDEO.getCode().equals(type)) {
			item.setContent(resolveCommentText(row.getCommentId(), row.getContent()));
		}
		// 赞评论：主文案用 actionText；refContent=被赞评论正文
		else if (InteractMessageTypeEnums.LIKE_COMMENT.getCode().equals(type)) {
			item.setContent(null);
			item.setRefContent(resolveCommentText(row.getCommentId(), row.getContent()));
		}
		// 赞作品
		else if (InteractMessageTypeEnums.LIKE_DRAMA.getCode().equals(type)) {
			item.setContent(null);
		}

		item.setDisplayContent(buildDisplayContent(item.getActionText(), item.getContent(), type));
		return item;
	}

	/** 优先取评论表正文，保证返回文案而不是 id */
	private String resolveCommentText(Integer commentId, String fallback) {
		if (commentId != null) {
			DramaVideoCommentEntity comment = dramaVideoCommentDao.selectById(commentId);
			if (comment != null && StringUtils.isNotEmpty(comment.getCommentInfo())) {
				return comment.getCommentInfo();
			}
		}
		return StringUtils.isEmpty(fallback) ? null : fallback;
	}

	private String buildDisplayContent(String actionText, String content, String messageType) {
		if (InteractMessageTypeEnums.REPLY_COMMENT.getCode().equals(messageType)
				|| InteractMessageTypeEnums.COMMENT_DRAMA.getCode().equals(messageType)
				|| InteractMessageTypeEnums.COMMENT_VIDEO.getCode().equals(messageType)) {
			if (StringUtils.isEmpty(content)) {
				return actionText;
			}
			if (StringUtils.isEmpty(actionText)) {
				return content;
			}
			return actionText + "：" + content;
		}
		return actionText;
	}

	private boolean isActionableType(String messageType) {
		return InteractMessageTypeEnums.REPLY_COMMENT.getCode().equals(messageType)
				|| InteractMessageTypeEnums.COMMENT_DRAMA.getCode().equals(messageType)
				|| InteractMessageTypeEnums.COMMENT_VIDEO.getCode().equals(messageType);
	}

	private String resolveActionText(String messageType) {
		if (InteractMessageTypeEnums.LIKE_COMMENT.getCode().equals(messageType)) {
			return I18nUtil.getMessage("interact.like_comment");
		}
		if (InteractMessageTypeEnums.LIKE_DRAMA.getCode().equals(messageType)) {
			return I18nUtil.getMessage("interact.like_drama");
		}
		if (InteractMessageTypeEnums.REPLY_COMMENT.getCode().equals(messageType)) {
			return I18nUtil.getMessage("interact.reply_you");
		}
		if (InteractMessageTypeEnums.COMMENT_DRAMA.getCode().equals(messageType)
				|| InteractMessageTypeEnums.COMMENT_VIDEO.getCode().equals(messageType)) {
			return I18nUtil.getMessage("interact.comment_you");
		}
		return I18nUtil.getMessage("interact.default");
	}

	private void pushLikeInteractMessage(Integer fromUid, DramaEntity drama, int likeType, String episodeId) {
		if (fromUid == null || drama == null || drama.getBelongUser() == null) {
			return;
		}
		Integer toUid = drama.getBelongUser();
		if (fromUid.equals(toUid)) {
			return;
		}
		UserInteractMessageEntity msg = new UserInteractMessageEntity();
		msg.setToUid(toUid);
		msg.setFromUid(fromUid);
		msg.setMessageType(InteractMessageTypeEnums.LIKE_DRAMA.getCode());
		msg.setDramaId(drama.getId());
		msg.setEpisodeId(likeType == LIKE_TYPE_EPISODE ? episodeId : null);
		msg.setIsRead(0);
		msg.setStatus(1);
		msg.setBizId(likeType + ":" + fromUid + ":" + drama.getId() + ":" + (episodeId == null ? "" : episodeId));
		try {
			GenericityUtil.setDate(msg);
			userInteractMessageDao.insert(msg);
			pushNotifyService.notifyInteract(toUid, fromUid, msg.getMessageType(),
					msg.getId(), msg.getDramaId(), msg.getEpisodeId());
		} catch (Exception e) {
			log.warn("insert like interact message failed fromUid={} dramaId={}: {}",
					fromUid, drama.getId(), e.getMessage());
		}
	}
}
