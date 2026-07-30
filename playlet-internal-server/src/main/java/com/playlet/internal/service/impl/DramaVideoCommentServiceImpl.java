package com.playlet.internal.service.impl;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaCommentLikeDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.DramaVideoCommentDao;
import com.playlet.internal.dao.drama.UserInteractMessageDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaCommentLikeEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.entity.drama.UserInteractMessageEntity;
import com.playlet.internal.enums.CommentTypeEnums;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.InteractMessageTypeEnums;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.enums.WelfareActionTypeEnums;
import com.playlet.internal.query.drama.AddDramaVideoCommentQuery;
import com.playlet.internal.query.drama.CommentGiveLikeQuery;
import com.playlet.internal.query.drama.ReplyVideoCommentQuery;
import com.playlet.internal.service.DramaVideoCommentService;
import com.playlet.internal.service.MedalProgressService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;

@RestController
@Transactional
@CrossOrigin
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

	@Override
	public ResponseBase publish(@Valid @RequestBody AddDramaVideoCommentQuery createPay) {
		try {
			DramaVideoCommentEntity entity = new DramaVideoCommentEntity();
			BeanUtils.copyProperties(createPay, entity);
			entity.setCommentType(CommentTypeEnums.VIDEO.getCode());
			entity.setScore(null);
			entity.setParentId(PublicEnums.ZERO.getIndex());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaVideoCommentDao.insert(entity);
			//视频、短剧添加评论量
			addDiscussScore(entity);
			DramaEntity drama = dramaDao.selectById(entity.getDramaId());
			pushInteractMessage(
					createPay.getUserId(),
					drama == null ? null : drama.getBelongUser(),
					InteractMessageTypeEnums.COMMENT_VIDEO.getCode(),
					entity.getId(),
					null,
					entity.getDramaId(),
					entity.getCommentInfo(),
					"video_comment:" + entity.getId());
			try {
				medalProgressService.onAction(createPay.getUserId(), WelfareActionTypeEnums.COMMENT, 1,
						String.valueOf(entity.getId()));
			} catch (Exception ignore) {
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase reply(@Valid @RequestBody ReplyVideoCommentQuery createPay) {
		try {
			DramaVideoCommentEntity entity = new DramaVideoCommentEntity();
			BeanUtils.copyProperties(createPay, entity);
			entity.setCommentType(CommentTypeEnums.VIDEO.getCode());
			entity.setScore(null);
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaVideoCommentDao.insert(entity);
			//上级评论添加回复量
			DramaVideoCommentEntity commentEntity = dramaVideoCommentDao.selectById(entity.getParentId());
			if(commentEntity != null) {
				commentEntity.setReplyCount(commentEntity.getReplyCount() + 1);
				dramaVideoCommentDao.updateById(commentEntity);
			}
			//视频、短剧添加评论量
			addDiscussScore(entity);
			pushInteractMessage(
					createPay.getUserId(),
					commentEntity == null ? null : commentEntity.getUserId(),
					InteractMessageTypeEnums.REPLY_COMMENT.getCode(),
					entity.getId(),
					commentEntity == null ? null : commentEntity.getId(),
					entity.getDramaId(),
					entity.getCommentInfo(),
					"video_reply:" + entity.getId());
			try {
				medalProgressService.onAction(createPay.getUserId(), WelfareActionTypeEnums.COMMENT, 1,
						String.valueOf(entity.getId()));
			} catch (Exception ignore) {
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
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
						null,
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
			e.printStackTrace();
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
			e.printStackTrace();
			throw new RuntimeException();
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
	 * @param content 消息内容
	 * @param bizId 业务id
	 */
	private void pushInteractMessage(Integer fromUid, Integer toUid, String type,
			Integer commentId, Integer replyCommentId, Integer dramaId, String content, String bizId) {
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
		msg.setContent(content);
		msg.setBizId(bizId);
		msg.setIsRead(0);
		msg.setStatus(1);
		try {
			GenericityUtil.setDate(msg);
			userInteractMessageDao.insert(msg);
		} catch (Exception e) {
			// 幂等与兜底：不影响主业务
		}
	}

}
