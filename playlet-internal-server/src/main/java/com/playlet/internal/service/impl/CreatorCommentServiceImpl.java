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
import com.playlet.internal.dao.creator.CreatorCommentDao;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.DramaVideoCommentDao;
import com.playlet.internal.dao.drama.UserInteractMessageDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.entity.drama.UserInteractMessageEntity;
import com.playlet.internal.enums.CommentTypeEnums;
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
import java.util.List;

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
		List<CreatorCommentListRespEntity> list = new ArrayList<>(rows.size());
		for (CreatorCommentListRow row : rows) {
			list.add(toListResp(row));
		}
		PageInfo<CreatorCommentListRespEntity> pageInfo = new PageInfo<>(list);
		pageInfo.setTotal(basePage.getTotal());
		pageInfo.setPages(basePage.getPages());
		pageInfo.setPageNum(basePage.getPageNum());
		pageInfo.setPageSize(basePage.getPageSize());
		log.info("creator comment findList creatorId={} size={}", account.getId(), list.size());
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
			comment.setPinFlag(query.getPinFlag());
			comment.setPinTime(pinOn ? new Date() : null);
			GenericityUtil.updateDate(comment);
			dramaVideoCommentDao.updateById(comment);
		} catch (Exception e) {
			log.error("creator comment pin failed creatorId={} commentId={}", account.getId(),
					query.getCommentId(), e);
			throw new RuntimeException(e);
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
		DramaVideoCommentEntity parent = loadOwnedComment(query.getCommentId(), account.getId(), true);
		if (parent == null) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		String sanitized = HtmlSanitizeUtils.plain(query.getCommentInfo());
		if (StringUtils.isEmpty(sanitized)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		SensitiveDecision decision = sensitiveWordService.decide(sanitized);
		if (decision.isReject()) {
			sensitiveRecordService.saveRecord(new SensitiveRecordEntity(
					null, CreatorConstants.COMMENT_CREATOR_USER_ID_PLACEHOLDER, parent.getDramaId(),
					parent.getVideoId(), sanitized, SensitiveSourceEnums.VIDEO_COMMENT.getCode()),
					decision.getCheck());
			return setResultError(I18nUtil.getMessage("sensitive_forbidden"));
		}

		DramaVideoCommentEntity entity = new DramaVideoCommentEntity();
		entity.setDramaId(parent.getDramaId());
		entity.setVideoId(parent.getVideoId());
		entity.setCommentType(CommentTypeEnums.VIDEO.getCode());
		entity.setUserId(CreatorConstants.COMMENT_CREATOR_USER_ID_PLACEHOLDER);
		entity.setFromCreatorId(account.getId());
		entity.setCommentInfo(decision.getContent());
		entity.setScore(null);
		entity.setLikeCount(0);
		entity.setReplyCount(0);
		entity.setParentId(parent.getId());
		// 回复目标：优先观众 uid；父评也是作者时保持占位
		Integer replyToUid = parent.getUserId() == null
				? CreatorConstants.COMMENT_CREATOR_USER_ID_PLACEHOLDER
				: parent.getUserId();
		entity.setReplyToUserId(replyToUid);
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
						SensitiveSourceEnums.VIDEO_COMMENT.getCode()), decision.getCheck());
			}
			if (decision.isHidden()) {
				return setResultSuccess(I18nUtil.getMessage("sensitive_under_review"));
			}
			incrementParentReplyCount(parent.getId());
			addDiscussScore(entity);
			pushInteractMessage(parent.getUserId(), entity.getId(), parent.getId(),
					entity.getDramaId(), entity.getVideoId(), entity.getCommentInfo());
		} catch (Exception e) {
			log.error("creator comment reply failed creatorId={} parentId={}", account.getId(),
					query.getCommentId(), e);
			throw new RuntimeException(e);
		}
		log.info("creator comment reply creatorId={} parentId={} commentId={}", account.getId(),
				parent.getId(), entity.getId());
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

	private CreatorCommentListRespEntity toListResp(CreatorCommentListRow row) {
		CreatorCommentListRespEntity resp = new CreatorCommentListRespEntity();
		resp.setId(row.getId());
		resp.setDramaId(row.getDramaId());
		resp.setDramaTitle(row.getDramaTitle());
		resp.setVideoId(row.getVideoId());
		resp.setSetNum(row.getSetNum());
		boolean isReply = row.getParentId() != null && row.getParentId() > 0;
		resp.setReplyFlag(isReply ? PublicEnums.ONE.getIndex() : PublicEnums.ZERO.getIndex());
		resp.setParentId(row.getParentId() == null ? 0 : row.getParentId());
		resp.setUserId(row.getUserId());
		resp.setFromCreatorId(row.getFromCreatorId());
		resp.setUserName(row.getUserName());
		resp.setAvatar(mediaUrlService.sign(row.getAvatar()));
		resp.setCommentInfo(row.getCommentInfo());
		resp.setLikeCount(row.getLikeCount() == null ? 0 : row.getLikeCount());
		resp.setPinFlag(row.getPinFlag() == null ? 0 : row.getPinFlag());
		resp.setPinTime(row.getPinTime());
		resp.setSetTime(row.getSetTime());
		resp.setContextText(buildContextText(row.getDramaTitle(), row.getSetNum(), isReply));
		if (isReply && row.getParentCommentId() != null) {
			CreatorCommentParentRespEntity parent = new CreatorCommentParentRespEntity();
			parent.setId(row.getParentCommentId());
			parent.setUserName(row.getParentUserName());
			parent.setAvatar(mediaUrlService.sign(row.getParentAvatar()));
			parent.setCommentInfo(row.getParentCommentInfo());
			resp.setParentComment(parent);
		}
		return resp;
	}

	/** 原型文案：在 {剧名} 第xx集 发表了评论 / 回复了评论 */
	private static String buildContextText(String dramaTitle, Integer setNum, boolean isReply) {
		String title = StringUtils.isEmpty(dramaTitle) ? "" : dramaTitle;
		String ep = setNum == null ? "--" : String.format("%02d", setNum);
		String action = isReply ? "回复了评论" : "发表了评论";
		return "在 " + title + " 第" + ep + "集 " + action;
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
			Integer dramaId, Integer videoId, String content) {
		if (toUid == null || toUid <= 0) {
			return;
		}
		Integer fromUid = CreatorConstants.COMMENT_CREATOR_USER_ID_PLACEHOLDER;
		String episodeId = videoId == null || videoId <= 0 ? null : String.valueOf(videoId);
		UserInteractMessageEntity msg = new UserInteractMessageEntity();
		msg.setToUid(toUid);
		msg.setFromUid(fromUid);
		msg.setMessageType(InteractMessageTypeEnums.REPLY_VIDEO.getCode());
		msg.setCommentId(commentId);
		msg.setReplyCommentId(replyCommentId);
		msg.setDramaId(dramaId);
		msg.setEpisodeId(episodeId);
		msg.setContent(content);
		msg.setBizId("creator_video_reply:" + commentId);
		msg.setIsRead(0);
		msg.setStatus(1);
		try {
			GenericityUtil.setDate(msg);
			userInteractMessageDao.insert(msg);
			pushNotifyService.notifyInteract(toUid, fromUid,
					InteractMessageTypeEnums.REPLY_VIDEO.getCode(), msg.getId(), dramaId, episodeId);
		} catch (Exception e) {
			log.warn("creator comment push failed commentId={}: {}", commentId, e.getMessage());
		}
	}
}
