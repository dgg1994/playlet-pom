package com.playlet.internal.service.creator.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playlet.internal.api.response.CreatorRepresentativeRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.creator.CreatorRepresentativeDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorRepresentativeEntity;
import com.playlet.internal.enums.CreatorIdentityAuditStatusEnums;
import com.playlet.internal.enums.ScriptShelfStatusEnums;
import com.playlet.internal.query.creator.CreatorRepresentativeSaveQuery;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.creator.CreatorRepresentativeService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品管理（创作者代表作）。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorRepresentativeServiceImpl extends BaseApiService implements CreatorRepresentativeService {

	/** 备案分类字典（与原型图一致） */
	private static final List<String> CATEGORIES = Arrays.asList("一类短剧", "二类短剧", "三类短剧");

	@Autowired
	private CreatorRepresentativeDao dao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase list(@RequestBody PageQueryHelperEntity query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		Page<CreatorRepresentativeEntity> page = new Page<>(
				query.getPageNumber() == null ? 1 : query.getPageNumber(),
				query.getPageSize() == null ? 10 : query.getPageSize());
		LambdaQueryWrapper<CreatorRepresentativeEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(CreatorRepresentativeEntity::getCreatorId, account.getId())
				.orderByDesc(CreatorRepresentativeEntity::getGmtModified);
		IPage<CreatorRepresentativeEntity> result = dao.selectPage(page, wrapper);

		List<CreatorRepresentativeRespEntity> list = new ArrayList<>();
		for (CreatorRepresentativeEntity e : result.getRecords()) {
			list.add(toResp(e));
		}
		Map<String, Object> data = new HashMap<>();
		data.put("list", list);
		data.put("total", result.getTotal());
		data.put("pageNumber", result.getCurrent());
		data.put("pageSize", result.getSize());
		return setResultSuccess(data);
	}

	@Override
	public ResponseBase save(@Valid @RequestBody CreatorRepresentativeSaveQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (StringUtils.isEmpty(query.getDramaName())) {
			return setResultError("请填写短剧名称");
		}
		if (StringUtils.isEmpty(query.getWorkLink())) {
			return setResultError("请填写作品链接");
		}
		CreatorRepresentativeEntity entity;
		Date now = new Date();
		if (query.getId() != null) {
			// 修改：校验归属 + 仅「驳回」或「通过」后可改（审核中不可改）
			CreatorRepresentativeEntity exist = dao.selectById(query.getId());
			if (exist == null || !exist.getCreatorId().equals(account.getId())) {
				return setResultError("作品不存在");
			}
			if (CreatorIdentityAuditStatusEnums.REVIEWING.getCode() == exist.getAuditStatus()) {
				return setResultError("审核中不可修改，请等待审核完成");
			}
			entity = exist;
		} else {
			entity = new CreatorRepresentativeEntity();
			entity.setCreatorId(account.getId());
			entity.setDisplayStatus(ScriptShelfStatusEnums.OFF.getCode());
			entity.setAuditStatus(CreatorIdentityAuditStatusEnums.REVIEWING.getCode());
			entity.setSetTime(now);
		}
		entity.setDramaName(trim(query.getDramaName()));
		entity.setCategory(trim(query.getCategory()));
		entity.setCover(trim(query.getCover()));
		entity.setWorkLink(trim(query.getWorkLink()));
		entity.setIntro(trim(query.getIntro()));
		entity.setGmtModified(now);
		try {
			if (entity.getId() == null) {
				dao.insert(entity);
			} else {
				// 修改已驳回 / 已通过的 → 重新进入审核
				if (CreatorIdentityAuditStatusEnums.REJECT.getCode() == entity.getAuditStatus()
						|| CreatorIdentityAuditStatusEnums.PASS.getCode() == entity.getAuditStatus()) {
					entity.setAuditStatus(CreatorIdentityAuditStatusEnums.REVIEWING.getCode());
					entity.setAuditRemark("");
					entity.setDisplayStatus(ScriptShelfStatusEnums.OFF.getCode());
				}
				dao.updateById(entity);
			}
		} catch (Exception e) {
			log.error("representative save creatorId={}", account.getId(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		log.info("representative save creatorId={} id={}", account.getId(), entity.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase delete(@RequestBody CreatorRepresentativeSaveQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query.getId() == null) {
			return setResultError("参数错误");
		}
		CreatorRepresentativeEntity exist = dao.selectById(query.getId());
		if (exist == null || !exist.getCreatorId().equals(account.getId())) {
			return setResultError("作品不存在");
		}
		dao.deleteById(query.getId());
		log.info("representative delete creatorId={} id={}", account.getId(), query.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase toggleShelf(@RequestParam("id") Integer id,
									@RequestParam("on") Boolean on,
									HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorRepresentativeEntity exist = dao.selectById(id);
		if (exist == null || !exist.getCreatorId().equals(account.getId())) {
			return setResultError("作品不存在");
		}
		// 仅审核通过可上架
		if (Boolean.TRUE.equals(on) && CreatorIdentityAuditStatusEnums.PASS.getCode() != exist.getAuditStatus()) {
			return setResultError("仅审核通过的作品可上架");
		}
		exist.setDisplayStatus(Boolean.TRUE.equals(on) ? ScriptShelfStatusEnums.ON.getCode() : ScriptShelfStatusEnums.OFF.getCode());
		exist.setGmtModified(new Date());
		dao.updateById(exist);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase categories(HttpServletRequest request) {
		return setResultSuccess(CATEGORIES);
	}

	@Override
	public ResponseBase detail(@RequestParam("id") Integer id, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorRepresentativeEntity exist = dao.selectById(id);
		if (exist == null || !exist.getCreatorId().equals(account.getId())) {
			return setResultError("作品不存在");
		}
		return setResultSuccess(toResp(exist));
	}

	private CreatorRepresentativeRespEntity toResp(CreatorRepresentativeEntity e) {
		CreatorRepresentativeRespEntity r = new CreatorRepresentativeRespEntity();
		r.setId(e.getId());
		r.setDramaName(e.getDramaName());
		r.setCategory(e.getCategory());
		r.setCover(e.getCover());
		r.setCoverUrl(mediaUrlService.sign(e.getCover()));
		r.setWorkLink(e.getWorkLink());
		r.setIntro(e.getIntro());
		r.setDisplayStatus(e.getDisplayStatus());
		r.setDisplayStatusText(ScriptShelfStatusEnums.getLabel(e.getDisplayStatus()));
		r.setAuditStatus(e.getAuditStatus());
		r.setAuditStatusText(auditStatusText(e.getAuditStatus()));
		r.setAuditRemark(e.getAuditRemark());
		return r;
	}

	private static String auditStatusText(Integer auditStatus) {
		if (auditStatus == null) {
			return "";
		}
		if (CreatorIdentityAuditStatusEnums.REVIEWING.getCode() == auditStatus) {
			return "审核中";
		}
		if (CreatorIdentityAuditStatusEnums.PASS.getCode() == auditStatus) {
			return "审核通过";
		}
		if (CreatorIdentityAuditStatusEnums.REJECT.getCode() == auditStatus) {
			return "审核驳回";
		}
		return "待审核";
	}

	private static String trim(String s) {
		return s == null ? "" : s.trim();
	}
}
