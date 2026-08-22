package com.playlet.oversea.service.impl;


import com.playlet.oversea.api.request.SensitiveRecordEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.drama.*;
import com.playlet.oversea.entity.drama.*;
import com.playlet.oversea.enums.*;
import com.playlet.oversea.query.drama.AddDramaVideoCommentQuery;
import com.playlet.oversea.query.drama.CommentGiveLikeQuery;
import com.playlet.oversea.query.drama.ReplyVideoCommentQuery;
import com.playlet.oversea.security.sensitive.SensitiveDecision;
import com.playlet.oversea.service.*;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.HtmlSanitizeUtils;
import com.playlet.oversea.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
@Slf4j
public class DramaVideoCommentServiceImpl extends BaseApiService implements DramaVideoCommentService{
	
	@Autowired
	private DramaVideoCommentDao dramaVideoCommentDao;
	
	@Autowired
	private DramaDao dramaDao;
	
	@Autowired
	private DramaAssetDao dramaAssetDao;
	
	@Autowired
	private DramaCommentLikeDao dramaCommentLikeDao;

	@Autowired
	private MedalProgressService medalProgressService;
	@Autowired
	private UserInteractMessageDao userInteractMessageDao;
	@Autowired
	private PushNotifyService pushNotifyService;
	@Autowired
	private SensitiveWordService sensitiveWordService;
	@Autowired
	private SensitiveRecordService sensitiveRecordService;

