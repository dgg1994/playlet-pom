package com.playlet.internal.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.playlet.internal.service.MediaUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.CommentLocateRespEntity;
import com.playlet.internal.api.response.CommentLocateRespEntity.CommentLocatePageEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.drama.DramaCommentLikeDao;
import com.playlet.internal.dao.drama.DramaVideoCommentDao;
import com.playlet.internal.entity.drama.DramaCommentLikeEntity;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.query.drama.CommentLocateQuery;
import com.playlet.internal.query.drama.QueryCommentVideoQuery;
import com.playlet.internal.service.DramaApiVideoCommentService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;

@RestController
@Transactional
@CrossOrigin
public class DramaApiVideoCommentServiceImpl extends BaseApiService implements DramaApiVideoCommentService {

	@Autowired
	private DramaVideoCommentDao dramaVideoCommentDao;

	@Autowired
	private DramaCommentLikeDao dramaCommentLikeDao;

	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase list(@Valid @RequestBody QueryCommentVideoQuery entity, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			entity.setParentId(PublicEnums.ZERO.getIndex());
			List<DramaVideoCommentEntity> list = dramaVideoCommentDao.getList(entity);
			fillFlags(list, uid);
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
			List<DramaVideoCommentEntity> list = dramaVideoCommentDao.findParentId(
					entity.getParentId(), DeleteStateEnum.NORMAL.getIndex());
			fillFlags(list, uid);
			PageInfo<DramaVideoCommentEntity> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase locate(@Valid @RequestBody CommentLocateQuery query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		int deleteState = DeleteStateEnum.NORMAL.getIndex();
		int parentPageSize = resolvePageSize(query.getParentPageSize());
		int pageSize = resolvePageSize(query.getPageSize());

		DramaVideoCommentEntity target = dramaVideoCommentDao.findByIdWithAvatar(query.getCommentId());
		if (target == null || !Integer.valueOf(deleteState).equals(target.getDeleteState())) {
			return setResultError(I18nUtil.getMessage("comment.not_found"));
		}
		if (target.getVideoId() == null || !target.getVideoId().equals(query.getVideoId())) {
			return setResultError(I18nUtil.getMessage("comment.video_mismatch"));
		}

		CommentLocateRespEntity resp = new CommentLocateRespEntity();
		fillFlagsOne(target, uid);
		resp.setTarget(target);

		Integer parentId = target.getParentId() == null ? PublicEnums.ZERO.getIndex() : target.getParentId();
		boolean isLevel1 = PublicEnums.ZERO.getIndex().equals(parentId);
		resp.setCommentLevel(isLevel1 ? 1 : 2);
		resp.setParentId(parentId);

		if (isLevel1) {
			resp.setParent(null);
			resp.setSiblings(null);
			resp.setParentPage(buildLevel1Page(query.getVideoId(), target, parentPageSize, uid, deleteState));
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		}

		DramaVideoCommentEntity parent = dramaVideoCommentDao.findByIdWithAvatar(parentId);
		if (parent == null || !Integer.valueOf(deleteState).equals(parent.getDeleteState())) {
			return setResultError(I18nUtil.getMessage("comment.parent_not_found"));
		}
		if (parent.getVideoId() == null || !parent.getVideoId().equals(query.getVideoId())) {
			return setResultError(I18nUtil.getMessage("comment.video_mismatch"));
		}
		fillFlagsOne(parent, uid);
		resp.setParent(parent);
		resp.setSiblings(buildReplyPage(parentId, target, pageSize, uid, deleteState));
		resp.setParentPage(buildLevel1Page(query.getVideoId(), parent, parentPageSize, uid, deleteState));
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	private CommentLocatePageEntity buildLevel1Page(Integer videoId, DramaVideoCommentEntity anchor,
			int pageSize, Integer uid, int deleteState) {
		Integer newer = dramaVideoCommentDao.countNewerLevel1(videoId, deleteState,
				anchor.getSetTime(), anchor.getId());
		int rank = newer == null ? 0 : newer;
		int pageNumber = rank / pageSize + 1;
		int targetIndex = rank % pageSize;

		QueryCommentVideoQuery q = new QueryCommentVideoQuery();
		q.setVoideId(videoId);
		q.setDeleteState(deleteState);
		q.setParentId(PublicEnums.ZERO.getIndex());
		PageHelper.startPage(pageNumber, pageSize);
		List<DramaVideoCommentEntity> list = dramaVideoCommentDao.getList(q);
		if (list == null) {
			list = new ArrayList<>();
		}
		fillFlags(list, uid);
		PageInfo<DramaVideoCommentEntity> pageInfo = new PageInfo<>(list);

		CommentLocatePageEntity page = new CommentLocatePageEntity();
		page.setList(list);
		page.setPageNumber(pageNumber);
		page.setPageSize(pageSize);
		page.setTotal(pageInfo.getTotal());
		page.setTargetIndex(resolveTargetIndex(list, anchor.getId(), targetIndex));
		return page;
	}

	private CommentLocatePageEntity buildReplyPage(Integer parentId, DramaVideoCommentEntity anchor,
			int pageSize, Integer uid, int deleteState) {
		Integer newer = dramaVideoCommentDao.countNewerReplies(parentId, deleteState,
				anchor.getSetTime(), anchor.getId());
		int rank = newer == null ? 0 : newer;
		int pageNumber = rank / pageSize + 1;
		int targetIndex = rank % pageSize;

		PageHelper.startPage(pageNumber, pageSize);
		List<DramaVideoCommentEntity> list = dramaVideoCommentDao.findParentId(parentId, deleteState);
		if (list == null) {
			list = new ArrayList<>();
		}
		fillFlags(list, uid);
		PageInfo<DramaVideoCommentEntity> pageInfo = new PageInfo<>(list);

		CommentLocatePageEntity page = new CommentLocatePageEntity();
		page.setList(list);
		page.setPageNumber(pageNumber);
		page.setPageSize(pageSize);
		page.setTotal(pageInfo.getTotal());
		page.setTargetIndex(resolveTargetIndex(list, anchor.getId(), targetIndex));
		return page;
	}

	/** 优先用列表内真实下标，避免并发插入导致 count 与页内容偏差 */
	private static Integer resolveTargetIndex(List<DramaVideoCommentEntity> list, Integer commentId,
			int fallbackIndex) {
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) != null && commentId.equals(list.get(i).getId())) {
					return i;
				}
			}
		}
		return fallbackIndex;
	}

	private static int resolvePageSize(Integer pageSize) {
		if (pageSize == null || pageSize < 1) {
			return Constants.PAGESIZE;
		}
		return pageSize;
	}

	private void fillFlags(List<DramaVideoCommentEntity> list, Integer uid) {
		if (list == null || list.isEmpty()) {
			return;
		}
		for (DramaVideoCommentEntity item : list) {
			fillFlagsOne(item, uid);
		}
	}

	private void fillFlagsOne(DramaVideoCommentEntity item, Integer uid) {
		if (item == null) {
			return;
		}
		if (uid != null && item.getUserId() != null && item.getUserId().equals(uid)) {
			item.setIsDelete(PublicEnums.ONE.getIndex());
		} else {
			item.setIsDelete(PublicEnums.ZERO.getIndex());
		}
		item.setAvatar(mediaUrlService.sign(item.getAvatar()));
		if (uid != null) {
			DramaCommentLikeEntity like = dramaCommentLikeDao.findOne(item.getId(), uid);
			item.setIsLike(like != null ? PublicEnums.ONE.getIndex() : PublicEnums.ZERO.getIndex());
		} else {
			item.setIsLike(PublicEnums.ZERO.getIndex());
		}
	}
}
