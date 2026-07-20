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
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.DramaVideoCommentDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.query.drama.AddDramaVideoCommentQuery;
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
			}
			DramaAssetEntity dramaAssetEntity= dramaAssetDao.selectById(entity.getVideoId());
			if(dramaAssetEntity != null) {
				dramaAssetEntity.setDiscussScore(dramaAssetEntity.getDiscussScore() + 1);
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
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
