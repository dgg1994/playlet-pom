package com.playlet.internal.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.DramaCommentLikeDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.DramaVideoCommentDao;
import com.playlet.internal.entity.drama.DramaCommentLikeEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.enums.CommentTypeEnums;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.query.drama.AddDramaCommentQuery;
import com.playlet.internal.query.drama.CommentGiveLikeQuery;
import com.playlet.internal.query.drama.ReplyDramaCommentQuery;
import com.playlet.internal.service.DramaCommentService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;

/**
 * 短剧评论：发布可评分，回复不可评分；与视频评论共用表。
 */
@RestController
@Transactional
@CrossOrigin
public class DramaCommentServiceImpl extends BaseApiService implements DramaCommentService {

	@Autowired
	private DramaVideoCommentDao dramaVideoCommentDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaCommentLikeDao dramaCommentLikeDao;

	@Override
	public ResponseBase publish(@Valid @RequestBody AddDramaCommentQuery createPay) {
		try {
			if (createPay.getScore() == null || createPay.getScore() < 1 || createPay.getScore() > 5) {
				return setResultError(I18nUtil.getMessage("comment_score_invalid"));
			}
			DramaEntity drama = dramaDao.selectById(createPay.getDramaId());
			if (drama == null) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			// 一用户一剧一条一级评论：已存在则更新评分与内容
			DramaVideoCommentEntity exist = dramaVideoCommentDao.findUserDramaComment(
					createPay.getDramaId(), createPay.getUserId(), DeleteStateEnum.NORMAL.getIndex());
			if (exist != null) {
				exist.setScore(createPay.getScore());
				exist.setCommentInfo(createPay.getCommentInfo());
				exist.setUserName(createPay.getUserName());
				GenericityUtil.updateDate(exist);
				dramaVideoCommentDao.updateById(exist);
				refreshDramaScoreNum(createPay.getDramaId());
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			DramaVideoCommentEntity entity = new DramaVideoCommentEntity();
			BeanUtils.copyProperties(createPay, entity);
			entity.setCommentType(CommentTypeEnums.DRAMA.getCode());
			entity.setVideoId(0);
			entity.setParentId(PublicEnums.ZERO.getIndex());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			entity.setLikeCount(0);
			entity.setReplyCount(0);
			GenericityUtil.setDate(entity);
			dramaVideoCommentDao.insert(entity);
			addDiscussScore(entity);
			refreshDramaScoreNum(createPay.getDramaId());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase reply(@Valid @RequestBody ReplyDramaCommentQuery createPay) {
		try {
			DramaVideoCommentEntity parent = dramaVideoCommentDao.selectById(createPay.getParentId());
			if (parent == null || !CommentTypeEnums.isDrama(parent.getCommentType())) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			DramaVideoCommentEntity entity = new DramaVideoCommentEntity();
			BeanUtils.copyProperties(createPay, entity);
			entity.setCommentType(CommentTypeEnums.DRAMA.getCode());
			entity.setVideoId(0);
			entity.setScore(null); // 回复人不能评分
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			entity.setLikeCount(0);
			entity.setReplyCount(0);
			GenericityUtil.setDate(entity);
			dramaVideoCommentDao.insert(entity);
			parent.setReplyCount((parent.getReplyCount() == null ? 0 : parent.getReplyCount()) + 1);
			dramaVideoCommentDao.updateById(parent);
			addDiscussScore(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase giveLike(@RequestBody CommentGiveLikeQuery giveLikeQuery) {
		try {
			DramaVideoCommentEntity commentEntity = dramaVideoCommentDao.selectById(giveLikeQuery.getCommentId());
			if (commentEntity == null || !CommentTypeEnums.isDrama(commentEntity.getCommentType())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			DramaCommentLikeEntity exist = dramaCommentLikeDao.findOne(
					giveLikeQuery.getCommentId(), giveLikeQuery.getUserId());
			if (PublicEnums.ONE.getIndex().equals(giveLikeQuery.getOperationType())) {
				// 已点赞则幂等成功，避免 uk_user_comment 冲突
				if (exist != null) {
					return setResultSuccess(I18nUtil.getMessage("base_success"));
				}
				DramaCommentLikeEntity like = new DramaCommentLikeEntity();
				like.setCommentId(commentEntity.getId());
				like.setDramaId(commentEntity.getDramaId());
				like.setVideoId(0);
				like.setUserId(giveLikeQuery.getUserId());
				if (PublicEnums.ZERO.getIndex().equals(commentEntity.getParentId())) {
					like.setLikeType(PublicEnums.ONE.getIndex());
				} else {
					like.setLikeType(PublicEnums.TOW.getIndex());
				}
				GenericityUtil.setDate(like);
				dramaCommentLikeDao.insert(like);
				commentEntity.setLikeCount((commentEntity.getLikeCount() == null ? 0 : commentEntity.getLikeCount()) + 1);
				dramaVideoCommentDao.updateById(commentEntity);
			} else {
				// 未点赞则幂等成功
				if (exist == null) {
					return setResultSuccess(I18nUtil.getMessage("base_success"));
				}
				dramaCommentLikeDao.deleteByUser(giveLikeQuery.getCommentId(), giveLikeQuery.getUserId());
				int likeCount = (commentEntity.getLikeCount() == null ? 0 : commentEntity.getLikeCount()) - 1;
				commentEntity.setLikeCount(Math.max(likeCount, 0));
				dramaVideoCommentDao.updateById(commentEntity);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase delete(Integer id) {
		try {
			DramaVideoCommentEntity commentEntity = dramaVideoCommentDao.selectById(id);
			if (commentEntity == null || !CommentTypeEnums.isDrama(commentEntity.getCommentType())) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			dramaVideoCommentDao.deleteById(id);
			DramaEntity dramaEntity = dramaDao.selectById(commentEntity.getDramaId());
			if (dramaEntity != null && dramaEntity.getDiscussScore() != null) {
				dramaEntity.setDiscussScore(Math.max(dramaEntity.getDiscussScore() - 1, 0));
				dramaDao.updateById(dramaEntity);
			}
			if (PublicEnums.ZERO.getIndex().equals(commentEntity.getParentId())) {
				refreshDramaScoreNum(commentEntity.getDramaId());
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/** 仅累加短剧评论量，不累加分集 */
	private void addDiscussScore(DramaVideoCommentEntity entity) {
		DramaEntity dramaEntity = dramaDao.selectById(entity.getDramaId());
		if (dramaEntity != null) {
			Long discuss = dramaEntity.getDiscussScore() == null ? 0L : dramaEntity.getDiscussScore();
			dramaEntity.setDiscussScore(discuss + 1);
			dramaDao.updateById(dramaEntity);
		}
	}

	/** 均分  */
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
}