	@Override
	public ResponseBase publish(@Valid @RequestBody AddDramaVideoCommentQuery createPay) {
		try {
			DramaVideoCommentEntity entity = new DramaVideoCommentEntity();
			BeanUtils.copyProperties(createPay, entity);
			String sanitized = HtmlSanitizeUtils.plain(entity.getCommentInfo());
			SensitiveDecision decision = sensitiveWordService.decide(sanitized);
			if (decision.isReject()) {
				sensitiveRecordService.saveRecord(new SensitiveRecordEntity(
						null, createPay.getUserId(), createPay.getDramaId(), createPay.getVideoId(),
						sanitized, SensitiveSourceEnums.VIDEO_COMMENT.getCode()), decision.getCheck());
				return setResultError(I18nUtil.getMessage("sensitive_forbidden"));
			}
			entity.setCommentInfo(decision.getContent());
			entity.setUserName(null);
			entity.setCommentType(CommentTypeEnums.VIDEO.getCode());
			entity.setScore(null);
			entity.setParentId(PublicEnums.ZERO.getIndex());
			entity.setDeleteState(decision.isHidden()
					? DeleteStateEnum.DELETE.getIndex() : DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaVideoCommentDao.insert(entity);
			if (decision.shouldRecord()) {
				sensitiveRecordService.saveRecord(new SensitiveRecordEntity(
						entity.getId(), entity.getUserId(), entity.getDramaId(), entity.getVideoId(),
						entity.getCommentInfo(), SensitiveSourceEnums.VIDEO_COMMENT.getCode()), decision.getCheck());
			}
			if (decision.isHidden()) {
				return setResultSuccess(I18nUtil.getMessage("sensitive_under_review"));
			}

			addDiscussScore(entity);
			DramaEntity drama = dramaDao.selectById(entity.getDramaId());
			pushInteractMessage(
					createPay.getUserId(),
					drama == null ? null : drama.getBelongUser(),
					InteractMessageTypeEnums.COMMENT_VIDEO.getCode(),
					entity.getId(),
					null,
					entity.getDramaId(),
					toEpisodeId(entity.getVideoId()),
					entity.getCommentInfo(),
					"video_comment:" + entity.getId());
			try {
				medalProgressService.onAction(createPay.getUserId(), WelfareActionTypeEnums.COMMENT, 1,
						String.valueOf(entity.getId()));
			} catch (Exception ignore) {
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase reply(@Valid @RequestBody ReplyVideoCommentQuery createPay) {
		try {
			DramaVideoCommentEntity entity = new DramaVideoCommentEntity();
			BeanUtils.copyProperties(createPay, entity);
			String sanitized = HtmlSanitizeUtils.plain(entity.getCommentInfo());
			SensitiveDecision decision = sensitiveWordService.decide(sanitized);
			if (decision.isReject()) {
				sensitiveRecordService.saveRecord(new SensitiveRecordEntity(
						null, createPay.getUserId(), createPay.getDramaId(), createPay.getVideoId(),
						sanitized, SensitiveSourceEnums.VIDEO_COMMENT.getCode()), decision.getCheck());
				return setResultError(I18nUtil.getMessage("sensitive_forbidden"));
			}
			entity.setCommentInfo(decision.getContent());
			entity.setUserName(null);
			entity.setReplyToUserName(null);
			entity.setCommentType(CommentTypeEnums.VIDEO.getCode());
			entity.setScore(null);
			entity.setDeleteState(decision.isHidden()
					? DeleteStateEnum.DELETE.getIndex() : DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaVideoCommentDao.insert(entity);
			DramaVideoCommentEntity commentEntity = dramaVideoCommentDao.selectById(entity.getParentId());
			if (commentEntity != null && !decision.isHidden()) {
				commentEntity.setReplyCount(commentEntity.getReplyCount() + 1);
				dramaVideoCommentDao.updateById(commentEntity);
			}
			if (decision.shouldRecord()) {
				sensitiveRecordService.saveRecord(new SensitiveRecordEntity(
						entity.getId(), entity.getUserId(), entity.getDramaId(), entity.getVideoId(),
						entity.getCommentInfo(), SensitiveSourceEnums.VIDEO_COMMENT.getCode()), decision.getCheck());
			}
			if (decision.isHidden()) {
				return setResultSuccess(I18nUtil.getMessage("sensitive_under_review"));
			}

			addDiscussScore(entity);
			pushInteractMessage(
					createPay.getUserId(),
					commentEntity == null ? null : commentEntity.getUserId(),
					InteractMessageTypeEnums.REPLY_VIDEO.getCode(),
					entity.getId(),
					commentEntity == null ? null : commentEntity.getId(),
					entity.getDramaId(),
					toEpisodeId(entity.getVideoId()),
					entity.getCommentInfo(),
					"video_reply:" + entity.getId());
			try {
				medalProgressService.onAction(createPay.getUserId(), WelfareActionTypeEnums.COMMENT, 1,
						String.valueOf(entity.getId()));
			} catch (Exception ignore) {
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 视频、短剧添加评论量
	 * @param entity
	 */
	public void addDiscussScore(DramaVideoCommentEntity entity){
		//视频、短剧添加评论量
		DramaEntity dramaEntity = dramaDao.selectById(entity.getDramaId());
		if(dramaEntity != null) {
			dramaEntity.setDiscussScore(dramaEntity.getDiscussScore() + 1);
			dramaDao.updateById(dramaEntity);
		}
		DramaAssetEntity dramaAssetEntity= dramaAssetDao.selectById(entity.getVideoId());
		if(dramaAssetEntity != null) {
			dramaAssetEntity.setDiscussScore(dramaAssetEntity.getDiscussScore() + 1);
			dramaAssetDao.updateById(dramaAssetEntity);
		}
	}

	@Override
	public ResponseBase giveLike(@RequestBody CommentGiveLikeQuery giveLikeQuery) {
		try {
			DramaVideoCommentEntity commentEntity = dramaVideoCommentDao.selectById(giveLikeQuery.getCommentId());
			if(commentEntity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			DramaCommentLikeEntity exist = dramaCommentLikeDao.findOne(
					giveLikeQuery.getCommentId(), giveLikeQuery.getUserId());
			if(PublicEnums.ONE.getIndex().equals(giveLikeQuery.getOperationType())) {
				if (exist != null) {
					return setResultSuccess(I18nUtil.getMessage("base_success"));
				}
				DramaCommentLikeEntity commentLikeEntity = new DramaCommentLikeEntity();
				commentLikeEntity.setCommentId(commentEntity.getId());
				commentLikeEntity.setDramaId(commentEntity.getDramaId());
				if(PublicEnums.ZERO.getIndex().equals(commentEntity.getParentId())) {
					commentLikeEntity.setLikeType(PublicEnums.ONE.getIndex());
				}else {
					commentLikeEntity.setLikeType(PublicEnums.TOW.getIndex());
				}
				commentLikeEntity.setUserId(giveLikeQuery.getUserId());
				commentLikeEntity.setVideoId(commentEntity.getVideoId());
				GenericityUtil.setDate(commentLikeEntity);
				dramaCommentLikeDao.insert(commentLikeEntity);
				commentEntity.setLikeCount((commentEntity.getLikeCount() == null ? 0 : commentEntity.getLikeCount()) + 1);
				dramaVideoCommentDao.updateById(commentEntity);
				pushInteractMessage(
						giveLikeQuery.getUserId(),
						commentEntity.getUserId(),
						InteractMessageTypeEnums.LIKE_COMMENT.getCode(),
						commentEntity.getId(),
						null,
						commentEntity.getDramaId(),
						toEpisodeId(commentEntity.getVideoId()),
						commentEntity.getCommentInfo(),
						"video_comment_like:" + commentEntity.getId() + ":" + giveLikeQuery.getUserId());
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}else {
				if (exist == null) {
					return setResultSuccess(I18nUtil.getMessage("base_success"));
				}
				dramaCommentLikeDao.deleteByUser(giveLikeQuery.getCommentId(),giveLikeQuery.getUserId());
				int likeCount = (commentEntity.getLikeCount() == null ? 0 : commentEntity.getLikeCount()) - 1;
				commentEntity.setLikeCount(Math.max(likeCount, 0));
				dramaVideoCommentDao.updateById(commentEntity);
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase delete(Integer id) {
		try {
			DramaVideoCommentEntity commentEntity = dramaVideoCommentDao.selectById(id);
			if(commentEntity != null) {
				dramaVideoCommentDao.deleteById(id);
				//视频、短剧删除评论
				DramaEntity dramaEntity = dramaDao.selectById(commentEntity.getDramaId());
				if(dramaEntity != null) {
					dramaEntity.setDiscussScore(dramaEntity.getDiscussScore() - 1);
					dramaDao.updateById(dramaEntity);
				}
				DramaAssetEntity dramaAssetEntity= dramaAssetDao.selectById(commentEntity.getVideoId());
				if(dramaAssetEntity != null) {
					dramaAssetEntity.setDiscussScore(dramaAssetEntity.getDiscussScore() - 1);
					dramaAssetDao.updateById(dramaAssetEntity);
				}
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 推送消息
	 * @param fromUid 发送用户
	 * @param toUid 接收用户
	 * @param type 消息类型
	 * @param commentId 评论id
	 * @param replyCommentId 回复评论id
	 * @param dramaId 短剧id
	 * @param episodeId 剧集ID（对应 videoId）
	 * @param content 消息内容
	 * @param bizId 业务id
	 */
	private void pushInteractMessage(Integer fromUid, Integer toUid, String type,
			Integer commentId, Integer replyCommentId, Integer dramaId, String episodeId,
			String content, String bizId) {
		if (fromUid == null || toUid == null || fromUid.equals(toUid)) {
			return;
		}
		UserInteractMessageEntity msg = new UserInteractMessageEntity();
		msg.setToUid(toUid);
		msg.setFromUid(fromUid);
		msg.setMessageType(type);
		msg.setCommentId(commentId);
		msg.setReplyCommentId(replyCommentId);
		msg.setDramaId(dramaId);
		msg.setEpisodeId(episodeId);
		msg.setContent(content);
		msg.setBizId(bizId);
		msg.setIsRead(0);
		msg.setStatus(1);
		try {
			GenericityUtil.setDate(msg);
			userInteractMessageDao.insert(msg);
			pushNotifyService.notifyInteract(toUid, fromUid, type, msg.getId(), dramaId, episodeId);
		} catch (Exception e) {
			// 幂等与兜底：不影响主业务
		}
	}

	/** videoId 转 episodeId；剧评/无效时返回 null */
	private String toEpisodeId(Integer videoId) {
		if (videoId == null || videoId <= 0) {
			return null;
		}
		return String.valueOf(videoId);
	}

}
