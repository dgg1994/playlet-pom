package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletAccountDao;
import com.playlet.internal.dao.wallet.WalletLogDao;
import com.playlet.internal.dao.wallet.WalletManualLogDao;
import com.playlet.internal.dao.wallet.WalletUserDao;
import com.playlet.internal.entity.wallet.WalletAccountEntity;
import com.playlet.internal.entity.wallet.WalletLogEntity;
import com.playlet.internal.entity.wallet.WalletManualLogEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.UserStateEnums;
import com.playlet.internal.enums.WalletLogOperateTypeEnums;
import com.playlet.internal.enums.WalletLogStatusEnums;
import com.playlet.internal.enums.WalletLogTradeTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.service.WalletLogManageService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.IpUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端钱包流水与人工充值。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WalletLogManageServiceImpl implements WalletLogManageService {

	private static final String MANUAL_FORM_ACCOUNT = "MANUAL";

	@Autowired
	private WalletLogDao walletLogDao;
	@Autowired
	private WalletManualLogDao walletManualLogDao;
	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private IpUtil ipUtil;

	@Override
	@SysLogAnnotation(module = "钱包管理", type = "POST", remark = "钱包流水")
	public ResponseBase findWalletLog(@RequestBody(required = false) WalletLogEntity entity) {
		if (entity == null) {
			entity = new WalletLogEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletLogEntity> list = walletLogDao.findByConditions(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (WalletLogEntity row : list) {
			enrichWalletLogRow(row);
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "钱包管理", type = "POST", remark = "人工充值")
	public ResponseBase walletTopUp(@RequestBody WalletManualLogEntity entity, HttpServletRequest request) {
		if (entity == null || StringUtils.isEmpty(entity.getUid()) || entity.getTopupAmount() == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		Integer localUid;
		try {
			localUid = Integer.parseInt(entity.getUid().trim());
		} catch (NumberFormatException e) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		AppAccountEntity appUser = appAccountDao.findByUid(localUid);
		if (appUser == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		if (UserStateEnums.LOGOUT.getIndex().equals(appUser.getUserState())) {
			return setResultError(I18nUtil.getMessage("user.account_null"));
		}
		WalletUserEntity walletUser = walletUserDao.findByLocal(WalletConstants.USER_TYPE_APP, localUid);
		if (walletUser == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(walletUser.getId());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		BigDecimal delta = entity.getTopupAmount();
		BigDecimal before = nz(account.getAvailableBalance());
		BigDecimal after = before.add(delta);
		if (after.compareTo(BigDecimal.ZERO) < 0) {
			return setResultError(I18nUtil.getMessage("wallet_Balance_null"));
		}
		String orderNo = OrderCodeFactory.getOrderCode(localUid.longValue());
		Date now = new Date();
		try {
			WalletManualLogEntity manual = new WalletManualLogEntity();
			manual.setOrderNo(orderNo);
			manual.setWalletUserId(walletUser.getId());
			manual.setWalletUid(walletUser.getWalletUid());
			manual.setLocalUid(localUid);
			manual.setUserEmail(walletUser.getEmail());
			manual.setUserTel(walletUser.getMobileNumber());
			manual.setTopupAmount(delta);
			manual.setTopupAmountForward(before);
			manual.setTopupAmountBack(after);
			manual.setOperateUserId(entity.getOperateUserId());
			manual.setOperateUserName(entity.getOperateUserName());
			manual.setOperateUserIp(request == null ? null : ipUtil.getClientIp(request));
			manual.setSetTime(now);
			manual.setGmtModified(now);
			walletManualLogDao.insert(manual);

			WalletLogEntity logRow = new WalletLogEntity();
			logRow.setOrderNo(orderNo);
			logRow.setOutOrderNo(orderNo);
			logRow.setWalletUserId(walletUser.getId());
			logRow.setWalletUid(walletUser.getWalletUid());
			logRow.setPrimevalMoney(before);
			logRow.setPrimevalMoneyUnit(WalletConstants.DEFAULT_CURRENCY);
			logRow.setServiceCharge(BigDecimal.ZERO);
			logRow.setFormAccount(MANUAL_FORM_ACCOUNT);
			logRow.setToAccount(walletUser.getEmail());
			logRow.setStatus(WalletLogStatusEnums.POSTED.getCode());
			logRow.setSetUser(entity.getOperateUserId());
			logRow.setSetUserName(entity.getOperateUserName());
			logRow.setSetTime(now);
			logRow.setGmtModified(now);
			if (delta.compareTo(BigDecimal.ZERO) < 0) {
				logRow.setTradeType(WalletLogTradeTypeEnums.EXPENDITURE.getCode());
				logRow.setRealMoney(delta.abs());
				logRow.setOperateType(WalletLogOperateTypeEnums.SYS_TOP_UP.getCode());
				logRow.setTitle(I18nUtil.getMessage("wallet.log.sys_top_up"));
				walletAccountDao.deductAvailableBalance(account.getId(), delta.abs());
			} else {
				logRow.setTradeType(WalletLogTradeTypeEnums.INCOME.getCode());
				logRow.setRealMoney(delta);
				logRow.setOperateType(WalletLogOperateTypeEnums.SYS_TOP_UP.getCode());
				logRow.setTitle(I18nUtil.getMessage("wallet.log.sys_top_up"));
				walletAccountDao.addAvailableBalance(account.getId(), delta);
			}
			walletLogDao.insert(logRow);
		} catch (BaseException e) {
			log.error("walletTopUp failed localUid={} amount={}", localUid, delta, e);
			throw e;
		} catch (Exception e) {
			log.error("walletTopUp error localUid={} amount={}", localUid, delta, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("walletTopUp success localUid={} orderNo={} amount={}", localUid, orderNo, delta);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	private void enrichWalletLogRow(WalletLogEntity row) {
		if (row == null) {
			return;
		}
		BigDecimal real = nz(row.getRealMoney());
		BigDecimal fee = nz(row.getServiceCharge());
		row.setRealMoney(real.add(fee));
		if (row.getOperateType() != null) {
			WalletLogOperateTypeEnums op = WalletLogOperateTypeEnums.fromCode(row.getOperateType());
			if (op != null) {
				row.setTitle(I18nUtil.getMessage(op.getI18nKey()));
			}
		}
		if (row.getWalletUserId() != null) {
			WalletUserEntity user = walletUserDao.selectById(row.getWalletUserId());
			if (user != null) {
				row.setUserEmail(user.getEmail());
			}
		}
	}

	private static BigDecimal nz(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}
}
