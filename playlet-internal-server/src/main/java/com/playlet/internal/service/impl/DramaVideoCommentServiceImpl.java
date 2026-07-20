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
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaCommentLikeEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.query.drama.AddDramaVideoCommentQuery;
import com.playlet.internal.query.drama.CommentGiveLikeQuery;
import com.playlet.internal.query.drama.ReplyVideoCommentQuery;
import com.playlet.internal.service.DramaVideoCommentService;
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

	@Override
	public ResponseBase publish(@Valid @RequestBody AddDramaVideoCommentQuery createPay) {
		try {
			DramaVideoCommentEntity entity = new DramaVideoCommentEntity();
			BeanUtils.copyProperties(createPay, entity);
			entity.setParentId(PublicEnums.ZERO.getIndex());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaVideoCommentDao.insert(entity);
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
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaVideoCommentDao.insert(entity);
			//上级评论添加回复量
			DramaVideoCommentEntity commentEntity = dramaVideoCommentDao.selectById(entity.getParentId());
			if(commentEntity != null) {
				commentEntity.setReplyCount(commentEntity.getReplyCount() + 1);
				dramaVideoCommentDao.updateById(commentEntity);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase giveLike(@RequestBody CommentGiveLikeQuery giveLikeQuery) {
		try {
			DramaVideoCommentEntity commentEntity = dramaVideoCommentDao.selectById(giveLikeQuery.getCommentId());
			if(commentEntity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			if(PublicEnums.ONE.getIndex().equals(giveLikeQuery.getOperationType())) {
				//新增记录
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
				GenericityUtil.setDate(commentEntity);
				dramaCommentLikeDao.insert(commentLikeEntity);
				//评论添加点赞量
				commentEntity.setLikeCount(commentEntity.getLikeCount() + 1);
				dramaVideoCommentDao.updateById(commentEntity);
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}else {
				dramaCommentLikeDao.deleteByUser(giveLikeQuery.getCommentId(),giveLikeQuery.getUserId());
				commentEntity.setLikeCount(commentEntity.getLikeCount() - 1);
				dramaVideoCommentDao.updateById(commentEntity);
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase delete(Integer id) {
		try {
			DramaVideoCommentEntity commentEntity = dramaVideoCommentDao.selectById(id);
			if(commentEntity != null) {
				dramaVideoCommentDao.deleteById(id);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
