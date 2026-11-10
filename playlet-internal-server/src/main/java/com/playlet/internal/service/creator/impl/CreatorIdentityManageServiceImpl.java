package com.playlet.internal.service.creator.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.CreatorIdentityManageItemResp;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.creator.CreatorIdentityDao;
import com.playlet.internal.dao.creator.CreatorProfileDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorIdentityEntity;
import com.playlet.internal.entity.creator.CreatorProfileEntity;
import com.playlet.internal.enums.CreatorIdentityAuditStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.creator.CreatorIdentityManageService;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管理端创作者身份资质审核实现。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorIdentityManageServiceImpl extends BaseApiService implements CreatorIdentityManageService {

	private static final String[] IDENTITY_NAMES = {"", "编剧", "演员", "导演/制作", "编剧机构"};

	@Autowired
	private CreatorIdentityDao creatorIdentityDao;
	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private CreatorProfileDao creatorProfileDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase findList(@RequestParam(value = "keyword", required = false) String keyword,
								 @RequestParam(value = "identityType", required = false) Integer identityType,
								 @RequestParam(value = "status", required = false) Integer status,
								 @RequestParam(value = "startTime", required = false) String startTime,
								 @RequestParam(value = "endTime", required = false) String endTime,
								 @RequestParam(value = "pageNumber", defaultValue = "1") Integer pageNumber,
								 @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		QueryWrapper<CreatorIdentityEntity> qw = new QueryWrapper<>();
		if (identityType != null) {
			qw.eq("identity_type", identityType);
		}
		if (status != null) {
			qw.eq("audit_status", status);
		}
		if (startTime != null && !startTime.isEmpty()) {
			qw.ge("setTime", startTime);
		}
		if (endTime != null && !endTime.isEmpty()) {
			qw.le("setTime", endTime);
		}
		qw.orderByDesc("id");
		PageHelper.startPage(pageNumber, Math.min(pageSize, 100));
		List<CreatorIdentityEntity> rows = creatorIdentityDao.selectList(qw);
		List<CreatorIdentityManageItemResp> list = new ArrayList<>();
		for (CreatorIdentityEntity e : rows) {
			CreatorAccountEntity account = creatorAccountDao.selectById(e.getCreatorId());
			CreatorProfileEntity profile = account != null ? creatorProfileDao.findByCreatorId(account.getId()) : null;
			CreatorIdentityManageItemResp item = new CreatorIdentityManageItemResp();
			item.setId(e.getId());
			item.setCreatorId(e.getCreatorId());
			item.setCreatorNickname(account != null ? account.getNickname() : "");
			item.setCreatorAvatarUrl(account != null ? mediaUrlService.sign(account.getAvatar()) : "");
			item.setCreatorOrgName(profile != null ? profile.getOrgName() : "");
			item.setIdentityType(e.getIdentityType());
			item.setIdentityTypeName(IDENTITY_NAMES[e.getIdentityType()]);
			item.setRealName(e.getRealName());
			item.setOrgName(e.getOrgName());
			item.setOrgLicenseUrl(mediaUrlService.sign(e.getOrgLicense()));
			item.setStatus(e.getAuditStatus());
			item.setStatusText(statusText(e.getAuditStatus()));
			item.setRejectReason(e.getRejectReason());
			item.setApplyTime(e.getSetTime());
			item.setAuditTime(e.getAuditTime());
			list.add(item);
		}
		PageInfo<CreatorIdentityEntity> page = new PageInfo<>(rows);
		page.setList(null);
		page.setTotal(list.isEmpty() ? 0 : page.getTotal());
		return setResultSuccess(list);
	}

	@Override
	public ResponseBase audit(@RequestParam("id") Integer id,
							  @RequestParam("auditStatus") Integer auditStatus,
							  @RequestParam(value = "rejectReason", required = false) String rejectReason,
							  @RequestParam(value = "auditorId", required = false) Integer auditorId) {
		if (auditStatus == null || (auditStatus != CreatorIdentityAuditStatusEnums.PASS.getCode()
				&& auditStatus != CreatorIdentityAuditStatusEnums.REJECT.getCode())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (CreatorIdentityAuditStatusEnums.REJECT.getCode() == auditStatus
				&& (rejectReason == null || rejectReason.trim().isEmpty())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		CreatorIdentityEntity e = creatorIdentityDao.selectById(id);
		if (e == null) {
			return setResultError(I18nUtil.getMessage("record_not_exist"));
		}
		e.setAuditStatus(auditStatus);
		e.setAuditTime(new Date());
		e.setAuditorId(auditorId);
		if (CreatorIdentityAuditStatusEnums.REJECT.getCode() == auditStatus) {
			e.setRejectReason(rejectReason);
		} else {
			e.setPassTime(new Date());
			e.setRejectReason("");
			// 编剧身份通过视为实名认证通过，提升入驻审核状态
			if (e.getIdentityType() == 1) {
				CreatorProfileEntity profile = creatorProfileDao.findByCreatorId(e.getCreatorId());
				if (profile != null && profile.getAuditStatus() != null && profile.getAuditStatus() != 2) {
					profile.setAuditStatus(2);
					profile.setAuditPassTime(new Date());
					creatorProfileDao.updateById(profile);
				}
			}
		}
		e.setGmtModified(new Date());
		try {
			creatorIdentityDao.updateById(e);
		} catch (Exception ex) {
			throw new BaseException(I18nUtil.getMessage("base_error"), ex);
		}
		log.info("creatorIdentityManage audit id={} status={}", id, auditStatus);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	private static String statusText(int status) {
		for (CreatorIdentityAuditStatusEnums en : CreatorIdentityAuditStatusEnums.values()) {
			if (en.getCode() == status) {
				return en.getLabel();
			}
		}
		return "";
	}
}
