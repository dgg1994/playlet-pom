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
import com.playlet.internal.dao.drama.DramaCommentDao;
import com.playlet.internal.entity.drama.DramaCommentEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.query.drama.AddDramaCommentQuery;
import com.playlet.internal.query.drama.ReplyCommentQuery;
import com.playlet.internal.service.DramaCommentService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;

@RestController
@Transactional
@CrossOrigin
public class DramaCommentServiceImpl extends BaseApiService implements DramaCommentService{
	
	@Autowired
	private DramaCommentDao dramaCommentDao;

	@Override
	public ResponseBase publish(@Valid @RequestBody AddDramaCommentQuery createPay) {
		try {
			DramaCommentEntity entity = new DramaCommentEntity();
			BeanUtils.copyProperties(createPay, entity);
			entity.setParentId(PublicEnums.ZERO.getIndex());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaCommentDao.insert(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase reply(@Valid @RequestBody ReplyCommentQuery createPay) {
		try {
			DramaCommentEntity entity = new DramaCommentEntity();
			BeanUtils.copyProperties(createPay, entity);
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaCommentDao.insert(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
