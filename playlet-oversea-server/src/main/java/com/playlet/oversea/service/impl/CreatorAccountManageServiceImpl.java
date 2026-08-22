package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.api.response.CreatorAccountManageItemEntity;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.dao.creator.CreatorAccountDao;
import com.playlet.oversea.dao.welfare.UserWithdrawOrderDao;
import com.playlet.oversea.entity.creator.CreatorAccountEntity;
import com.playlet.oversea.enums.CreatorIdentityTypeEnums;
import com.playlet.oversea.enums.CreatorProfileAuditStatusEnums;
import com.playlet.oversea.enums.UserStateEnums;
import com.playlet.oversea.enums.WithdrawUserTypeEnums;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.query.creator.CreatorAccountManageQuery;
import com.playlet.oversea.service.CreatorAccountManageService;
import com.playlet.oversea.service.MediaUrlService;
import com.playlet.oversea.utils.CreatorTokenUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import com.playlet.oversea.utils.SysUserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 管理端创作者用户：列表查看、软删除（注销）。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorAccountManageServiceImpl implements CreatorAccountManageService {

	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private UserWithdrawOrderDao userWithdrawOrderDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	@SysLogAnnotation(module = "创作者用户管理", type = "POST", remark = "用户列表")
	public ResponseBase findList(@RequestBody(required = false) CreatorAccountManageQuery query) {
		if (query == null) {
			query = new CreatorAccountManageQuery();
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<CreatorAccountManageItemEntity> list = creatorAccountDao.findAdminList(query);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (CreatorAccountManageItemEntity item : list) {
			fillDisplayFields(item);
		}
		PageInfo<CreatorAccountManageItemEntity> pageInfo = new PageInfo<>(list);
		log.info("creator account admin list keyword={} userState={} auditStatus={} total={}",
				query.getKeyword(), query.getUserState(), query.getAuditStatus(), pageInfo.getTotal());
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "创作者用户管理", type = "GET", remark = "删除用户")
	public ResponseBase delete(@RequestParam("id") Integer id, HttpServletRequest request) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (id == null || id <= 0) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			CreatorAccountEntity account = creatorAccountDao.selectById(id);
			if (account == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (UserStateEnums.LOGOUT.getIndex().equals(account.getUserState())) {
				return setResultError(I18nUtil.getMessage("creator_account_already_deleted"));
			}
			long frozen = account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
			if (frozen > 0) {
				return setResultError(I18nUtil.getMessage("creator_account_frozen_balance"));
			}
			int pending = userWithdrawOrderDao.countProcessingByUid(id, WithdrawUserTypeEnums.CREATOR.getCode());
			if (pending > 0) {
				return setResultError(I18nUtil.getMessage("creator_account_withdraw_pending"));
			}
			int rows = creatorAccountDao.updateUserState(id, UserStateEnums.LOGOUT.getIndex());
			if (rows <= 0) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			// 踢下线：吊销 Redis 会话
			CreatorTokenUtil.invalidateSession(account.getUserAccount());
			log.info("creator account deleted adminId={} creatorId={} email={}", adminId, id,
					maskEmail(account.getUserAccount()));
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("creator account delete failed creatorId={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private void fillDisplayFields(CreatorAccountManageItemEntity item) {
		if (item == null) {
			return;
		}
		item.setAvatarUrl(mediaUrlService.sign(item.getAvatarUrl()));
		item.setMobile(formatMobile(item.getMobile()));
		long balance = item.getCoinBalance() == null ? 0L : item.getCoinBalance();
		long frozen = item.getFrozenCoinBalance() == null ? 0L : item.getFrozenCoinBalance();
		item.setAvailableCoin(Math.max(balance - frozen, 0L));
		item.setUserStateLabel(UserStateEnums.getValue(item.getUserState() == null ? -1 : item.getUserState()));
		item.setIdentityTypeLabel(resolveIdentityLabel(item.getIdentityType()));
		item.setAuditStatusLabel(resolveAuditLabel(item.getAuditStatus()));
		if (StringUtils.isEmpty(item.getNickname())) {
			item.setNickname(item.getUserAccount());
		}
	}

	private static String formatMobile(String raw) {
		return StringUtils.isEmpty(raw) ? "" : raw.trim();
	}

	private static String resolveIdentityLabel(Integer type) {
		if (type == null) {
			return "";
		}
		if (CreatorIdentityTypeEnums.PERSONAL.getCode() == type) {
			return CreatorIdentityTypeEnums.PERSONAL.getLabel();
		}
		if (CreatorIdentityTypeEnums.ORG.getCode() == type) {
			return CreatorIdentityTypeEnums.ORG.getLabel();
		}
		return "";
	}

	private static String resolveAuditLabel(Integer status) {
		if (status == null) {
			return "";
		}
		for (CreatorProfileAuditStatusEnums e : CreatorProfileAuditStatusEnums.values()) {
			if (e.getCode() == status) {
				return e.getLabel();
			}
		}
		return "";
	}

	private static String maskEmail(String email) {
		if (StringUtils.isEmpty(email) || !email.contains("@")) {
			return "***";
		}
		int at = email.indexOf('@');
		if (at <= 1) {
			return "***" + email.substring(at);
		}
		return email.charAt(0) + "***" + email.substring(at);
	}
}
