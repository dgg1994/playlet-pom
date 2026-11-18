package com.playlet.internal.service.creator.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.CreatorIdentityManageItemResp;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.creator.CreatorIdentityDao;
import com.playlet.internal.dao.creator.CreatorProfileDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorIdentityEntity;
import com.playlet.internal.entity.creator.CreatorProfileEntity;
import com.playlet.internal.enums.CreatorIdentityAuditStatusEnums;
import com.playlet.internal.enums.CreatorProfileAuditStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.creator.CreatorIdentityManageService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.SysUserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理端创作者身份资质审核实现。
 */
@Slf4j
@RestController
@CrossOrigin
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
		// keyword：按创作者昵称/账号/手机过滤
		if (StringUtils.isNotEmpty(keyword)) {
			List<Integer> creatorIds = creatorAccountDao.findIdsByKeyword(keyword.trim());
			if (creatorIds == null || creatorIds.isEmpty()) {
				PageInfo<CreatorIdentityManageItemResp> empty = new PageInfo<>(Collections.emptyList());
				empty.setPageNum(pageNumber == null ? 1 : pageNumber);
				empty.setPageSize(pageSize == null ? 10 : pageSize);
				empty.setTotal(0);
				return setResultSuccess(empty);
			}
			qw.in("creator_id", creatorIds);
		}
		qw.orderByDesc("id");
		int pn = pageNumber == null || pageNumber < 1 ? 1 : pageNumber;
		int ps = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
		PageHelper.startPage(pn, ps);
		List<CreatorIdentityEntity> rows = creatorIdentityDao.selectList(qw);
		PageInfo<CreatorIdentityEntity> basePage = new PageInfo<>(rows);

		Map<Integer, CreatorAccountEntity> accountMap = loadAccounts(rows);
		Map<Integer, CreatorProfileEntity> profileMap = loadProfiles(accountMap.keySet());

		List<CreatorIdentityManageItemResp> list = new ArrayList<>();
		for (CreatorIdentityEntity e : rows) {
			CreatorAccountEntity account = accountMap.get(e.getCreatorId());
			CreatorProfileEntity profile = account != null ? profileMap.get(account.getId()) : null;
			CreatorIdentityManageItemResp item = new CreatorIdentityManageItemResp();
			item.setId(e.getId());
			item.setCreatorId(e.getCreatorId());
			item.setCreatorNickname(account != null ? account.getNickname() : "");
			item.setCreatorAvatarUrl(account != null ? mediaUrlService.sign(account.getAvatar()) : "");
			item.setCreatorOrgName(profile != null ? profile.getOrgName() : "");
			item.setIdentityType(e.getIdentityType());
			item.setIdentityTypeName(resolveIdentityName(e.getIdentityType()));
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
		PageInfo<CreatorIdentityManageItemResp> page = new PageInfo<>();
		page.setList(list);
		page.setTotal(basePage.getTotal());
		page.setPageNum(basePage.getPageNum());
		page.setPageSize(basePage.getPageSize());
		page.setPages(basePage.getPages());
		return setResultSuccess(page);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase audit(@RequestParam("id") Integer id,
							  @RequestParam("auditStatus") Integer auditStatus,
							  @RequestParam(value = "rejectReason", required = false) String rejectReason,
							  HttpServletRequest request) {
		Integer auditorId = SysUserTokenUtil.resolveAdminId(request);
		if (auditorId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
		}
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
			if (e.getIdentityType() != null && e.getIdentityType() == 1) {
				CreatorProfileEntity profile = creatorProfileDao.findByCreatorId(e.getCreatorId());
				if (profile != null && profile.getAuditStatus() != null
						&& profile.getAuditStatus() != CreatorProfileAuditStatusEnums.APPROVED.getCode()) {
					profile.setAuditStatus(CreatorProfileAuditStatusEnums.APPROVED.getCode());
					profile.setAuditPassTime(new Date());
					try {
						creatorProfileDao.updateById(profile);
					} catch (Exception ex) {
						log.error("creatorIdentityManage update profile failed id={} creatorId={}", id,
								e.getCreatorId(), ex);
						throw new BaseException(I18nUtil.getMessage("base_error"), ex);
					}
				}
			}
		}
		e.setGmtModified(new Date());
		try {
			creatorIdentityDao.updateById(e);
		} catch (Exception ex) {
			log.error("creatorIdentityManage audit failed id={} status={}", id, auditStatus, ex);
			throw new BaseException(I18nUtil.getMessage("base_error"), ex);
		}
		log.info("creatorIdentityManage audit id={} status={} auditorId={}", id, auditStatus, auditorId);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	private Map<Integer, CreatorAccountEntity> loadAccounts(List<CreatorIdentityEntity> rows) {
		Map<Integer, CreatorAccountEntity> map = new HashMap<>();
		if (rows == null || rows.isEmpty()) {
			return map;
		}
		Set<Integer> ids = new HashSet<>();
		for (CreatorIdentityEntity row : rows) {
			if (row != null && row.getCreatorId() != null) {
				ids.add(row.getCreatorId());
			}
		}
		if (ids.isEmpty()) {
			return map;
		}
		List<CreatorAccountEntity> accounts = creatorAccountDao.findByIds(new ArrayList<>(ids));
		if (accounts != null) {
			for (CreatorAccountEntity account : accounts) {
				map.put(account.getId(), account);
			}
		}
		return map;
	}

	private Map<Integer, CreatorProfileEntity> loadProfiles(Set<Integer> creatorIds) {
		Map<Integer, CreatorProfileEntity> map = new HashMap<>();
		if (creatorIds == null || creatorIds.isEmpty()) {
			return map;
		}
		for (Integer creatorId : creatorIds) {
			CreatorProfileEntity profile = creatorProfileDao.findByCreatorId(creatorId);
			if (profile != null) {
				map.put(creatorId, profile);
			}
		}
		return map;
	}

	private static String resolveIdentityName(Integer identityType) {
		if (identityType == null || identityType < 1 || identityType >= IDENTITY_NAMES.length) {
			return "";
		}
		return IDENTITY_NAMES[identityType];
	}

	private static String statusText(Integer status) {
		if (status == null) {
			return "";
		}
		for (CreatorIdentityAuditStatusEnums en : CreatorIdentityAuditStatusEnums.values()) {
			if (en.getCode() == status) {
				return en.getLabel();
			}
		}
		return "";
	}
}
