package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.AppUserFreezeQuery;
import com.playlet.internal.api.request.AppUserMuteQuery;
import com.playlet.internal.api.request.AppUserNotifyQuery;
import com.playlet.internal.api.request.AppUserProfileUpdateQuery;
import com.playlet.internal.api.request.AppUserResetPwdQuery;
import com.playlet.internal.api.request.AppUserUidPageQuery;
import com.playlet.internal.api.response.AppUserDetailRespEntity;
import com.playlet.internal.api.response.AppUserListItemEntity;
import com.playlet.internal.api.response.AppUserStatsRespEntity;
import com.playlet.internal.api.response.AppUserWatchHistoryItemEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.AppUserManageConstants;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.account.AppUserStatusLogDao;
import com.playlet.internal.dao.drama.UserWatchHistoryDao;
import com.playlet.internal.dao.member.EpisodeUnlockOrderDao;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.dao.member.UserMemberDao;
import com.playlet.internal.dao.ops.UserPlayDailyDao;
import com.playlet.internal.dao.system.SysUserDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.account.AppUserStatusLogEntity;
import com.playlet.internal.entity.member.MemberOrderEntity;
import com.playlet.internal.entity.member.UserMemberEntity;
import com.playlet.internal.entity.system.SysUserEntity;
import com.playlet.internal.enums.AppUserStatusActionEnums;
import com.playlet.internal.enums.UserStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.account.AppUserManageQuery;
import com.playlet.internal.service.AppUserManageService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.SystemMessageSendService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.PasswordHashUtils;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.SysUserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端 App 用户：列表统计、详情、封禁禁言、子记录。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class AppUserManageServiceImpl implements AppUserManageService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private AppUserStatusLogDao appUserStatusLogDao;
	@Autowired
	private UserMemberDao userMemberDao;
	@Autowired
	private MemberOrderDao memberOrderDao;
	@Autowired
	private EpisodeUnlockOrderDao episodeUnlockOrderDao;
	@Autowired
	private UserWatchHistoryDao userWatchHistoryDao;
	@Autowired
	private UserPlayDailyDao userPlayDailyDao;
	@Autowired
	private SysUserDao sysUserDao;
	@Autowired
	private MediaUrlService mediaUrlService;
	@Autowired
	private SystemMessageSendService systemMessageSendService;

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "GET", remark = "顶部统计")
	public ResponseBase stats() {
		LocalDate today = LocalDate.now(ZONE);
		String start = today.atStartOfDay(ZONE).toLocalDateTime()
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String endExclusive = today.plusDays(1).atStartOfDay(ZONE).toLocalDateTime()
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		AppUserStatsRespEntity resp = new AppUserStatsRespEntity();
		resp.setTotalUsers(appAccountDao.countAllUsers());
		resp.setTodayNewUsers(appAccountDao.countNewUsers(start, endExclusive));
		resp.setPaidUsers(appAccountDao.countPaidUsers());
		resp.setActiveMembers(appAccountDao.countActiveMembers());
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "用户列表")
	public ResponseBase findList(@RequestBody(required = false) AppUserManageQuery query) {
		if (query == null) {
			query = new AppUserManageQuery();
		}
		if (!StringUtils.isEmpty(query.getKeyword())) {
			query.setKeyword(query.getKeyword().trim());
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<AppUserListItemEntity> list = appAccountDao.findAdminUserList(query);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (AppUserListItemEntity item : list) {
			fillListItem(item);
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "GET", remark = "用户详情")
	public ResponseBase detail(@RequestParam("uid") Integer uid) {
		if (uid == null || uid <= 0) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		return setResultSuccess(buildDetail(account), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "编辑资料")
	public ResponseBase updateProfile(@RequestBody AppUserProfileUpdateQuery query, HttpServletRequest request) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (query == null || query.getUid() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AppAccountEntity account = appAccountDao.findByUid(query.getUid());
			if (account == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			AppAccountEntity upd = new AppAccountEntity();
			upd.setId(account.getId());
			if (query.getNickname() != null) {
				upd.setNickname(query.getNickname().trim());
			}
			if (query.getRealName() != null) {
				upd.setRealName(query.getRealName().trim());
			}
			if (query.getAvatar() != null) {
				upd.setAvatar(query.getAvatar().trim());
			}
			if (query.getMobileNumber() != null) {
				upd.setMobileNumber(query.getMobileNumber().trim());
			}
			if (query.getMobilePrefix() != null) {
				upd.setMobilePrefix(query.getMobilePrefix().trim());
			}
			if (query.getGender() != null) {
				upd.setGender(query.getGender());
			}
			if (query.getBirthMonth() != null) {
				upd.setBirthMonth(query.getBirthMonth().trim());
			}
			upd.setGmtModified(new Date());
			appAccountDao.updateById(upd);
			log.info("app user profile update adminId={} uid={}", adminId, query.getUid());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("app user updateProfile biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("app user updateProfile failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "重置密码")
	public ResponseBase resetPassword(@RequestBody AppUserResetPwdQuery query, HttpServletRequest request) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (query == null || query.getUid() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AppAccountEntity account = appAccountDao.findByUid(query.getUid());
			if (account == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String raw = query.getNewPassword();
			boolean generated = StringUtils.isEmpty(raw);
			if (generated) {
				raw = randomPassword(AppUserManageConstants.RESET_PWD_LEN);
			}
			appAccountDao.updatePassword(query.getUid(), PasswordHashUtils.encode(raw));
			AppTokenUtil.invalidateAccountSessions(account);
			log.info("app user resetPassword adminId={} uid={} generated={}", adminId, query.getUid(), generated);
			Map<String, Object> data = new HashMap<>(2);
			if (generated) {
				data.put("tempPassword", raw);
			}
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("app user resetPassword biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("app user resetPassword failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "禁言")
	public ResponseBase mute(@RequestBody AppUserMuteQuery query, HttpServletRequest request) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (query == null || query.getUid() == null
					|| query.getMuteDays() == null || query.getMuteDays() < 1) {
				return setResultError("uid、muteDays 无效");
			}
			AppAccountEntity account = appAccountDao.findByUid(query.getUid());
			if (account == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, query.getMuteDays());
			Date until = cal.getTime();
			appAccountDao.updateMuteUntil(query.getUid(), until);
			writeStatusLog(account, AppUserStatusActionEnums.MUTE, account.getUserState(),
					account.getUserState(), until, query.getReason(), adminId);
			log.info("app user mute adminId={} uid={} muteDays={} until={}",
					adminId, query.getUid(), query.getMuteDays(), until);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("app user mute biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("app user mute failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "解除禁言")
	public ResponseBase unmute(@RequestBody AppUserMuteQuery query, HttpServletRequest request) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (query == null || query.getUid() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AppAccountEntity account = appAccountDao.findByUid(query.getUid());
			if (account == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			appAccountDao.updateMuteUntil(query.getUid(), null);
			writeStatusLog(account, AppUserStatusActionEnums.UNMUTE, account.getUserState(),
					account.getUserState(), null, query.getReason(), adminId);
			log.info("app user unmute adminId={} uid={}", adminId, query.getUid());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("app user unmute biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("app user unmute failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "冻结账号")
	public ResponseBase freeze(@RequestBody AppUserFreezeQuery query, HttpServletRequest request) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (query == null || query.getUid() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AppAccountEntity account = appAccountDao.findByUid(query.getUid());
			if (account == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (UserStateEnums.DISABLE.getIndex().equals(account.getUserState())) {
				return setResultError("账号已冻结");
			}
			Integer from = account.getUserState();
			appAccountDao.updateUserState(query.getUid(), UserStateEnums.DISABLE.getIndex());
			AppTokenUtil.invalidateAccountSessions(account);
			writeStatusLog(account, AppUserStatusActionEnums.FREEZE, from,
					UserStateEnums.DISABLE.getIndex(), account.getMuteUntil(), query.getReason(), adminId);
			log.info("app user freeze adminId={} uid={}", adminId, query.getUid());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("app user freeze biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("app user freeze failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "解冻账号")
	public ResponseBase unfreeze(@RequestBody AppUserFreezeQuery query, HttpServletRequest request) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (query == null || query.getUid() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AppAccountEntity account = appAccountDao.findByUid(query.getUid());
			if (account == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			Integer from = account.getUserState();
			appAccountDao.updateUserState(query.getUid(), UserStateEnums.NORMAL.getIndex());
			writeStatusLog(account, AppUserStatusActionEnums.UNFREEZE, from,
					UserStateEnums.NORMAL.getIndex(), account.getMuteUntil(), query.getReason(), adminId);
			log.info("app user unfreeze adminId={} uid={}", adminId, query.getUid());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("app user unfreeze biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("app user unfreeze failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "发送站内通知")
	public ResponseBase sendNotify(@RequestBody AppUserNotifyQuery query, HttpServletRequest request) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (query == null || query.getUid() == null
					|| StringUtils.isEmpty(query.getTitle()) || StringUtils.isEmpty(query.getContent())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AppAccountEntity account = appAccountDao.findByUid(query.getUid());
			if (account == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String bizId = "ADMIN_NOTIFY_" + query.getUid() + "_"
					+ UUID.randomUUID().toString().replace("-", "");
			boolean ok = systemMessageSendService.sendToUser(query.getUid(),
					AppUserManageConstants.MSG_TYPE_ADMIN_NOTIFY, "zh-cn",
					query.getTitle().trim(), query.getContent().trim(),
					null, null, bizId, "none", null, true);
			if (!ok) {
				return setResultError("发送失败");
			}
			log.info("app user sendNotify adminId={} uid={} bizId={}", adminId, query.getUid(), bizId);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("app user sendNotify biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("app user sendNotify failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "状态流水")
	public ResponseBase statusLogs(@RequestBody AppUserUidPageQuery query) {
		if (query == null || query.getUid() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<AppUserStatusLogEntity> list = appUserStatusLogDao.findByUid(query.getUid());
		if (list == null) {
			list = new ArrayList<>();
		}
		for (AppUserStatusLogEntity row : list) {
			row.setActionLabel(AppUserStatusActionEnums.labelOf(row.getAction()));
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "充值记录")
	public ResponseBase rechargeOrders(@RequestBody AppUserUidPageQuery query) {
		if (query == null || query.getUid() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<MemberOrderEntity> list = memberOrderDao.findPaidByUid(query.getUid());
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "观看记录")
	public ResponseBase watchHistory(@RequestBody AppUserUidPageQuery query) {
		if (query == null || query.getUid() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<AppUserWatchHistoryItemEntity> list = userWatchHistoryDao.findAdminByUid(query.getUid());
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "反馈记录")
	public ResponseBase feedbackList(@RequestBody AppUserUidPageQuery query) {
		if (query == null || query.getUid() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 反馈表尚未落地，占位空分页供前端 Tab 联调
		PageInfo<Object> empty = new PageInfo<>(Collections.emptyList());
		empty.setPageNum(query.getPageNumber());
		empty.setPageSize(query.getPageSize());
		empty.setTotal(0);
		return setResultSuccess(empty, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "App用户管理", type = "POST", remark = "导出用户")
	public void export(@RequestBody(required = false) AppUserManageQuery query, HttpServletResponse response) {
		try {
			if (query == null) {
				query = new AppUserManageQuery();
			}
			if (!StringUtils.isEmpty(query.getKeyword())) {
				query.setKeyword(query.getKeyword().trim());
			}
			PageHelper.startPage(1, AppUserManageConstants.EXPORT_MAX_ROWS);
			List<AppUserListItemEntity> list = appAccountDao.findAdminUserList(query);
			if (list == null) {
				list = new ArrayList<>();
			}
			for (AppUserListItemEntity item : list) {
				fillListItem(item);
			}
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/csv;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=app_users.csv");
			try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
				writer.write('\ufeff');
				writer.write("用户ID,昵称,手机号,实名,来源,会员状态,金币余额,累计充值,账号状态,注册时间,最后登录\n");
				for (AppUserListItemEntity item : list) {
					writer.write(csv(item.getUid()));
					writer.write(',');
					writer.write(csv(item.getNickname()));
					writer.write(',');
					writer.write(csv(item.getMobileMasked()));
					writer.write(',');
					writer.write(csv(item.getRealName()));
					writer.write(',');
					writer.write(csv(item.getSourceChannelLabel()));
					writer.write(',');
					writer.write(csv(item.getMemberStatusLabel()));
					writer.write(',');
					writer.write(csv(item.getCoinBalance()));
					writer.write(',');
					writer.write(csv(item.getTotalRechargeYuan()));
					writer.write(',');
					writer.write(csv(item.getUserStateLabel()));
					writer.write(',');
					writer.write(csv(item.getSetTime()));
					writer.write(',');
					writer.write(csv(item.getLastLoginTime()));
					writer.write('\n');
				}
				writer.flush();
			}
			log.info("app user export rows={}", list.size());
		} catch (Exception e) {
			log.error("app user export failed", e);
			throw new BaseException("导出失败", e);
		}
	}

	private AppUserDetailRespEntity buildDetail(AppAccountEntity account) {
		Integer uid = account.getId();
		AppUserDetailRespEntity resp = new AppUserDetailRespEntity();
		resp.setUid(uid);
		resp.setNickname(account.getNickname());
		resp.setRealName(account.getRealName());
		resp.setAvatarUrl(mediaUrlService.sign(account.getAvatar()));
		resp.setMobileMasked(maskMobile(account.getMobileNumber()));
		resp.setEmailMasked(maskEmail(account.getUserEmail()));
		resp.setSourceChannelLabel(resolveSourceLabel(account.getRegisterSource(), account.getInvitedByUid()));
		resp.setDeviceName(account.getDeviceName());
		resp.setInvitedByUid(account.getInvitedByUid());
		resp.setUserState(account.getUserState());
		resp.setUserStateLabel(UserStateEnums.getValue(account.getUserState() == null ? -1 : account.getUserState()));
		resp.setMuted(isMuted(account.getMuteUntil()));
		resp.setMuteUntil(account.getMuteUntil());
		resp.setSetTime(account.getSetTime());
		resp.setLastLoginTime(account.getLastLoginTime());
		resp.setRegisterDays(calcRegisterDays(account.getSetTime()));

		UserMemberEntity member = userMemberDao.findByUid(uid);
		int memberStatus = resolveMemberStatus(member);
		resp.setMemberStatus(memberStatus);
		resp.setMemberStatusLabel(memberStatusLabel(memberStatus));
		resp.setMemberLevelLabel(memberStatus == AppUserManageConstants.MEMBER_FILTER_NORMAL ? "普通用户" : "VIP会员");
		resp.setMemberActiveTime(member == null ? null : member.getSetTime());
		resp.setMemberExpireTime(member == null ? null : member.getExpireTime());
		resp.setAutoRenew(Boolean.FALSE);

		BigDecimal memberPaid = nullToZero(memberOrderDao.sumPaidAmountByUid(uid));
		BigDecimal unlockPaid = nullToZero(episodeUnlockOrderDao.sumPaidAmountByUid(uid));
		resp.setTotalRechargeYuan(memberPaid);
		resp.setTotalConsumeYuan(memberPaid.add(unlockPaid));

		long balance = account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		long frozen = account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
		resp.setCoinBalance(balance);
		resp.setFrozenCoinBalance(frozen);
		resp.setAvailableCoin(Math.max(balance - frozen, 0L));
		resp.setWatchedDramaCount(userWatchHistoryDao.countDistinctDramaByUid(uid));
		long seconds = userPlayDailyDao.sumPlaySecondsByUid(uid);
		resp.setWatchHours(seconds / 3600L);
		return resp;
	}

	private void fillListItem(AppUserListItemEntity item) {
		if (item == null) {
			return;
		}
		item.setAvatarUrl(mediaUrlService.sign(item.getAvatarUrl()));
		item.setMobileMasked(maskMobile(item.getMobileMasked()));
		item.setSourceChannelLabel(resolveSourceLabel(item.getRegisterSource(), item.getInvitedByUid()));
		item.setMemberStatusLabel(memberStatusLabel(item.getMemberStatus()));
		item.setUserStateLabel(UserStateEnums.getValue(item.getUserState() == null ? -1 : item.getUserState()));
		item.setMuted(isMuted(item.getMuteUntil()));
		if (item.getTotalRechargeYuan() == null) {
			item.setTotalRechargeYuan(BigDecimal.ZERO);
		}
		if (item.getCoinBalance() == null) {
			item.setCoinBalance(0L);
		}
	}

	private void writeStatusLog(AppAccountEntity account, AppUserStatusActionEnums action,
			Integer fromState, Integer toState, Date muteUntil, String reason, Integer adminId) {
		AppUserStatusLogEntity logRow = new AppUserStatusLogEntity();
		logRow.setUid(account.getId());
		logRow.setAction(action.getCode());
		logRow.setFromState(fromState);
		logRow.setToState(toState);
		logRow.setMuteUntil(muteUntil);
		logRow.setReason(trimReason(reason));
		logRow.setOperatorId(adminId);
		logRow.setOperatorName(resolveAdminName(adminId));
		logRow.setSetTime(new Date());
		appUserStatusLogDao.insert(logRow);
	}

	private String resolveAdminName(Integer adminId) {
		if (adminId == null) {
			return AppUserManageConstants.OPERATOR_SYSTEM;
		}
		SysUserEntity admin = sysUserDao.findById(adminId);
		if (admin == null || StringUtils.isEmpty(admin.getAcctive())) {
			return AppUserManageConstants.OPERATOR_ADMIN;
		}
		return admin.getAcctive();
	}

	private static int resolveMemberStatus(UserMemberEntity member) {
		if (member == null) {
			return AppUserManageConstants.MEMBER_FILTER_NORMAL;
		}
		boolean active = Integer.valueOf(1).equals(member.getStatus())
				&& member.getExpireTime() != null
				&& member.getExpireTime().after(new Date());
		if (active) {
			return AppUserManageConstants.MEMBER_FILTER_ACTIVE;
		}
		return AppUserManageConstants.MEMBER_FILTER_EXPIRED;
	}

	private static String memberStatusLabel(Integer status) {
		if (status == null) {
			return AppUserManageConstants.MEMBER_LABEL_NORMAL;
		}
		if (status == AppUserManageConstants.MEMBER_FILTER_ACTIVE) {
			return AppUserManageConstants.MEMBER_LABEL_ACTIVE;
		}
		if (status == AppUserManageConstants.MEMBER_FILTER_EXPIRED) {
			return AppUserManageConstants.MEMBER_LABEL_EXPIRED;
		}
		return AppUserManageConstants.MEMBER_LABEL_NORMAL;
	}

	private static String resolveSourceLabel(Integer registerSource, String invitedByUid) {
		if (!StringUtils.isEmpty(invitedByUid)) {
			return "老用户邀请";
		}
		if (Integer.valueOf(1).equals(registerSource)) {
			return "一键登录";
		}
		if (Integer.valueOf(2).equals(registerSource)) {
			return "账号注册";
		}
		return "未知";
	}

	private static boolean isMuted(Date muteUntil) {
		return muteUntil != null && muteUntil.after(new Date());
	}

	private static Integer calcRegisterDays(Date setTime) {
		if (setTime == null) {
			return null;
		}
		LocalDate reg = setTime.toInstant().atZone(ZONE).toLocalDate();
		return (int) ChronoUnit.DAYS.between(reg, LocalDate.now(ZONE));
	}

	private static String maskMobile(String mobile) {
		if (StringUtils.isEmpty(mobile)) {
			return "";
		}
		String m = mobile.trim();
		if (m.length() < 7) {
			return m.charAt(0) + "****";
		}
		return m.substring(0, 3) + "****" + m.substring(m.length() - 4);
	}

	private static String maskEmail(String email) {
		if (StringUtils.isEmpty(email) || !email.contains("@")) {
			return "";
		}
		int at = email.indexOf('@');
		if (at <= 1) {
			return "***" + email.substring(at);
		}
		return email.charAt(0) + "***" + email.substring(at);
	}

	private static BigDecimal nullToZero(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}

	private static String trimReason(String reason) {
		if (reason == null) {
			return null;
		}
		String t = reason.trim();
		return t.isEmpty() ? null : t;
	}

	private static String randomPassword(int len) {
		final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
		}
		return sb.toString();
	}

	private static String csv(Object val) {
		if (val == null) {
			return "";
		}
		String s = String.valueOf(val);
		if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
			return "\"" + s.replace("\"", "\"\"") + "\"";
		}
		return s;
	}
}
