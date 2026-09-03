package com.playlet.oversea.service.support;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.api.response.WalletLogPageResp;
import com.playlet.oversea.api.response.WalletTransferReadingResp;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.constants.WalletConstants;
import com.playlet.oversea.dao.wallet.WalletAccountDao;
import com.playlet.oversea.dao.wallet.WalletLogDao;
import com.playlet.oversea.dao.wallet.WalletTransfetListDao;
import com.playlet.oversea.dao.wallet.WalletTransfetRatesDao;
import com.playlet.oversea.dao.wallet.WalletUserDao;
import com.playlet.oversea.entity.wallet.WalletAccountEntity;
import com.playlet.oversea.entity.wallet.WalletLogEntity;
import com.playlet.oversea.entity.wallet.WalletTransfetListEntity;
import com.playlet.oversea.entity.wallet.WalletTransfetRatesEntity;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.enums.WalletLogOperateTypeEnums;
import com.playlet.oversea.enums.WalletLogStatusEnums;
import com.playlet.oversea.enums.WalletLogTradeTypeEnums;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.third.WalletUserService;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.OrderCodeFactory;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResult;
import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 钱包内部转账与账变查询（对齐 onetoken WalletService.transfer / walletLog）。
 */
@Slf4j
@Service
public class WalletTransferService {

	private static final int MONEY_SCALE = 6;

	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private WalletTransfetRatesDao walletTransfetRatesDao;
	@Autowired
	private WalletTransfetListDao walletTransfetListDao;
	@Autowired
	private WalletLogDao walletLogDao;
	@Autowired
	private WalletUserService walletUserService;

	/**
	 * 内部转账：登录用户为发送方，不信任客户端 sendUid。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase transfer(Integer userType, Integer localUid, WalletTransfetListEntity request) {
		try {
			WalletUserEntity senderUser = requireWalletUser(userType, localUid);
			WalletAccountEntity senderAccount = requireWalletAccount(senderUser.getId());

			ResponseBase payPwdResult = walletUserService.verifyPayPassword(senderAccount, request.getPayPassword());
			if (!Constants.HTTP_RES_CODE_200.equals(payPwdResult.getCode())) {
				return payPwdResult;
			}

			WalletUserEntity recipientUser = resolveRecipientUser(request);
			if (recipientUser == null) {
				return setResultError(I18nUtil.getMessage("recipient_account_null"));
			}
			if (senderUser.getWalletUid().equals(recipientUser.getWalletUid())) {
				return setResultError(I18nUtil.getMessage("wallet.transfer_self_forbidden"));
			}
			WalletAccountEntity recipientAccount = walletAccountDao.findByWalletUserId(recipientUser.getId());
			if (recipientAccount == null) {
				return setResultError(I18nUtil.getMessage("recipient_account_null"));
			}

			BigDecimal sendMoney = normalizeMoney(request.getSendMoney());
			if (sendMoney.compareTo(BigDecimal.ZERO) <= 0) {
				return setResultError(I18nUtil.getMessage("wallet.transfer_amount_invalid"));
			}

			BigDecimal transferable = resolveTransferableBalance(senderAccount);
			if (transferable.compareTo(sendMoney) < 0) {
				return setResult(Constants.HTTP_RES_CODE_601, I18nUtil.getMessage("wallet_Balance_null"), null);
			}

			BigDecimal rates = resolveTransferRates();
			BigDecimal handlingFee = sendMoney.multiply(rates).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
			BigDecimal actualMoney = sendMoney.subtract(handlingFee).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
			if (actualMoney.compareTo(BigDecimal.ZERO) <= 0) {
				return setResultError(I18nUtil.getMessage("wallet.transfer_amount_invalid"));
			}

			BigDecimal senderBefore = nz(senderAccount.getAvailableBalance());
			BigDecimal recipientBefore = nz(recipientAccount.getAvailableBalance());
			BigDecimal senderAfter = senderBefore.subtract(sendMoney);
			BigDecimal recipientAfter = recipientBefore.add(actualMoney);

			String orderNo = WalletConstants.TRANSFER_ORDER_PREFIX
					+ OrderCodeFactory.getOrderCode(senderUser.getWalletUid());

			WalletTransfetListEntity record = new WalletTransfetListEntity();
			record.setOrderNo(orderNo);
			record.setSendWalletUid(senderUser.getWalletUid());
			record.setSendEmail(senderUser.getEmail());
			record.setSendForwardBalance(senderBefore);
			record.setSendBackBalance(senderAfter);
			record.setRecipientWalletUid(recipientUser.getWalletUid());
			record.setRecipientEmail(recipientUser.getEmail());
			record.setRecipientForwardBalance(recipientBefore);
			record.setRecipientBackBalance(recipientAfter);
			record.setSendMoney(sendMoney);
			record.setSendRates(rates);
			record.setHandlingFee(handlingFee);
			record.setActualMoney(actualMoney);
			stampNow(record);
			walletTransfetListDao.insert(record);

			insertTransferWalletLog(senderUser, senderAccount, recipientUser, record, handlingFee,
					WalletLogTradeTypeEnums.EXPENDITURE, WalletLogOperateTypeEnums.INTERNAL_TRANSFER_OUT);
			insertTransferWalletLog(recipientUser, recipientAccount, senderUser, record, BigDecimal.ZERO,
					WalletLogTradeTypeEnums.INCOME, WalletLogOperateTypeEnums.INTERNAL_TRANSFER_IN);

			int senderUpdated = walletAccountDao.deductTransferableBalance(senderAccount.getId(), sendMoney);
			if (senderUpdated <= 0) {
				throw new BaseException(I18nUtil.getMessage("wallet_Balance_null"));
			}
			int recipientUpdated = walletAccountDao.addAvailableBalance(recipientAccount.getId(), actualMoney);
			if (recipientUpdated <= 0) {
				throw new BaseException(I18nUtil.getMessage("base_error"));
			}

			log.info("wallet internal transfer orderNo={} sendWalletUid={} recipientWalletUid={} sendMoney={} actualMoney={}",
					orderNo, senderUser.getWalletUid(), recipientUser.getWalletUid(), sendMoney, actualMoney);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("wallet transfer biz error userType={} localUid={}", userType, localUid, e);
			throw e;
		} catch (Exception e) {
			log.error("wallet transfer failed userType={} localUid={}", userType, localUid, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** 转账试算：手续费与实际到账 */
	public ResponseBase transferReading(Integer userType, Integer localUid, BigDecimal sendMoney) {
		WalletUserEntity senderUser = requireWalletUser(userType, localUid);
		WalletAccountEntity senderAccount = requireWalletAccount(senderUser.getId());
		return buildTransferReading(senderAccount, sendMoney);
	}

