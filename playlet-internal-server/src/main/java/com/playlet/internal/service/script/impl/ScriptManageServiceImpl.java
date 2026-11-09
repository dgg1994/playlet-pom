package com.playlet.internal.service.script.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.ScriptManageListRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.ScriptConstants;
import com.playlet.internal.dao.script.ScriptDao;
import com.playlet.internal.entity.script.ScriptEntity;
import com.playlet.internal.enums.ScriptAuditStatusEnums;
import com.playlet.internal.enums.ScriptShelfStatusEnums;
import com.playlet.internal.query.script.ScriptAuditQuery;
import com.playlet.internal.query.script.ScriptManageListQuery;
import com.playlet.internal.query.script.ScriptShelfQuery;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.script.ScriptManageService;
import com.playlet.internal.service.script.ScriptSupportService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.SysUserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * 管理端剧本管理实现。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class ScriptManageServiceImpl extends BaseApiService implements ScriptManageService {

	@Resource
	private ScriptDao scriptDao;
	@Resource
	private MediaUrlService mediaUrlService;
	@Resource
	private ScriptSupportService scriptSupportService;

	@Override
	public ResponseBase findList(@RequestBody ScriptManageListQuery query) {
		if (query == null) {
			query = new ScriptManageListQuery();
		}
		if (query.getTitle() != null) {
			String title = query.getTitle().trim();
			query.setTitle(title.isEmpty() ? null : title);
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<ScriptManageListRespEntity> list = scriptDao.findManageList(query);
		if (list != null) {
			for (ScriptManageListRespEntity item : list) {
				item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
				item.setAuditStatusText(scriptSupportService.auditStatusText(item.getAuditStatus()));
				item.setShelfStatusText(scriptSupportService.shelfStatusText(item.getShelfStatus()));
				item.setSaleStatusText(scriptSupportService.saleStatusText(item.getSaleStatus()));
			}
		}
		return setResultSuccess(new PageInfo<>(list));
	}

	@Override
	public ResponseBase audit(@Valid @RequestBody ScriptAuditQuery query, HttpServletRequest request) {
		Integer adminId = SysUserTokenUtil.resolveAdminId(request);
		if (adminId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query.getAuditStatus() != ScriptAuditStatusEnums.APPROVED.getCode()
				&& query.getAuditStatus() != ScriptAuditStatusEnums.REJECTED.getCode()) {
			return setResultError(Constants.HTTP_RES_CODE_400, "审核结果仅支持通过或驳回");
		}
		if (query.getAuditStatus() == ScriptAuditStatusEnums.REJECTED.getCode()
				&& !StringUtils.hasText(query.getRejectReason())) {
			return setResultError(Constants.HTTP_RES_CODE_400, "驳回原因不能为空");
		}
		// 查询剧本信息
		ScriptEntity script = scriptDao.findByIdIncludeDeleted(query.getId());
		if (script == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		int fromStatus = script.getAuditStatus() == null ? ScriptAuditStatusEnums.PENDING.getCode()
				: script.getAuditStatus();
		int version = script.getVersion() == null ? 0 : script.getVersion();
		Date passTime = query.getAuditStatus() == ScriptAuditStatusEnums.APPROVED.getCode() ? new Date() : null;
		String rejectReason = query.getAuditStatus() == ScriptAuditStatusEnums.REJECTED.getCode()
				? query.getRejectReason() : null;
		// 修改剧本状态
		int rows = scriptDao.casAudit(script.getId(), fromStatus, query.getAuditStatus(),
				rejectReason, adminId, passTime, version);
		if (rows <= 0) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 写审核/上下架日志
		scriptSupportService.writeAuditLog(script.getId(), ScriptConstants.LOG_ACTION_AUDIT,
				fromStatus, query.getAuditStatus(), null, null,
				adminId, ScriptConstants.OPERATOR_ADMIN, rejectReason);
		log.info("script audited adminId={} scriptId={} auditStatus={}", adminId, script.getId(), query.getAuditStatus());
		return setResultSuccess(true);
	}

	@Override
	public ResponseBase shelf(@Valid @RequestBody ScriptShelfQuery query, HttpServletRequest request) {
		Integer adminId = SysUserTokenUtil.resolveAdminId(request);
		if (adminId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query.getShelfStatus() != ScriptShelfStatusEnums.ON.getCode()
				&& query.getShelfStatus() != ScriptShelfStatusEnums.OFF.getCode()) {
			return setResultError(Constants.HTTP_RES_CODE_400, "上架状态不合法");
		}
		// 剧本信息
		ScriptEntity script = scriptDao.findByIdIncludeDeleted(query.getId());
		if (script == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		int fromShelf = script.getShelfStatus() == null ? ScriptShelfStatusEnums.OFF.getCode() : script.getShelfStatus();
		int version = script.getVersion() == null ? 0 : script.getVersion();
		Date shelfTime = query.getShelfStatus() == ScriptShelfStatusEnums.ON.getCode() ? new Date() : null;
		String reason = query.getShelfStatus() == ScriptShelfStatusEnums.OFF.getCode() ? query.getReason() : null;
		// 修改剧本状态
		int rows = scriptDao.casShelf(script.getId(), fromShelf, query.getShelfStatus(), shelfTime, reason, version);
		if (rows <= 0) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 写审核/上下架日志
		scriptSupportService.writeAuditLog(script.getId(), ScriptConstants.LOG_ACTION_SHELF,
				null, null, fromShelf, query.getShelfStatus(),
				adminId, ScriptConstants.OPERATOR_ADMIN, reason);
		log.info("script shelf changed adminId={} scriptId={} shelfStatus={}", adminId, script.getId(), query.getShelfStatus());
		return setResultSuccess(true);
	}
}
