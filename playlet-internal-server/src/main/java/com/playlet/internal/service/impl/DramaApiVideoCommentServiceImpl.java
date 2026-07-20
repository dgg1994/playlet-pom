package com.playlet.internal.service.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.DramaCommentLikeDao;
import com.playlet.internal.dao.drama.DramaVideoCommentDao;
import com.playlet.internal.entity.drama.DramaCommentLikeEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.query.drama.QueryCommentVideoQuery;
import com.playlet.internal.service.DramaApiVideoCommentService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;

@RestController
@Transactional
@CrossOrigin
public class DramaApiVideoCommentServiceImpl extends BaseApiService implements DramaApiVideoCommentService{
	
	@Autowired
	private DramaVideoCommentDao dramaVideoCommentDao;
	
	@Autowired
	private DramaCommentLikeDao dramaCommentLikeDao;

	@Override
	public ResponseBase list(@Valid @RequestBody QueryCommentVideoQuery entity, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			entity.setParentId(PublicEnums.ZERO.getIndex());
			List<DramaVideoCommentEntity> list = dramaVideoCommentDao.getList(entity);
			if(list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					//是否本人评论可删除
					if(uid != null && list.get(i).getUserId().equals(uid)) {
						list.get(i).setIsDelete(PublicEnums.ONE.getIndex());
					}else {
						list.get(i).setIsDelete(PublicEnums.ZERO.getIndex());
					}
					//是否点赞
					if(uid != null) {
						DramaCommentLikeEntity commentLikeEntity = dramaCommentLikeDao.findOne(list.get(i).getId(),uid);
						if(commentLikeEntity != null) {
							list.get(i).setIsLike(PublicEnums.ONE.getIndex());
						}
					}else {
						list.get(i).setIsLike(PublicEnums.ZERO.getIndex());
					}
				}
			}
			PageInfo<DramaVideoCommentEntity> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase replyList(@RequestBody QueryCommentVideoQuery entity, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			List<DramaVideoCommentEntity> list = dramaVideoCommentDao.findParentId(entity.getParentId(),DeleteStateEnum.NORMAL.getIndex());
			if(list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					//是否本人评论可删除
					if(uid != null && list.get(i).getUserId().equals(uid)) {
						list.get(i).setIsDelete(PublicEnums.ONE.getIndex());
					}else {
						list.get(i).setIsDelete(PublicEnums.ZERO.getIndex());
					}
					if(uid != null) {
						DramaCommentLikeEntity commentLikeEntity = dramaCommentLikeDao.findOne(list.get(i).getId(),uid);
						if(commentLikeEntity != null) {
							list.get(i).setIsLike(PublicEnums.ONE.getIndex());
						}
					}else {
						list.get(i).setIsLike(PublicEnums.ZERO.getIndex());
					}
				}
			}
			PageInfo<DramaVideoCommentEntity> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