	/** 管理端按 wallet_uid 试算 */
	public ResponseBase transferReadingByWalletUid(Long walletUid, BigDecimal sendMoney) {
		WalletUserEntity senderUser = walletUserDao.findByWalletUid(walletUid);
		if (senderUser == null) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		WalletAccountEntity senderAccount = requireWalletAccount(senderUser.getId());
		return buildTransferReading(senderAccount, sendMoney);
	}

	private ResponseBase buildTransferReading(WalletAccountEntity senderAccount, BigDecimal sendMoney) {
		BigDecimal amount = normalizeMoney(sendMoney);
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			return setResultError(I18nUtil.getMessage("wallet.transfer_amount_invalid"));
		}

		BigDecimal transferable = resolveTransferableBalance(senderAccount);
		if (transferable.compareTo(amount) <= 0) {
			return setResultError(I18nUtil.getMessage("wallet_Balance_null"));
		}

		BigDecimal rates = resolveTransferRates();
		BigDecimal handlingFee = amount.multiply(rates).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		BigDecimal actualMoney = amount.subtract(handlingFee).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

		WalletTransferReadingResp resp = new WalletTransferReadingResp();
		resp.setSendRates(rates);
		resp.setHandlingFee(handlingFee);
		resp.setActualMoney(actualMoney);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/** 查询当前转账费率配置 */
	public ResponseBase findReading() {
		WalletTransfetRatesEntity ratesEntity = walletTransfetRatesDao.findFirst();
		if (ratesEntity == null) {
			WalletTransfetRatesEntity empty = new WalletTransfetRatesEntity();
			empty.setRates(WalletConstants.ZERO_RATE);
			return setResultSuccess(empty, I18nUtil.getMessage("base_success"));
		}
		return setResultSuccess(ratesEntity, I18nUtil.getMessage("base_success"));
	}

