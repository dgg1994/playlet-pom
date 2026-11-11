package com.playlet.internal.service.creator.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.CreatorFavoriteItemResp;
import com.playlet.internal.api.response.ScriptFavoriteItemRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.creator.CreatorFavoriteDao;
import com.playlet.internal.dao.creator.CreatorScriptFavoriteDao;
import com.playlet.internal.enums.CreatorCertIdentityEnums;
import com.playlet.internal.query.creator.CreatorFavoriteQuery;
import com.playlet.internal.query.script.ScriptFavoriteQuery;
import com.playlet.internal.service.creator.CreatorFavoriteService;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 我的收藏 - 创作者收藏实现。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorFavoriteServiceImpl extends BaseApiService implements CreatorFavoriteService {

	@Autowired
	private CreatorFavoriteDao creatorFavoriteDao;
	@Autowired
	private CreatorScriptFavoriteDao creatorScriptFavoriteDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase scriptList(ScriptFavoriteQuery query, HttpServletRequest request) {
		Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
		if (creatorId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		ScriptFavoriteQuery q = query == null ? new ScriptFavoriteQuery() : query;
		int pageNumber = q.getPageNumber() == null ? 1 : q.getPageNumber();
		int pageSize = q.getPageSize() == null ? 10 : q.getPageSize();

		PageHelper.startPage(pageNumber, pageSize);
		List<ScriptFavoriteItemRespEntity> list = creatorScriptFavoriteDao.findFavoriteList(creatorId,
				q.getKeyword(), q.getTopic());
		for (ScriptFavoriteItemRespEntity item : list) {
			item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
			item.setPriceModeText(item.getPrice() != null && item.getPrice().doubleValue() > 0 ? "固定报价" : "议价");
			item.setFavoritedAtText(item.getFavoritedAt() == null ? ""
					: new SimpleDateFormat("yyyy-MM-dd").format(item.getFavoritedAt()));
		}
		return setResultSuccess(new PageInfo<>(list));
	}

	@Override
	public ResponseBase addScript(Integer scriptId, HttpServletRequest request) {
		Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
		if (creatorId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (scriptId == null) {
			return setResultError("请选择要收藏的剧本");
		}
		if (creatorScriptFavoriteDao.countScript(scriptId) <= 0) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		creatorScriptFavoriteDao.insertIgnore(creatorId, scriptId);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase cancelScript(Integer scriptId, HttpServletRequest request) {
		Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
		if (creatorId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (scriptId == null) {
			return setResultError("请选择要取消收藏的剧本");
		}
		creatorScriptFavoriteDao.deleteByCreatorAndScript(creatorId, scriptId);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase creatorList(CreatorFavoriteQuery query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorFavoriteQuery q = query == null ? new CreatorFavoriteQuery() : query;
		int pageNumber = q.getPageNumber() == null ? 1 : q.getPageNumber();
		int pageSize = q.getPageSize() == null ? 10 : q.getPageSize();

		PageHelper.startPage(pageNumber, pageSize);
		List<CreatorFavoriteItemResp> list = creatorFavoriteDao.findFavoriteList(uid, q.getKeyword());
		for (CreatorFavoriteItemResp item : list) {
			item.setAvatarUrl(mediaUrlService.sign(item.getAvatarUrl()));
			item.setIdentityText(CreatorCertIdentityEnums.namesOf(item.getIdentityCategory(), item.getIdentityTypes()));
			item.setGenresText(item.getField() == null ? "" : item.getField());
			item.setWorksText(item.getWorks() == null ? "" : item.getWorks());
			// 一期无接单开关字段，默认展示可接单
			item.setAcceptStatus("可接单");
			item.setFavoritedAtText(item.getFavoritedAt() == null ? ""
					: new SimpleDateFormat("yyyy-MM-dd").format(item.getFavoritedAt()));
		}
		return setResultSuccess(new PageInfo<>(list));
	}

	@Override
	public ResponseBase addCreator(Integer creatorId, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (creatorId == null) {
			return setResultError("请选择要收藏的创作者");
		}
		if (creatorId.equals(uid)) {
			return setResultError("不能收藏自己");
		}
		creatorFavoriteDao.insertIgnore(uid, creatorId);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase cancelCreator(Integer creatorId, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (creatorId == null) {
			return setResultError("请选择要取消收藏的创作者");
		}
		creatorFavoriteDao.deleteByUidAndCreator(uid, creatorId);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}
}
