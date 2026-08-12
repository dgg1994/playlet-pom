package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.response.IllegalCommentRecordListResp;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.security.IllegalCommentRecordDao;
import com.playlet.internal.dao.system.SysUserDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.security.IllegalCommentRecordEntity;
import com.playlet.internal.entity.system.SysUserEntity;
import com.playlet.internal.enums.IllegalCommentHandleTypeEnums;
import com.playlet.internal.enums.IllegalCommentStatusEnums;
import com.playlet.internal.enums.UserStateEnums;
import com.playlet.internal.filter.JWTAuthenticationFilter;
import com.playlet.internal.query.security.IllegalCommentHandleQuery;
import com.playlet.internal.service.CommentModerationService;
import com.playlet.internal.service.IllegalCommentManageService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class IllegalCommentManageServiceImpl implements IllegalCommentManageService {

	@Autowired
	private IllegalCommentRecordDao illegalCommentRecordDao;
	@Autowired
	private CommentModerationService commentModerationService;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private SysUserDao sysUserDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	@SysLogAnnotation(module = "违规评论管理", type = "POST", remark = "违规记录列表")
	public ResponseBase findList(@RequestBody IllegalCommentRecordEntity entity) {
		if (entity == null) {
			entity = new IllegalCommentRecordEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<IllegalCommentRecordListResp> list = illegalCommentRecordDao.findAdminViewList(entity);
		list.forEach(illegalCommentRecordListResp -> illegalCommentRecordListResp.setUserAvatar(mediaUrlService.sign(illegalCommentRecordListResp.getUserAvatar())));
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "违规评论管理", type = "POST", remark = "违规记录详情")
	public ResponseBase detail(@RequestBody IllegalCommentRecordEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		IllegalCommentRecordEntity row = illegalCommentRecordDao.selectById(entity.getId());
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "违规评论管理", type = "POST", remark = "违规记录处置")
	public ResponseBase handle(@RequestBody IllegalCommentHandleQuery query, HttpServletRequest request) {
		try {
			Integer adminId = resolveAdminId(request);
			if (adminId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (query == null || query.getId() == null || query.getHandleType() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			IllegalCommentHandleTypeEnums handleType = IllegalCommentHandleTypeEnums.fromCode(query.getHandleType());
			if (handleType == null) {
				return setResultError(I18nUtil.getMessage("illegal_comment_handle_invalid"));
			}
			IllegalCommentRecordEntity record = illegalCommentRecordDao.selectById(query.getId());
			if (record == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (record.getStatus() != null && record.getStatus() != IllegalCommentStatusEnums.PENDING.getCode()) {
				return setResultError(I18nUtil.getMessage("illegal_comment_already_handled"));
			}

			switch (handleType) {
				case IGNORE:
					commentModerationService.approveHiddenComment(record);
					break;
				case DELETE_COMMENT:
					if (record.getCommentId() != null) {
						commentModerationService.deleteComment(record.getCommentId());
					}
					break;
				case MUTE_USER:
					disableAppUser(record.getUserId());
					break;
				case FREEZE_ACCOUNT:
					freezeAppUser(record.getUserId());
					break;
				default:
					return setResultError(I18nUtil.getMessage("illegal_comment_handle_invalid"));
			}

			IllegalCommentStatusEnums targetStatus = IllegalCommentStatusEnums.fromHandleType(handleType);
			record.setStatus(targetStatus == null ? IllegalCommentStatusEnums.APPROVED.getCode() : targetStatus.getCode());
			record.setHandleType(handleType.getCode());
			record.setHandlerId(adminId.longValue());
			record.setHandleRemark(trimRemark(query.getHandleRemark()));
			GenericityUtil.updateDate(record);
			illegalCommentRecordDao.updateById(record);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("illegal comment handle failed id={}", query == null ? null : query.getId(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 禁用用户
	 * @param uid
	 * @throws Exception
	 */
	private void disableAppUser(Integer uid) throws Exception {
		if (uid == null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account == null) {
			return;
		}
		account.setUserState(UserStateEnums.DISABLE.getIndex());
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
	}

	/**
	 * 冻结用户
	 * @param uid
	 */
	private void freezeAppUser(Integer uid) {
		if (uid == null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account == null) {
			return;
		}
		long balance = account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		long frozen = account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
		long available = balance - frozen;
		if (available > 0 && available <= Integer.MAX_VALUE) {
			appAccountDao.freezeCoinBalance(uid, (int) available);
		}
		account.setUserState(UserStateEnums.DISABLE.getIndex());
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
	}

	/**
	 * 解析管理员ID
	 * @param request
	 * @return
	 */
	private Integer resolveAdminId(HttpServletRequest request) {
		UsernamePasswordAuthenticationToken token = JWTAuthenticationFilter.getAuthentication(request);
		if (token == null) {
			return null;
		}
		SysUserEntity admin = sysUserDao.findByAcctiveState(token.getName(), UserStateEnums.NORMAL.getIndex());
		return admin == null ? null : admin.getId();
	}

	/**
	 * 去除空格
	 * @param remark
	 * @return
	 */
	private static String trimRemark(String remark) {
		if (remark == null) {
			return null;
		}
		String trimmed = remark.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
