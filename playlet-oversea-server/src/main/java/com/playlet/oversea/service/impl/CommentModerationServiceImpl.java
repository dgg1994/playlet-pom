package com.playlet.oversea.service.impl;

import com.playlet.oversea.dao.drama.DramaAssetDao;
import com.playlet.oversea.dao.drama.DramaDao;
import com.playlet.oversea.dao.drama.DramaVideoCommentDao;
import com.playlet.oversea.dao.drama.UserInteractMessageDao;
import com.playlet.oversea.entity.drama.DramaAssetEntity;
import com.playlet.oversea.entity.drama.DramaEntity;
import com.playlet.oversea.entity.drama.DramaVideoCommentEntity;
import com.playlet.oversea.entity.drama.UserInteractMessageEntity;
import com.playlet.oversea.entity.security.IllegalCommentRecordEntity;
import com.playlet.oversea.enums.CommentTypeEnums;
import com.playlet.oversea.enums.DeleteStateEnum;
import com.playlet.oversea.enums.InteractMessageTypeEnums;
import com.playlet.oversea.enums.PublicEnums;
import com.playlet.oversea.enums.WelfareActionTypeEnums;
import com.playlet.oversea.service.CommentModerationService;
import com.playlet.oversea.service.MedalProgressService;
import com.playlet.oversea.service.PushNotifyService;
import com.playlet.oversea.utils.GenericityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CommentModerationServiceImpl implements CommentModerationService {

	@Autowired
	private DramaVideoCommentDao dramaVideoCommentDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private UserInteractMessageDao userInteractMessageDao;
	@Autowired
	private PushNotifyService pushNotifyService;
	@Autowired
	private MedalProgressService medalProgressService;

	@Override
	public void approveHiddenComment(IllegalCommentRecordEntity record) throws Exception {
		if (record == null || record.getCommentId() == null) {
			return;
		}
		DramaVideoCommentEntity comment = dramaVideoCommentDao.selectById(record.getCommentId());
		if (comment == null) {
			return;
		}
		boolean wasHidden = comment.getDeleteState() != null
				&& DeleteStateEnum.DELETE.getIndex().equals(comment.getDeleteState());
		if (!wasHidden) {
			return;
		}
		comment.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
		GenericityUtil.updateDate(comment);
		dramaVideoCommentDao.updateById(comment);

		boolean isDrama = CommentTypeEnums.isDrama(comment.getCommentType());
		boolean isTopLevel = PublicEnums.ZERO.getIndex().equals(comment.getParentId());
		if (isDrama) {
			addDiscussScoreForDrama(comment);
			if (isTopLevel) {
				DramaEntity drama = dramaDao.selectById(comment.getDramaId());
				pushInteractMessage(comment.getUserId(),
						drama == null ? null : drama.getBelongUser(),
						InteractMessageTypeEnums.COMMENT_DRAMA.getCode(),
						comment.getId(), null, comment.getDramaId(), null,
						comment.getCommentInfo(), "comment:" + comment.getId());
				refreshDramaScoreNum(comment.getDramaId());
				medalProgressService.onAction(comment.getUserId(), WelfareActionTypeEnums.COMMENT, 1,
						"review:" + comment.getId());
			} else {
				incrementParentReplyCount(comment.getParentId());
				DramaVideoCommentEntity parent = dramaVideoCommentDao.selectById(comment.getParentId());
				pushInteractMessage(comment.getUserId(),
						parent == null ? null : parent.getUserId(),
						InteractMessageTypeEnums.REPLY_DRAMA.getCode(),
						comment.getId(), parent == null ? null : parent.getId(),
						comment.getDramaId(), null, comment.getCommentInfo(), "reply:" + comment.getId());
				medalProgressService.onAction(comment.getUserId(), WelfareActionTypeEnums.COMMENT, 1,
						String.valueOf(comment.getId()));
			}
			return;
		}

		addDiscussScoreForVideo(comment);
		if (isTopLevel) {
			DramaEntity drama = dramaDao.selectById(comment.getDramaId());
				pushInteractMessage(comment.getUserId(),
						drama == null ? null : drama.getBelongUser(),
						InteractMessageTypeEnums.COMMENT_VIDEO.getCode(),
						comment.getId(), null, comment.getDramaId(),
						toEpisodeId(comment.getVideoId()),
						comment.getCommentInfo(), "video_comment:" + comment.getId());
			medalProgressService.onAction(comment.getUserId(), WelfareActionTypeEnums.COMMENT, 1,
					String.valueOf(comment.getId()));
		} else {
			incrementParentReplyCount(comment.getParentId());
			DramaVideoCommentEntity parent = dramaVideoCommentDao.selectById(comment.getParentId());
			pushInteractMessage(comment.getUserId(),
					parent == null ? null : parent.getUserId(),
					InteractMessageTypeEnums.REPLY_VIDEO.getCode(),
					comment.getId(), parent == null ? null : parent.getId(),
					comment.getDramaId(), toEpisodeId(comment.getVideoId()),
					comment.getCommentInfo(), "video_reply:" + comment.getId());
			medalProgressService.onAction(comment.getUserId(), WelfareActionTypeEnums.COMMENT, 1,
					String.valueOf(comment.getId()));
		}
	}

	@Override
	public void deleteComment(Integer commentId) throws Exception {
		if (commentId == null) {
			return;
		}
		DramaVideoCommentEntity comment = dramaVideoCommentDao.selectById(commentId);
		if (comment == null) {
			return;
		}
		boolean wasVisible = comment.getDeleteState() == null
				|| DeleteStateEnum.NORMAL.getIndex().equals(comment.getDeleteState());
		if (!wasVisible) {
			return;
		}
		comment.setDeleteState(DeleteStateEnum.DELETE.getIndex());
		GenericityUtil.updateDate(comment);
		dramaVideoCommentDao.updateById(comment);
		if (CommentTypeEnums.isDrama(comment.getCommentType())) {
			decrementDiscussScoreForDrama(comment);
			if (PublicEnums.ZERO.getIndex().equals(comment.getParentId())) {
				refreshDramaScoreNum(comment.getDramaId());
			}
		} else {
			decrementDiscussScoreForVideo(comment);
		}
	}

	/**
	 * 减分
	 * @param entity 视频评论实体
	 */
	private void addDiscussScoreForDrama(DramaVideoCommentEntity entity) {
		DramaEntity dramaEntity = dramaDao.selectById(entity.getDramaId());
		if (dramaEntity != null) {
			Long discuss = dramaEntity.getDiscussScore() == null ? 0L : dramaEntity.getDiscussScore();
			dramaEntity.setDiscussScore(discuss + 1);
			dramaDao.updateById(dramaEntity);
		}
	}

	/**
	 * 加分
	 * @param entity 视频评论实体
	 */
	private void addDiscussScoreForVideo(DramaVideoCommentEntity entity) {
		DramaEntity dramaEntity = dramaDao.selectById(entity.getDramaId());
		if (dramaEntity != null) {
			Long discuss = dramaEntity.getDiscussScore() == null ? 0L : dramaEntity.getDiscussScore();
			dramaEntity.setDiscussScore(discuss + 1);
			dramaDao.updateById(dramaEntity);
		}
		DramaAssetEntity asset = dramaAssetDao.selectById(entity.getVideoId());
		if (asset != null) {
			Long discuss = asset.getDiscussScore() == null ? 0L : asset.getDiscussScore();
			asset.setDiscussScore(discuss + 1);
			dramaAssetDao.updateById(asset);
		}
	}

	/**
	 * 减分
	 * @param entity 评论实体
	 */
	private void decrementDiscussScoreForDrama(DramaVideoCommentEntity entity) {
		DramaEntity dramaEntity = dramaDao.selectById(entity.getDramaId());
		if (dramaEntity != null && dramaEntity.getDiscussScore() != null) {
			dramaEntity.setDiscussScore(Math.max(dramaEntity.getDiscussScore() - 1, 0));
			dramaDao.updateById(dramaEntity);
		}
	}

	/**
	 * 减分
	 * @param entity 评论实体
	 */
	private void decrementDiscussScoreForVideo(DramaVideoCommentEntity entity) {
		DramaEntity dramaEntity = dramaDao.selectById(entity.getDramaId());
		if (dramaEntity != null && dramaEntity.getDiscussScore() != null) {
			dramaEntity.setDiscussScore(Math.max(dramaEntity.getDiscussScore() - 1, 0));
			dramaDao.updateById(dramaEntity);
		}
		DramaAssetEntity asset = dramaAssetDao.selectById(entity.getVideoId());
		if (asset != null && asset.getDiscussScore() != null) {
			asset.setDiscussScore(Math.max(asset.getDiscussScore() - 1, 0));
			dramaAssetDao.updateById(asset);
		}
	}

	/**
	 * 递增父级评论的回复数
	 * @param parentId
	 */
	private void incrementParentReplyCount(Integer parentId) {
		if (parentId == null) {
			return;
		}
		DramaVideoCommentEntity parent = dramaVideoCommentDao.selectById(parentId);
		if (parent == null) {
			return;
		}
		parent.setReplyCount((parent.getReplyCount() == null ? 0 : parent.getReplyCount()) + 1);
		dramaVideoCommentDao.updateById(parent);
	}

	/**
	 * 刷新 dramaId 的评分
	 * @param dramaId
	 */
	private void refreshDramaScoreNum(Integer dramaId) {
		Map<String, Object> agg = dramaVideoCommentDao.avgScoreByDramaId(dramaId, DeleteStateEnum.NORMAL.getIndex());
		DramaEntity dramaEntity = dramaDao.selectById(dramaId);
		if (dramaEntity == null) {
			return;
		}
		double avg = 0D;
		if (agg != null && agg.get("avgScore") != null) {
			avg = new BigDecimal(String.valueOf(agg.get("avgScore")))
					.setScale(1, RoundingMode.HALF_UP)
					.doubleValue();
		}
		dramaEntity.setScoreNum(avg);
		dramaDao.updateById(dramaEntity);
	}

	/**
	 * 推送互动消息
	 * @param fromUid
	 * @param toUid
	 * @param type
	 * @param commentId
	 * @param replyCommentId
	 * @param dramaId
	 * @param episodeId
	 * @param content
	 * @param bizId
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
			log.warn("moderation push failed commentId={}: {}", commentId, e.getMessage());
		}
	}

	/**
	 * 转换为剧集的 episodeId
	 * @param videoId
	 * @return
	 */
	private String toEpisodeId(Integer videoId) {
		if (videoId == null || videoId <= 0) {
			return null;
		}
		return String.valueOf(videoId);
	}
}
