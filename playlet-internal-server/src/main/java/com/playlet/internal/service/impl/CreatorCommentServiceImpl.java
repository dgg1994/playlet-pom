package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.request.SensitiveRecordEntity;
import com.playlet.internal.api.response.CreatorCommentListRespEntity;
import com.playlet.internal.api.response.CreatorCommentListRow;
import com.playlet.internal.api.response.CreatorCommentParentRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.creator.CreatorCommentDao;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.DramaVideoCommentDao;
import com.playlet.internal.dao.drama.UserInteractMessageDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.entity.drama.UserInteractMessageEntity;
import com.playlet.internal.enums.CommentTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.enums.CreatorCommentSortEnums;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.InteractMessageTypeEnums;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.enums.SensitiveSourceEnums;
import com.playlet.internal.query.creator.CreatorCommentListQuery;
import com.playlet.internal.query.creator.CreatorCommentPinQuery;
import com.playlet.internal.query.creator.CreatorCommentReplyQuery;
import com.playlet.internal.security.sensitive.SensitiveDecision;
import com.playlet.internal.service.CreatorCommentService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.PushNotifyService;
import com.playlet.internal.service.SensitiveRecordService;
import com.playlet.internal.service.SensitiveWordService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.HtmlSanitizeUtils;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作家评论管理：列表 / 置顶 / 软删 / 作者身份回复。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorCommentServiceImpl extends BaseApiService implements CreatorCommentService {

	@Autowired
	private CreatorCommentDao creatorCommentDao;
	@Autowired
	private DramaVideoCommentDao dramaVideoCommentDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private MediaUrlService mediaUrlService;
	@Autowired
	private SensitiveWordService sensitiveWordService;
	@Autowired
	private SensitiveRecordService sensitiveRecordService;
	@Autowired
	private UserInteractMessageDao userInteractMessageDao;
	@Autowired
	private PushNotifyService pushNotifyService;

	@Override
	public ResponseBase findList(@RequestBody(required = false) CreatorCommentListQuery query,
			HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query == null) {
			query = new CreatorCommentListQuery();
		}
		if (StringUtils.isNotEmpty(query.getDramaTitle())) {
			query.setDramaTitle(query.getDramaTitle().trim());
		}
		// commentType 缺省按集评，非法值直接拒
		if (query.getCommentType() == null) {
			query.setCommentType(CommentTypeEnums.VIDEO.getCode());
		} else if (CommentTypeEnums.fromCode(query.getCommentType()) == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		CreatorCommentSortEnums sort = CreatorCommentSortEnums.fromCode(query.getSortType());
		if (sort == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		query.setSortType(sort.getCode());

		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<CreatorCommentListRow> rows = creatorCommentDao.findList(query, account.getId());
		if (rows == null) {
			rows = Collections.emptyList();
		}
		PageInfo<CreatorCommentListRow> basePage = new PageInfo<>(rows);
		// 第二步：批量补用户昵称头像、父评摘要
		List<CreatorCommentListRespEntity> list = buildListResp(rows);
		PageInfo<CreatorCommentListRespEntity> pageInfo = new PageInfo<>(list);
		pageInfo.setTotal(basePage.getTotal());
		pageInfo.setPages(basePage.getPages());
		pageInfo.setPageNum(basePage.getPageNum());
		pageInfo.setPageSize(basePage.getPageSize());
		log.info("creator comment findList creatorId={} dramaId={} commentType={} size={}",
				account.getId(), query.getDramaId(), query.getCommentType(), list.size());
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase pin(@Valid @RequestBody CreatorCommentPinQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		boolean pinOn = CreatorConstants.COMMENT_PIN_ON == query.getPinFlag();
		boolean pinOff = CreatorConstants.COMMENT_PIN_OFF == query.getPinFlag();
		if (!pinOn && !pinOff) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		DramaVideoCommentEntity comment = loadOwnedComment(query.getCommentId(), account.getId(), true);
		if (comment == null) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		try {
			// 每剧仅一条置顶：置新顶时先取消该剧其它置顶
			if (pinOn && comment.getDramaId() != null) {
				dramaVideoCommentDao.unpinOthersOnDrama(comment.getDramaId(), comment.getId(),
						CreatorConstants.COMMENT_PIN_ON, CreatorConstants.COMMENT_PIN_OFF,
						DeleteStateEnum.NORMAL.getIndex());
			}
			comment.setPinFlag(query.getPinFlag());
			comment.setPinTime(pinOn ? new Date() : null);
			GenericityUtil.updateDate(comment);
			dramaVideoCommentDao.updateById(comment);
		} catch (BaseException e) {
			log.error("creator comment pin biz error creatorId={} commentId={}", account.getId(),
					query.getCommentId(), e);
			throw e;
		} catch (Exception e) {
			log.error("creator comment pin failed creatorId={} commentId={}", account.getId(),
					query.getCommentId(), e);
			throw new BaseException("操作失败", e);
		}
		log.info("creator comment pin creatorId={} commentId={} pinFlag={}", account.getId(),
				query.getCommentId(), query.getPinFlag());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase delete(@RequestParam("id") Integer id, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		DramaVideoCommentEntity comment = loadOwnedComment(id, account.getId(), false);
		if (comment == null) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		boolean alreadyDeleted = comment.getDeleteState() != null
				&& DeleteStateEnum.DELETE.getIndex().equals(comment.getDeleteState());
		if (alreadyDeleted) {
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}
		try {
			comment.setDeleteState(DeleteStateEnum.DELETE.getIndex());
			GenericityUtil.updateDate(comment);
			dramaVideoCommentDao.updateById(comment);
			decrementDiscussScore(comment);
			// 删回复时回写父评回复数
			if (comment.getParentId() != null && comment.getParentId() > 0) {
				decrementParentReplyCount(comment.getParentId());
			}
		} catch (Exception e) {
			log.error("creator comment delete failed creatorId={} commentId={}", account.getId(), id, e);
			throw new RuntimeException(e);
		}
		log.info("creator comment delete creatorId={} commentId={}", account.getId(), id);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase reply(@Valid @RequestBody CreatorCommentReplyQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CommentTypeEnums commentType;
		if (query.getCommentType() == null) {
			commentType = CommentTypeEnums.VIDEO;
		} else {
			commentType = CommentTypeEnums.fromCode(query.getCommentType());
			if (commentType == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
		}
		if (CommentTypeEnums.VIDEO.equals(commentType) && query.getVideoId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		return doReply(account, query.getDramaId(), query.getVideoId(), query.getParentId(),
				query.getReplyToUserId(), query.getCommentInfo(), commentType);
	}

	/**
	 * 作者身份回复：落库格式对齐 C 端剧评/集评，userId 用占位、from_creator_id 为当前作家。
	 */
	private ResponseBase doReply(CreatorAccountEntity account, Integer dramaId, Integer videoId,
			Integer parentId, Integer replyToUserId, String commentInfo, CommentTypeEnums commentType) {
		boolean dramaReply = CommentTypeEnums.DRAMA.equals(commentType);
		DramaVideoCommentEntity parent = loadOwnedComment(parentId, account.getId(), true);
		if (parent == null) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		boolean parentIsDrama = CommentTypeEnums.isDrama(parent.getCommentType());
		if (dramaReply != parentIsDrama) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		if (dramaId == null || !dramaId.equals(parent.getDramaId())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (!dramaReply) {
			Integer parentVideoId = parent.getVideoId();
			if (videoId == null || parentVideoId == null || !videoId.equals(parentVideoId)) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
		}
		String sanitized = HtmlSanitizeUtils.plain(commentInfo);
		if (StringUtils.isEmpty(sanitized)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		Integer persistVideoId = dramaReply ? 0 : videoId;
		Integer sourceCode = dramaReply
				? SensitiveSourceEnums.DRAMA_COMMENT.getCode()
				: SensitiveSourceEnums.VIDEO_COMMENT.getCode();
		SensitiveDecision decision = sensitiveWordService.decide(sanitized);
		if (decision.isReject()) {
			sensitiveRecordService.saveRecord(new SensitiveRecordEntity(
					null, CreatorConstants.COMMENT_CREATOR_USER_ID_PLACEHOLDER, dramaId,
					persistVideoId, sanitized, sourceCode), decision.getCheck());
			return setResultError(I18nUtil.getMessage("sensitive_forbidden"));
		}

		DramaVideoCommentEntity entity = new DramaVideoCommentEntity();
		entity.setDramaId(dramaId);
		entity.setVideoId(persistVideoId);
		entity.setCommentType(commentType.getCode());
		entity.setUserId(CreatorConstants.COMMENT_CREATOR_USER_ID_PLACEHOLDER);
		entity.setFromCreatorId(account.getId());
		entity.setCommentInfo(decision.getContent());
		entity.setScore(null);
		entity.setLikeCount(0);
		entity.setReplyCount(0);
		entity.setParentId(parent.getId());
		entity.setReplyToUserId(replyToUserId);
		entity.setPinFlag(CreatorConstants.COMMENT_PIN_OFF);
		entity.setDeleteState(decision.isHidden()
				? DeleteStateEnum.DELETE.getIndex()
				: DeleteStateEnum.NORMAL.getIndex());
		try {
			GenericityUtil.setDate(entity);
			dramaVideoCommentDao.insert(entity);
			if (decision.shouldRecord()) {
				sensitiveRecordService.saveRecord(new SensitiveRecordEntity(
						entity.getId(), CreatorConstants.COMMENT_CREATOR_USER_ID_PLACEHOLDER,
						entity.getDramaId(), entity.getVideoId(), entity.getCommentInfo(),
						sourceCode), decision.getCheck());
			}
			if (decision.isHidden()) {
				return setResultSuccess(I18nUtil.getMessage("sensitive_under_review"));
			}
			incrementParentReplyCount(parent.getId());
			addDiscussScore(entity);
			pushInteractMessage(replyToUserId, entity.getId(), parent.getId(),
					entity.getDramaId(), persistVideoId, entity.getCommentInfo(), dramaReply);
		} catch (BaseException e) {
			log.error("creator comment reply biz error creatorId={} parentId={}", account.getId(), parentId, e);
			throw e;
		} catch (Exception e) {
			log.error("creator comment reply failed creatorId={} parentId={}", account.getId(), parentId, e);
			throw new BaseException("操作失败", e);
		}
		log.info("creator comment reply creatorId={} parentId={} commentId={} type={}",
				account.getId(), parent.getId(), entity.getId(), commentType.getCode());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 评论须存在且所属剧 belong_user = 当前作家。
	 * @param requireVisible true 时已删评论不可操作（置顶/回复）
	 */
	private DramaVideoCommentEntity loadOwnedComment(Integer commentId, Integer creatorId,
			boolean requireVisible) {
		if (commentId == null || creatorId == null) {
			return null;
		}
		DramaVideoCommentEntity comment = dramaVideoCommentDao.selectById(commentId);
		if (comment == null) {
			return null;
		}
		DramaEntity drama = dramaDao.selectById(comment.getDramaId());
		if (drama == null || drama.getBelongUser() == null || !drama.getBelongUser().equals(creatorId)) {
			return null;
		}
		boolean deleted = comment.getDeleteState() != null
				&& DeleteStateEnum.DELETE.getIndex().equals(comment.getDeleteState());
		if (requireVisible && deleted) {
			return null;
		}
		return comment;
	}

	/**
	 * 批量补齐评论用户、父评摘要后组装列表。
	 */
	private List<CreatorCommentListRespEntity> buildListResp(List<CreatorCommentListRow> rows) {
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		Set<Integer> userIds = new HashSet<>();
		Set<Integer> creatorIds = new HashSet<>();
		Set<Integer> parentIds = new HashSet<>();
		for (CreatorCommentListRow row : rows) {
			collectAuthorIds(row.getFromCreatorId(), row.getUserId(), userIds, creatorIds);
			if (row.getParentId() != null && row.getParentId() > 0) {
				parentIds.add(row.getParentId());
			}
		}

		Map<Integer, DramaVideoCommentEntity> parentMap = loadParentMap(parentIds);
		for (DramaVideoCommentEntity parent : parentMap.values()) {
			if (parent == null) {
				continue;
			}
			collectAuthorIds(parent.getFromCreatorId(), parent.getUserId(), userIds, creatorIds);
		}
		Map<Integer, AppAccountEntity> userMap = loadUserMap(userIds);
		Map<Integer, CreatorAccountEntity> creatorMap = loadCreatorMap(creatorIds);

		List<CreatorCommentListRespEntity> list = new ArrayList<>(rows.size());
		for (CreatorCommentListRow row : rows) {
			list.add(toListResp(row, parentMap, userMap, creatorMap));
		}
		return list;
	}

	private void collectAuthorIds(Integer fromCreatorId, Integer userId,
			Set<Integer> userIds, Set<Integer> creatorIds) {
		if (fromCreatorId != null && fromCreatorId > 0) {
			creatorIds.add(fromCreatorId);
			return;
		}
		if (userId != null && userId > 0) {
			userIds.add(userId);
		}
	}

	private Map<Integer, DramaVideoCommentEntity> loadParentMap(Set<Integer> parentIds) {
		if (parentIds == null || parentIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<DramaVideoCommentEntity> parents = dramaVideoCommentDao.findByIds(new ArrayList<>(parentIds));
		if (parents == null || parents.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, DramaVideoCommentEntity> map = new HashMap<>(parents.size());
		for (DramaVideoCommentEntity parent : parents) {
			if (parent != null && parent.getId() != null) {
				map.put(parent.getId(), parent);
			}
		}
		return map;
	}


	private Map<Integer, AppAccountEntity> loadUserMap(Set<Integer> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<AppAccountEntity> users = appAccountDao.findByUids(new ArrayList<>(userIds));
		if (users == null || users.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, AppAccountEntity> map = new HashMap<>(users.size());
		for (AppAccountEntity user : users) {
			if (user != null && user.getId() != null) {
				map.put(user.getId(), user);
			}
		}
		return map;
	}

	private Map<Integer, CreatorAccountEntity> loadCreatorMap(Set<Integer> creatorIds) {
		if (creatorIds == null || creatorIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<CreatorAccountEntity> creators = creatorAccountDao.findByIds(new ArrayList<>(creatorIds));
		if (creators == null || creators.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, CreatorAccountEntity> map = new HashMap<>(creators.size());
		for (CreatorAccountEntity creator : creators) {
			if (creator != null && creator.getId() != null) {
				map.put(creator.getId(), creator);
			}
		}
		return map;
	}

	private CreatorCommentListRespEntity toListResp(CreatorCommentListRow row,
			Map<Integer, DramaVideoCommentEntity> parentMap,
			Map<Integer, AppAccountEntity> userMap,
			Map<Integer, CreatorAccountEntity> creatorMap) {
		CreatorCommentListRespEntity resp = new CreatorCommentListRespEntity();
		resp.setId(row.getId());
		resp.setDramaId(row.getDramaId());
		resp.setDramaTitle(row.getDramaTitle());
		resp.setVideoId(row.getVideoId());
		resp.setSetNum(row.getSetNum());
		resp.setCommentType(row.getCommentType() == null
				? CommentTypeEnums.VIDEO.getCode()
				: row.getCommentType());
		boolean isReply = row.getParentId() != null && row.getParentId() > 0;
		resp.setReplyFlag(isReply ? PublicEnums.ONE.getIndex() : PublicEnums.ZERO.getIndex());
		resp.setParentId(row.getParentId() == null ? 0 : row.getParentId());
		resp.setUserId(row.getUserId());
		resp.setFromCreatorId(row.getFromCreatorId());
		fillAuthor(resp, row.getFromCreatorId(), row.getUserId(), userMap, creatorMap);
		resp.setCommentInfo(row.getCommentInfo());
		resp.setLikeCount(row.getLikeCount() == null ? 0 : row.getLikeCount());
		resp.setPinFlag(row.getPinFlag() == null ? 0 : row.getPinFlag());
		resp.setPinTime(row.getPinTime());
		resp.setSetTime(row.getSetTime());
		resp.setContextText(buildContextText(row.getDramaTitle(), row.getSetNum(), isReply));
		if (isReply) {
			DramaVideoCommentEntity parent = parentMap.get(row.getParentId());
			if (parent != null) {
				CreatorCommentParentRespEntity parentResp = new CreatorCommentParentRespEntity();
				parentResp.setId(parent.getId());
				parentResp.setCommentInfo(parent.getCommentInfo());
				fillParentAuthor(parentResp, parent.getFromCreatorId(), parent.getUserId(), userMap, creatorMap);
				resp.setParentComment(parentResp);
			}
		}
		return resp;
	}

	/** 填充评论作者展示名/头像：优先作家身份 */
	private void fillAuthor(CreatorCommentListRespEntity resp, Integer fromCreatorId, Integer userId,
			Map<Integer, AppAccountEntity> userMap, Map<Integer, CreatorAccountEntity> creatorMap) {
		if (fromCreatorId != null && fromCreatorId > 0) {
			CreatorAccountEntity creator = creatorMap.get(fromCreatorId);
			if (creator != null) {
				resp.setUserName(resolveCreatorName(creator));
				resp.setAvatar(mediaUrlService.sign(creator.getAvatar()));
				return;
			}
		}
		AppAccountEntity user = userId == null ? null : userMap.get(userId);
		if (user != null) {
			resp.setUserName(user.getNickname());
			resp.setAvatar(mediaUrlService.sign(user.getAvatar()));
			return;
		}
		resp.setUserName(null);
		resp.setAvatar(null);
	}

	private void fillParentAuthor(CreatorCommentParentRespEntity parentResp, Integer fromCreatorId,
			Integer userId, Map<Integer, AppAccountEntity> userMap,
			Map<Integer, CreatorAccountEntity> creatorMap) {
		if (fromCreatorId != null && fromCreatorId > 0) {
			CreatorAccountEntity creator = creatorMap.get(fromCreatorId);
			if (creator != null) {
				parentResp.setUserName(resolveCreatorName(creator));
				parentResp.setAvatar(mediaUrlService.sign(creator.getAvatar()));
				return;
			}
		}
		AppAccountEntity user = userId == null ? null : userMap.get(userId);
		if (user != null) {
			parentResp.setUserName(user.getNickname());
			parentResp.setAvatar(mediaUrlService.sign(user.getAvatar()));
			return;
		}
		parentResp.setUserName(null);
		parentResp.setAvatar(null);
	}

	private static String resolveCreatorName(CreatorAccountEntity creator) {
		if (creator == null) {
			return null;
		}
		if (StringUtils.isNotEmpty(creator.getNickname())) {
			return creator.getNickname();
		}
		return creator.getUserAccount();
	}

	/** 原型文案：在 {剧名} 第xx集 发表了评论 / 回复了评论（多语言） */
	private static String buildContextText(String dramaTitle, Integer setNum, boolean isReply) {
		String title = StringUtils.isEmpty(dramaTitle) ? "" : dramaTitle;
		String ep = setNum == null ? "--" : String.format("%02d", setNum);
		String code = isReply ? "creator.comment.context_reply" : "creator.comment.context_post";
		return I18nUtil.getMessage(code, title, ep);
	}

	private void addDiscussScore(DramaVideoCommentEntity entity) {
		DramaEntity drama = dramaDao.selectById(entity.getDramaId());
		if (drama != null) {
			long discuss = drama.getDiscussScore() == null ? 0L : drama.getDiscussScore();
			drama.setDiscussScore(discuss + 1);
			dramaDao.updateById(drama);
		}
		if (entity.getVideoId() != null && entity.getVideoId() > 0) {
			DramaAssetEntity asset = dramaAssetDao.selectById(entity.getVideoId());
			if (asset != null) {
				long discuss = asset.getDiscussScore() == null ? 0L : asset.getDiscussScore();
				asset.setDiscussScore(discuss + 1);
				dramaAssetDao.updateById(asset);
			}
		}
	}

	private void decrementDiscussScore(DramaVideoCommentEntity entity) {
		DramaEntity drama = dramaDao.selectById(entity.getDramaId());
		if (drama != null && drama.getDiscussScore() != null) {
			drama.setDiscussScore(Math.max(drama.getDiscussScore() - 1, 0));
			dramaDao.updateById(drama);
		}
		if (entity.getVideoId() != null && entity.getVideoId() > 0) {
			DramaAssetEntity asset = dramaAssetDao.selectById(entity.getVideoId());
			if (asset != null && asset.getDiscussScore() != null) {
				asset.setDiscussScore(Math.max(asset.getDiscussScore() - 1, 0));
				dramaAssetDao.updateById(asset);
			}
		}
	}

	private void incrementParentReplyCount(Integer parentId) {
		DramaVideoCommentEntity parent = dramaVideoCommentDao.selectById(parentId);
		if (parent == null) {
			return;
		}
		int count = parent.getReplyCount() == null ? 0 : parent.getReplyCount();
		parent.setReplyCount(count + 1);
		dramaVideoCommentDao.updateById(parent);
	}

	private void decrementParentReplyCount(Integer parentId) {
		DramaVideoCommentEntity parent = dramaVideoCommentDao.selectById(parentId);
		if (parent == null) {
			return;
		}
		int count = parent.getReplyCount() == null ? 0 : parent.getReplyCount();
		parent.setReplyCount(Math.max(count - 1, 0));
		dramaVideoCommentDao.updateById(parent);
	}

	/** 通知被回复观众；fromUid 用占位，C 端可按 comment.from_creator_id 展示作者 */
	private void pushInteractMessage(Integer toUid, Integer commentId, Integer replyCommentId,
			Integer dramaId, Integer videoId, String content, boolean dramaReply) {
		if (toUid == null || toUid <= 0) {
			return;
		}
		Integer fromUid = CreatorConstants.COMMENT_CREATOR_USER_ID_PLACEHOLDER;
		String episodeId = videoId == null || videoId <= 0 ? null : String.valueOf(videoId);
		String messageType = dramaReply
				? InteractMessageTypeEnums.REPLY_DRAMA.getCode()
				: InteractMessageTypeEnums.REPLY_VIDEO.getCode();
		String bizPrefix = dramaReply ? "creator_drama_reply:" : "creator_video_reply:";
		UserInteractMessageEntity msg = new UserInteractMessageEntity();
		msg.setToUid(toUid);
		msg.setFromUid(fromUid);
		msg.setMessageType(messageType);
		msg.setCommentId(commentId);
		msg.setReplyCommentId(replyCommentId);
		msg.setDramaId(dramaId);
		msg.setEpisodeId(episodeId);
		msg.setContent(content);
		msg.setBizId(bizPrefix + commentId);
		msg.setIsRead(0);
		msg.setStatus(1);
		try {
			GenericityUtil.setDate(msg);
			userInteractMessageDao.insert(msg);
			pushNotifyService.notifyInteract(toUid, fromUid, messageType, msg.getId(), dramaId, episodeId);
		} catch (Exception e) {
			log.warn("creator comment push failed commentId={}: {}", commentId, e.getMessage());
		}
	}
}
