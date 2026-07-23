package com.playlet.internal.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.DramaCommentScoreSummaryEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.DramaCommentLikeDao;
import com.playlet.internal.dao.drama.DramaVideoCommentDao;
import com.playlet.internal.entity.drama.DramaCommentLikeEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.query.drama.QueryDramaCommentQuery;
import com.playlet.internal.service.DramaApiDramaCommentService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;

@RestController
@Transactional
@CrossOrigin
public class DramaApiDramaCommentServiceImpl extends BaseApiService implements DramaApiDramaCommentService {

	@Autowired
	private DramaVideoCommentDao dramaVideoCommentDao;
	@Autowired
	private DramaCommentLikeDao dramaCommentLikeDao;

	@Override
	public ResponseBase list(@Valid @RequestBody QueryDramaCommentQuery entity, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			entity.setParentId(PublicEnums.ZERO.getIndex());
			List<DramaVideoCommentEntity> list = dramaVideoCommentDao.getDramaCommentList(entity);
			fillFlags(list, uid);
			PageInfo<DramaVideoCommentEntity> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase replyList(@RequestBody QueryDramaCommentQuery entity, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			List<DramaVideoCommentEntity> list = dramaVideoCommentDao.findParentId(
					entity.getParentId(), DeleteStateEnum.NORMAL.getIndex());
			fillFlags(list, uid);
			PageInfo<DramaVideoCommentEntity> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase scoreSummary(@RequestBody QueryDramaCommentQuery entity) {
		try {
			if (entity == null || entity.getDramaId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			Map<String, Object> agg = dramaVideoCommentDao.avgScoreByDramaId(
					entity.getDramaId(), DeleteStateEnum.NORMAL.getIndex());
			DramaCommentScoreSummaryEntity summary = new DramaCommentScoreSummaryEntity();
			summary.setDramaId(entity.getDramaId());
			double avg = 0D;
			int count = 0;
			if (agg != null) {
				if (agg.get("avgScore") != null) {
					avg = new BigDecimal(String.valueOf(agg.get("avgScore")))
							.setScale(1, RoundingMode.HALF_UP)
							.doubleValue();
				}
				if (agg.get("scoreCount") != null) {
					count = Integer.parseInt(String.valueOf(agg.get("scoreCount")));
				}
			}
			summary.setAvgScore(avg);
			summary.setScoreCount(count);
			return setResultSuccess(summary, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase mine(@RequestBody QueryDramaCommentQuery entity, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			if (uid == null) {
				return setResultError(I18nUtil.getMessage("login_required"));
			}
			if (entity == null || entity.getDramaId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			DramaVideoCommentEntity row = dramaVideoCommentDao.findUserDramaComment(
					entity.getDramaId(), uid, DeleteStateEnum.NORMAL.getIndex());
			return setResultSuccess(row, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void fillFlags(List<DramaVideoCommentEntity> list, Integer uid) {
		if (list == null || list.isEmpty()) {
			return;
		}
		for (DramaVideoCommentEntity item : list) {
			if (uid != null && item.getUserId() != null && item.getUserId().equals(uid)) {
				item.setIsDelete(PublicEnums.ONE.getIndex());
			} else {
				item.setIsDelete(PublicEnums.ZERO.getIndex());
			}
			if (uid != null) {
				DramaCommentLikeEntity like = dramaCommentLikeDao.findOne(item.getId(), uid);
				item.setIsLike(like != null ? PublicEnums.ONE.getIndex() : PublicEnums.ZERO.getIndex());
			} else {
				item.setIsLike(PublicEnums.ZERO.getIndex());
			}
		}
	}
}