	/** 钱包账变列表（默认当月） */
	public ResponseBase walletLog(Integer userType, Integer localUid, WalletLogEntity query) {
		WalletUserEntity user = requireWalletUser(userType, localUid);
		if (query == null) {
			query = new WalletLogEntity();
		}
		if (StringUtils.isEmpty(query.getYearsMonth())) {
			query.setYearsMonth(new SimpleDateFormat("yyyy-MM").format(new Date()));
		}
		query.setWalletUid(user.getWalletUid());

		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<WalletLogEntity> list = walletLogDao.findByConditions(query);
		if (list != null) {
			for (WalletLogEntity row : list) {
				enrichWalletLogRow(row, user);
			}
		}
		PageInfo<WalletLogEntity> pageInfo = new PageInfo<>(list);

		WalletLogPageResp resp = new WalletLogPageResp();
		resp.setPageInfo(pageInfo);
		resp.setTotalIncome(nz(walletLogDao.sumTotalIncome(query)));
		resp.setTotalExpenses(nz(walletLogDao.sumTotalExpenses(query)));
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	private void enrichWalletLogRow(WalletLogEntity row, WalletUserEntity user) {
		row.setUserEmail(user.getEmail());
		BigDecimal realMoney = nz(row.getRealMoney());
		BigDecimal serviceCharge = nz(row.getServiceCharge());
		row.setRealMoney(realMoney.add(serviceCharge));

		WalletLogOperateTypeEnums operateType = WalletLogOperateTypeEnums.fromCode(row.getOperateType());
		if (operateType != null) {
			row.setTitle(I18nUtil.getMessage(operateType.getI18nKey()));
		}
	}

	private void insertTransferWalletLog(WalletUserEntity owner, WalletAccountEntity ownerAccount,
			WalletUserEntity counterparty, WalletTransfetListEntity transfer, BigDecimal serviceCharge,
			WalletLogTradeTypeEnums tradeType, WalletLogOperateTypeEnums operateType) {
		WalletLogEntity logEntity = new WalletLogEntity();
		logEntity.setOrderNo(transfer.getOrderNo());
		logEntity.setOutOrderNo(transfer.getOrderNo());
		logEntity.setWalletUserId(owner.getId());
		logEntity.setWalletUid(owner.getWalletUid());
		logEntity.setTradeType(tradeType.getCode());
		logEntity.setTitle(I18nUtil.getMessage(operateType.getI18nKey()));
		logEntity.setPrimevalMoney(nz(ownerAccount.getAvailableBalance()));
		logEntity.setPrimevalMoneyUnit(WalletConstants.DEFAULT_CURRENCY);
		logEntity.setRealMoney(transfer.getActualMoney());
		logEntity.setServiceCharge(serviceCharge);
		logEntity.setFormName(transfer.getSendEmail());
		logEntity.setFormAccount(String.valueOf(transfer.getSendWalletUid()));
		logEntity.setToName(String.valueOf(counterparty.getWalletUid()));
		logEntity.setToAccount(counterparty.getEmail());
		logEntity.setStatus(WalletLogStatusEnums.POSTED.getCode());
		logEntity.setOperateType(operateType.getCode());
		stampNow(logEntity);
		walletLogDao.insert(logEntity);
	}

	private WalletUserEntity requireWalletUser(Integer userType, Integer localUid) {
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			throw new BaseException(I18nUtil.getMessage("wallet.not_opened"));
		}
		return user;
	}

	private WalletAccountEntity requireWalletAccount(Long walletUserId) {
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(walletUserId);
		if (account == null) {
			throw new BaseException(I18nUtil.getMessage("wallet.not_opened"));
		}
		return account;
	}

	/** 按收款人邮箱解析钱包用户（转账入参 recipientEmail） */
	private WalletUserEntity resolveRecipientUser(WalletTransfetListEntity request) {
		if (request == null || StringUtils.isEmpty(request.getRecipientEmail())) {
			return null;
		}
		String email = request.getRecipientEmail().trim();
		WalletUserEntity user = walletUserDao.findByEmail(email);
		if (user == null || user.getStatus() == null || user.getStatus() != 1) {
			return null;
		}
		return user;
	}

	private BigDecimal resolveTransferRates() {
		WalletTransfetRatesEntity ratesEntity = walletTransfetRatesDao.findFirst();
		if (ratesEntity == null || ratesEntity.getRates() == null) {
			return WalletConstants.ZERO_RATE;
		}
		return ratesEntity.getRates();
	}

	private static BigDecimal resolveTransferableBalance(WalletAccountEntity account) {
		BigDecimal available = nz(account.getAvailableBalance());
		BigDecimal openFreeze = nz(account.getOpenFreezeBalance());
		BigDecimal transferable = available.subtract(openFreeze);
		if (transferable.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}
		return transferable;
	}

	private static BigDecimal normalizeMoney(BigDecimal money) {
		if (money == null) {
			return BigDecimal.ZERO;
		}
		return money.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
	}

	private static BigDecimal nz(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private static void stampNow(WalletTransfetListEntity entity) {
		Date now = new Date();
		entity.setSetTime(now);
		entity.setGmtModified(now);
	}

	private static void stampNow(WalletLogEntity entity) {
		Date now = new Date();
		entity.setSetTime(now);
		entity.setGmtModified(now);
	}
}
