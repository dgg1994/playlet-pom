package com.playlet.internal.service.third;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import cn.hutool.crypto.digest.DigestUtil;
import com.playlet.internal.api.request.BankcardActiveRequest;
import com.playlet.internal.api.request.BankcardApplyRequest;
import com.playlet.internal.api.request.BankcardCanActiveRequest;
import com.playlet.internal.api.request.BankcardRechargeRequest;
import com.playlet.internal.api.request.BankcardSetPinRequest;
import com.playlet.internal.api.request.BankcardUpdateEmailRequest;
import com.playlet.internal.api.request.BankcardUpdateStatusRequest;
import com.playlet.internal.api.request.BankcardUserIdRequest;
import com.playlet.internal.api.request.KycApplyRequest;
import com.playlet.internal.api.request.WalletApplyCardRequest;
import com.playlet.internal.api.request.WalletBindPayPwdRequest;
import com.playlet.internal.api.response.KycCountryResp;
import com.playlet.internal.api.response.KycStatusResp;
import com.playlet.internal.api.response.ThirdBankcardActiveResp;
import com.playlet.internal.api.response.ThirdBankcardApplyResp;
import com.playlet.internal.api.response.ThirdBankcardBalanceResp;
import com.playlet.internal.api.response.ThirdBankcardCanActiveResp;
import com.playlet.internal.api.response.ThirdBankcardInfoResp;
import com.playlet.internal.api.response.ThirdBankcardPinResp;
import com.playlet.internal.api.response.WalletApplyCardResp;
import com.playlet.internal.api.response.WalletCardItemResp;
import com.playlet.internal.api.response.WalletCardProductItemResp;
import com.playlet.internal.api.response.WalletKycStatusResp;
import com.playlet.internal.api.response.WalletTransactionItemResp;
import com.playlet.internal.api.response.WalletUserInfoResp;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.constants.WalletKycApiStatus;
import com.playlet.internal.dao.wallet.WalletAccountDao;
import com.playlet.internal.dao.wallet.WalletBankcardDao;
import com.playlet.internal.dao.wallet.WalletCardApplyDao;
import com.playlet.internal.dao.wallet.WalletCardProductDao;
import com.playlet.internal.dao.wallet.WalletCardTransactionDao;
import com.playlet.internal.dao.wallet.WalletKycApplyDao;
import com.playlet.internal.dao.wallet.WalletUserDao;
import com.playlet.internal.entity.wallet.WalletAccountEntity;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import com.playlet.internal.entity.wallet.WalletCardTransactionEntity;
import com.playlet.internal.entity.wallet.WalletKycApplyEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.WalletCardApplyStateEnums;
import com.playlet.internal.enums.WalletCardStatusEnums;
import com.playlet.internal.enums.WalletKycStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.support.WalletCardProductService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.PasswordHashUtils;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 钱包用户：注册编排 + 首页读接口（概要 / 卡列表 / 交易）。
 */
@Slf4j
@Service
public class WalletUserService extends BaseApiService {

	private static final int STATUS_NORMAL = 1;
	private static final int ACTIVATION_NOT_YET = 0;
	private static final Pattern PAY_PASSWORD_PATTERN = Pattern.compile(WalletConstants.PAY_PASSWORD_REGEX);

	@Autowired
	private ThirdService thirdService;
	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private WalletBankcardDao walletBankcardDao;
	@Autowired
	private WalletCardApplyDao walletCardApplyDao;
	@Autowired
	private WalletCardTransactionDao walletCardTransactionDao;
	@Autowired
	private WalletCardProductDao walletCardProductDao;
	@Autowired
	private WalletCardProductService walletCardProductService;
	@Autowired
	private WalletKycApplyDao walletKycApplyDao;

	/**
	 * 本地账号注册成功后调用：开通 U 卡三方用户并写入 P0 表。
	 *
	 * @param userType     主体类型：1=C端 2=作家
	 * @param localUid     本地 app_account.id / creator_account.id
	 * @param email        注册邮箱
	 * @param mobilePrefix 区号（可空）
	 * @param mobileNumber 手机号（可空）
	 * @return wallet_user
	 */
	public WalletUserEntity registerOnSignUp(Integer userType, Integer localUid, String email,
			String mobilePrefix, String mobileNumber) {
		if (userType == null || localUid == null) {
			throw new BaseException("钱包注册参数不完整");
		}
		if (StringUtils.isEmpty(email)) {
			throw new BaseException("email不能为空");
		}
		// 幂等：已映射则直接返回
		WalletUserEntity existed = walletUserDao.findByLocal(userType, localUid);
		if (existed != null) {
			log.info("wallet user already exists userType={} localUid={} walletUid={}",
					userType, localUid, existed.getWalletUid());
			ensureWalletAccount(existed);
			return existed;
		}

		String tel = buildTel(mobilePrefix, mobileNumber);
		Long walletUid = thirdService.registerUser(email.trim(), tel);
		Date now = new Date();
		WalletUserEntity user = new WalletUserEntity();
		user.setUserType(userType);
		user.setLocalUid(localUid);
		user.setWalletUid(walletUid);
		user.setEmail(email.trim());
		user.setMobilePrefix(trimToNull(mobilePrefix));
		user.setMobileNumber(trimToNull(mobileNumber));
		user.setStatus(STATUS_NORMAL);
		user.setSetTime(now);
		user.setGmtModified(now);
		try {
			walletUserDao.insert(user);
		} catch (DuplicateKeyException e) {
			// 并发下已插入，回读
			log.warn("wallet user duplicate userType={} localUid={} walletUid={}",
					userType, localUid, walletUid, e);
			WalletUserEntity again = walletUserDao.findByLocal(userType, localUid);
			if (again == null) {
				throw new BaseException("钱包用户写入失败", e);
			}
			ensureWalletAccount(again);
			return again;
		}
		insertWalletAccount(user.getId(), walletUid, now);
		log.info("wallet register success userType={} localUid={} walletUid={}",
				userType, localUid, walletUid);
		return user;
	}

	/**
	 * 查询当前主体的钱包信息（不含支付密码）。
	 */
	public ResponseBase findInfo(Integer userType, Integer localUid) {
		WalletUserInfoResp resp = getInfoOrNull(userType, localUid);
		if (resp == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 组装钱包概要；未开通返回 null（供 findToken 等嵌套，不打断主流程）。
	 */
	public WalletUserInfoResp getInfoOrNull(Integer userType, Integer localUid) {
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return null;
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		WalletUserInfoResp resp = new WalletUserInfoResp();
		resp.setWalletUserId(user.getId());
		resp.setWalletUid(user.getWalletUid());
		resp.setUserType(user.getUserType());
		resp.setEmail(user.getEmail());
		resp.setStatus(user.getStatus());
		resp.setSetTime(user.getSetTime());
		if (account != null) {
			// 账户余额缓存：首页顶部可用余额；权威以三方同步为准
			resp.setAvailableBalance(nvlBalance(account.getAvailableBalance()));
			resp.setFreezeBalance(nvlBalance(account.getFreezeBalance()));
			resp.setOpenFreezeBalance(nvlBalance(account.getOpenFreezeBalance()));
			resp.setCurrency(StringUtils.isEmpty(account.getCurrency())
					? WalletConstants.DEFAULT_CURRENCY : account.getCurrency());
			resp.setBalanceSyncTime(account.getBalanceSyncTime());
			resp.setKycState(account.getKycState());
			resp.setKycStateName(account.getKycStateName());
			resp.setKycApiStatus(account.getKycApiStatus());
			resp.setActivationState(account.getActivationState());
			resp.setPayPasswordSet(!StringUtils.isEmpty(account.getPayPassword()));
			resp.setTronUsdtAddress(account.getTronUsdtAddress());
		} else {
			resp.setAvailableBalance(BigDecimal.ZERO);
			resp.setFreezeBalance(BigDecimal.ZERO);
			resp.setOpenFreezeBalance(BigDecimal.ZERO);
			resp.setCurrency(WalletConstants.DEFAULT_CURRENCY);
			resp.setPayPasswordSet(false);
		}
		return resp;
	}

	/**
	 * 卡片列表：默认卡优先，供首页切换与卡片列表页复用。
	 */
	public ResponseBase listCards(Integer userType, Integer localUid) {
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		List<WalletBankcardEntity> rows = walletBankcardDao.findByWalletUserId(user.getId());
		if (rows == null || rows.isEmpty()) {
			return setResultSuccess(Collections.emptyList(), I18nUtil.getMessage("base_success"));
		}
		List<WalletCardItemResp> items = new ArrayList<>(rows.size());
		Map<Integer, String> cardImgMap = loadCardImgMap(collectCardProductIds(rows));
		for (WalletBankcardEntity row : rows) {
			items.add(toCardItem(row, cardImgMap.get(row.getCardProductId())));
		}
		return setResultSuccess(items, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 商户可用卡产品列表：读本地 wallet_card_product，仅 enable=1。
	 */
	public ResponseBase listCardProducts() {
		List<WalletCardProductItemResp> items = walletCardProductService.listEnabledProducts();
		log.info("wallet card product list size={}", items.size());
		return setResultSuccess(items, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 申请开卡：调三方 apply → 落 wallet_card_apply / wallet_bankcard。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase applyCard(Integer userType, Integer localUid, WalletApplyCardRequest query) {
		if (query == null || query.getProductId() == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_product_required"));
		}
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null
				|| account.getKycState() == null
				|| account.getKycState() != WalletKycStateEnums.SUCCESS_APPROVE.getCode()) {
			return setResultError(I18nUtil.getMessage("wallet.kyc_required"));
		}

		BankcardApplyRequest thirdReq = new BankcardApplyRequest();
		thirdReq.setProductId(query.getProductId());
		thirdReq.setDeliveryAddressId(query.getDeliveryAddressId());
		ThirdBankcardApplyResp third;
		try {
			third = thirdService.applyBankcard(user.getWalletUid(), thirdReq);
		} catch (BaseException e) {
			log.error("wallet apply card third failed walletUid={} productId={}",
					user.getWalletUid(), query.getProductId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet apply card third error walletUid={} productId={}",
					user.getWalletUid(), query.getProductId(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}

		Date now = new Date();
		WalletCardApplyEntity apply = new WalletCardApplyEntity();
		apply.setWalletUserId(user.getId());
		apply.setWalletUid(user.getWalletUid());
		apply.setCardProductId(query.getProductId());
		apply.setApplyState(WalletCardApplyStateEnums.ISSUED.getCode());
		apply.setApplyStateName(WalletCardApplyStateEnums.ISSUED.getLabel());
		apply.setKycState(account.getKycState());
		apply.setRequestOrderId(third.getOrderNo());
		apply.setSetTime(now);
		apply.setGmtModified(now);
		try {
			walletCardApplyDao.insert(apply);
		} catch (DuplicateKeyException e) {
			// 幂等：同 orderNo 已落库
			log.warn("wallet apply card duplicate orderNo={}", third.getOrderNo(), e);
			WalletCardApplyEntity existed = walletCardApplyDao.findByRequestOrderId(third.getOrderNo());
			if (existed != null) {
				apply = existed;
			} else {
				throw new BaseException(I18nUtil.getMessage("base_error"), e);
			}
		} catch (Exception e) {
			log.error("wallet apply card insert apply failed orderNo={}", third.getOrderNo(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}

		Long walletBankcardId = null;
		if (third.getUserBankcardId() != null) {
			WalletBankcardEntity existedCard = walletBankcardDao.findByUserBankcardId(third.getUserBankcardId());
			if (existedCard != null) {
				walletBankcardId = existedCard.getId();
			} else {
				walletBankcardId = insertAppliedBankcard(user, apply.getId(), query.getProductId(), third, now);
			}
			// 账户侧标记已开卡（首次）
			walletAccountDao.markActivated(user.getId());
		}

		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 银行卡是否可激活。
	 */
	public ResponseBase canActiveCard(Integer userType, Integer localUid, BankcardCanActiveRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || StringUtils.isEmpty(query.getCardNo()) || StringUtils.isEmpty(query.getVerifyCode())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		try {
			ThirdBankcardCanActiveResp resp = thirdService.canActiveBankcard(user.getWalletUid(), query);
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("wallet card canActive failed walletUid={}", user.getWalletUid(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card canActive error walletUid={}", user.getWalletUid(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
	}

	/**
	 * 银行卡激活：成功后回写本地卡号与状态。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase activeCard(Integer userType, Integer localUid, BankcardActiveRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getProductId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		ThirdBankcardActiveResp third;
		try {
			third = thirdService.activeBankcard(user.getWalletUid(), query);
		} catch (BaseException e) {
			log.error("wallet card active failed walletUid={} productId={}", user.getWalletUid(),
					query.getProductId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card active error walletUid={}", user.getWalletUid(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		syncCardAfterActive(user, third, query.getCardNo());
		log.info("wallet card active success walletUserId={} userBankcardId={}",
				user.getId(), third.getUserBankcardId());
		return setResultSuccess(third, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 设置 ATM Pin。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase setCardPin(Integer userType, Integer localUid, BankcardSetPinRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getUserBankcardId() == null || StringUtils.isEmpty(query.getPin())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletBankcardEntity card = findOwnedCard(user, query.getUserBankcardId());
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		try {
			thirdService.setBankcardPin(user.getWalletUid(), query);
			walletBankcardDao.updatePinSet(card.getId(), 1);
		} catch (BaseException e) {
			log.error("wallet card setPin failed walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card setPin error walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet card setPin success walletUserId={} userBankcardId={}",
				user.getId(), query.getUserBankcardId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 查询银行卡余额并同步本地缓存。
	 */
	public ResponseBase getCardBalance(Integer userType, Integer localUid, BankcardUserIdRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getUserBankcardId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletBankcardEntity card = findOwnedCard(user, query.getUserBankcardId());
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		ThirdBankcardBalanceResp resp;
		try {
			resp = thirdService.getBankcardBalance(user.getWalletUid(), query.getUserBankcardId());
		} catch (BaseException e) {
			log.error("wallet card balance failed walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card balance error walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		BigDecimal balance = parseBalance(resp == null ? null : resp.getBalance());
		if (balance != null) {
			try {
				walletBankcardDao.updateBalance(card.getId(), balance);
			} catch (Exception e) {
				log.error("wallet card balance cache failed cardId={}", card.getId(), e);
			}
		}
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 银行卡充值：落本地充值流水（处理中），结果由 Webhook 回写。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase rechargeCard(Integer userType, Integer localUid, BankcardRechargeRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getUserBankcardId() == null || query.getAmount() == null
				|| StringUtils.isEmpty(query.getRequestOrderId())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletBankcardEntity card = findOwnedCard(user, query.getUserBankcardId());
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		try {
			thirdService.rechargeBankcard(user.getWalletUid(), query);
		} catch (BaseException e) {
			log.error("wallet card recharge failed walletUid={} requestOrderId={}",
					user.getWalletUid(), query.getRequestOrderId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card recharge error walletUid={} requestOrderId={}",
					user.getWalletUid(), query.getRequestOrderId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		insertRechargeTransaction(user, card, query);
		log.info("wallet card recharge submitted walletUserId={} userBankcardId={} requestOrderId={}",
				user.getId(), query.getUserBankcardId(), query.getRequestOrderId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 更新银行卡状态（冻结/解冻）。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase updateCardStatus(Integer userType, Integer localUid, BankcardUpdateStatusRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getUserBankcardId() == null || query.getEnable() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletBankcardEntity card = findOwnedCard(user, query.getUserBankcardId());
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		try {
			thirdService.updateBankcardStatus(user.getWalletUid(), query);
		} catch (BaseException e) {
			log.error("wallet card updateStatus failed walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card updateStatus error walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		WalletCardStatusEnums status = Boolean.TRUE.equals(query.getEnable())
				? WalletCardStatusEnums.ACTIVE : WalletCardStatusEnums.FREEZE;
		walletBankcardDao.updateCardStatus(card.getId(), status.getCode(), status.getLabel());
		log.info("wallet card updateStatus success walletUserId={} userBankcardId={} enable={}",
				user.getId(), query.getUserBankcardId(), query.getEnable());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 注销银行卡。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase closeCard(Integer userType, Integer localUid, BankcardUserIdRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getUserBankcardId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletBankcardEntity card = findOwnedCard(user, query.getUserBankcardId());
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		try {
			thirdService.closeBankcard(user.getWalletUid(), query.getUserBankcardId());
		} catch (BaseException e) {
			log.error("wallet card close failed walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card close error walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		walletBankcardDao.updateCardStatus(card.getId(),
				WalletCardStatusEnums.CLOSED.getCode(), WalletCardStatusEnums.CLOSED.getLabel());
		log.info("wallet card close success walletUserId={} userBankcardId={}",
				user.getId(), query.getUserBankcardId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 查询银行卡信息（含 cvv/明文卡号等敏感信息，按需展示）。
	 */
	public ResponseBase getCardInfo(Integer userType, Integer localUid, BankcardUserIdRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getUserBankcardId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletBankcardEntity card = findOwnedCard(user, query.getUserBankcardId());
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		try {
			ThirdBankcardInfoResp resp = thirdService.getBankcardInfo(user.getWalletUid(), query.getUserBankcardId());
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("wallet card info failed walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card info error walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
	}

	/**
	 * 更新银行卡邮箱。
	 */
	public ResponseBase updateCardEmail(Integer userType, Integer localUid, BankcardUpdateEmailRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getUserBankcardId() == null || StringUtils.isEmpty(query.getEmail())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletBankcardEntity card = findOwnedCard(user, query.getUserBankcardId());
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		try {
			thirdService.updateBankcardEmail(user.getWalletUid(), query);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("wallet card updateEmail failed walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card updateEmail error walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
	}

	/**
	 * 查询 Pin（三方返回 AES 密文）。
	 */
	public ResponseBase queryCardPin(Integer userType, Integer localUid, BankcardUserIdRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getUserBankcardId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletBankcardEntity card = findOwnedCard(user, query.getUserBankcardId());
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		try {
			ThirdBankcardPinResp resp = thirdService.queryBankcardPin(user.getWalletUid(), query.getUserBankcardId());
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("wallet card queryPin failed walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card queryPin error walletUid={} userBankcardId={}",
					user.getWalletUid(), query.getUserBankcardId(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
	}

	/** 申请成功后写入本地卡记录 */
	private Long insertAppliedBankcard(WalletUserEntity user, Long applyId, Integer productId,
			ThirdBankcardApplyResp third, Date now) {
		WalletBankcardEntity card = new WalletBankcardEntity();
		card.setWalletUserId(user.getId());
		card.setWalletUid(user.getWalletUid());
		card.setCardApplyId(applyId);
		card.setCardProductId(productId);
		card.setUserBankcardId(third.getUserBankcardId());
		card.setCardNo(third.getCardNo());
		card.setCurrency(WalletConstants.DEFAULT_CURRENCY);
		// 申请刚成功：待激活
		card.setCardStatus(WalletCardStatusEnums.WAIT_ACTIVE.getCode());
		card.setCardStatusName(WalletCardStatusEnums.WAIT_ACTIVE.getLabel());
		card.setBalance(BigDecimal.ZERO);
		card.setPinSet(0);
		card.setIsDefault(WalletConstants.CARD_DEFAULT_NO);
		card.setApplyOrderNo(third.getOrderNo());
		card.setSetTime(now);
		card.setGmtModified(now);
		// 若尚无默认卡，设为默认
		if (walletBankcardDao.findDefaultByWalletUserId(user.getId()) == null) {
			card.setIsDefault(WalletConstants.CARD_DEFAULT_YES);
		}
		try {
			walletBankcardDao.insert(card);
			return card.getId();
		} catch (DuplicateKeyException e) {
			log.warn("wallet bankcard duplicate userBankcardId={}", third.getUserBankcardId(), e);
			WalletBankcardEntity again = walletBankcardDao.findByUserBankcardId(third.getUserBankcardId());
			return again == null ? null : again.getId();
		} catch (Exception e) {
			log.error("wallet bankcard insert failed userBankcardId={}", third.getUserBankcardId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/**
	 * 交易记录分页：首页传较小 pageSize，点「全部」继续翻页。
	 */
	public ResponseBase listTransactions(Integer userType, Integer localUid, PageQueryHelperEntity page) {
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (page == null) {
			page = new PageQueryHelperEntity();
		}
		PageHelper.startPage(page.getPageNumber(), page.getPageSize());
		List<WalletCardTransactionEntity> rows = walletCardTransactionDao.findByWalletUserId(user.getId());
		if (rows == null) {
			rows = new ArrayList<>();
		}
		PageInfo<WalletCardTransactionEntity> basePage = new PageInfo<>(rows);
		List<WalletTransactionItemResp> items = new ArrayList<>(rows.size());
		for (WalletCardTransactionEntity row : rows) {
			items.add(toTransactionItem(row));
		}
		PageInfo<WalletTransactionItemResp> pageInfo = new PageInfo<>(items);
		pageInfo.setTotal(basePage.getTotal());
		pageInfo.setPages(basePage.getPages());
		pageInfo.setPageNum(basePage.getPageNum());
		pageInfo.setPageSize(basePage.getPageSize());
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	/**
	 * KYC 国家列表：透传三方 POST /api/user/kyc/country/list；name 空则返回全部。
	 */
	public ResponseBase listKycCountries(String name) {
		List<KycCountryResp> list;
		try {
			list = thirdService.listKycCountries(name);
		} catch (BaseException e) {
			log.error("wallet kyc country list failed name={}", name, e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet kyc country list error name={}", name, e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (list == null) {
			list = Collections.emptyList();
		}
		log.info("wallet kyc country list size={} name={}", list.size(),
				StringUtils.isEmpty(name) ? "ALL" : name.trim());
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 查询 KYC 状态：拉三方并回写 wallet_account / 最近申请单。
	 */
	public ResponseBase getKycStatus(Integer userType, Integer localUid) {
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		ensureWalletAccount(user);
		KycStatusResp third;
		try {
			third = thirdService.getKycStatus(user.getWalletUid());
		} catch (BaseException e) {
			log.error("wallet kyc status third failed walletUid={}", user.getWalletUid(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet kyc status third error walletUid={}", user.getWalletUid(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletKycStateEnums localState = WalletKycStateEnums.fromApiStatus(third.getStatus());
		String failedReason = third.getFailedReason();
		try {
			syncKycLocal(user.getId(), third.getStatus(), localState, failedReason);
		} catch (Exception e) {
			log.error("wallet kyc status sync failed walletUserId={} status={}",
					user.getId(), third.getStatus(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		WalletKycStatusResp resp = new WalletKycStatusResp();
		resp.setStatus(third.getStatus());
		resp.setFailedReason(failedReason);
		resp.setKycState(localState.getCode());
		resp.setKycStateName(localState.getLabel());
		log.info("wallet kyc status walletUserId={} status={} kycState={}",
				user.getId(), third.getStatus(), localState.getCode());
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 提交 KYC：调三方 apply → 落 wallet_kyc_apply → 账户置为认证中。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase applyKyc(Integer userType, Integer localUid, KycApplyRequest query) {
		if (query == null) {
			return setResultError(I18nUtil.getMessage("wallet.kyc_param_required"));
		}
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		ensureWalletAccount(user);
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		// 已成功不可重复提交
		if (account.getKycState() != null
				&& account.getKycState() == WalletKycStateEnums.SUCCESS_APPROVE.getCode()) {
			return setResultError(I18nUtil.getMessage("wallet.kyc_already_success"));
		}
		// 审核中不可重复提交
		if (account.getKycState() != null
				&& account.getKycState() == WalletKycStateEnums.PROCESS_APPROVE.getCode()) {
			return setResultError(I18nUtil.getMessage("wallet.kyc_processing"));
		}

		try {
			thirdService.applyKyc(user.getWalletUid(), query);
		} catch (BaseException e) {
			log.error("wallet kyc apply third failed walletUid={} nationCode={}",
					user.getWalletUid(), query.getNationCode(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet kyc apply third error walletUid={}", user.getWalletUid(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}

		Date now = new Date();
		WalletKycApplyEntity apply = buildKycApply(user, query, now);
		try {
			walletKycApplyDao.insert(apply);
			syncKycLocal(user.getId(), WalletKycApiStatus.WAITING,
					WalletKycStateEnums.PROCESS_APPROVE, null);
		} catch (Exception e) {
			log.error("wallet kyc apply persist failed walletUserId={}", user.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * WebHook：KYC 状态变更回写本地。
	 */
	@Transactional(rollbackFor = Exception.class)
	public void syncKycFromWebhook(Long walletUid, String apiStatus, String failedReason) {
		if (walletUid == null) {
			throw new BaseException("walletUid empty");
		}
		WalletUserEntity user = walletUserDao.findByWalletUid(walletUid);
		if (user == null) {
			log.warn("wallet webhook kyc user not found walletUid={}", walletUid);
			return;
		}
		WalletKycStateEnums localState = WalletKycStateEnums.fromApiStatus(apiStatus);
		try {
			syncKycLocal(user.getId(), apiStatus, localState, failedReason);
		} catch (Exception e) {
			log.error("wallet webhook kyc sync failed walletUserId={} status={}", user.getId(), apiStatus, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** WebHook：卡激活后标记账户已激活 */
	public void markAccountActivated(Long walletUserId) {
		if (walletUserId == null) {
			return;
		}
		try {
			walletAccountDao.markActivated(walletUserId);
		} catch (Exception e) {
			log.error("wallet webhook mark activated failed walletUserId={}", walletUserId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** 回写账户 KYC，并刷新最近一条申请单状态 */
	private void syncKycLocal(Long walletUserId, String apiStatus, WalletKycStateEnums localState,
			String failedReason) {
		walletAccountDao.updateKycStatus(walletUserId, localState.getCode(), localState.getLabel(),
				apiStatus, failedReason);
		WalletKycApplyEntity latest = walletKycApplyDao.findLatestByWalletUserId(walletUserId);
		if (latest != null) {
			walletKycApplyDao.updateStatus(latest.getId(), apiStatus, localState.getCode(), failedReason);
		}
	}

	/** 组装 KYC 申请流水（证件号仅存 hash，不明文） */
	private WalletKycApplyEntity buildKycApply(WalletUserEntity user, KycApplyRequest query, Date now) {
		WalletKycApplyEntity apply = new WalletKycApplyEntity();
		apply.setWalletUserId(user.getId());
		apply.setWalletUid(user.getWalletUid());
		apply.setFirstName(query.getFirstName().trim());
		apply.setLastName(query.getLastName().trim());
		apply.setIdNoHash(DigestUtil.sha256Hex(query.getIdNo().trim()));
		apply.setEmail(query.getEmail().trim());
		apply.setNationCode(query.getNationCode().trim());
		apply.setCertType(query.getCertType());
		apply.setIdUrl(query.getIdUrl().trim());
		apply.setIdBackUrl(StringUtils.isEmpty(query.getIdBackUrl()) ? null : query.getIdBackUrl().trim());
		apply.setBirthday(query.getBirthday().trim());
		apply.setCountryCode(query.getCountryCode().trim());
		apply.setAreaCode(query.getAreaCode().trim());
		apply.setPhone(query.getPhone().trim());
		apply.setFileType(query.getFileType());
		apply.setFileUrl(query.getFileUrl());
		apply.setFaceUrl(query.getFaceUrl());
		apply.setReferenceId(query.getReferenceId());
		apply.setReferenceType(query.getReferenceType());
		apply.setSelfieUrl(query.getSelfieUrl());
		apply.setApplyStatus(WalletKycApiStatus.WAITING);
		apply.setKycState(WalletKycStateEnums.PROCESS_APPROVE.getCode());
		apply.setSetTime(now);
		apply.setGmtModified(now);
		return apply;
	}

	/**
	 * 首次绑定支付密码。
	 */
	public ResponseBase bindPayPassword(Integer userType, Integer localUid, WalletBindPayPwdRequest query) {
		if (query == null || StringUtils.isEmpty(query.getPayPassword())
				|| StringUtils.isEmpty(query.getConfirmPayPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (!query.getPayPassword().equals(query.getConfirmPayPassword())) {
			return setResultError(I18nUtil.getMessage("creator.password_not_match"));
		}
		if (!PAY_PASSWORD_PATTERN.matcher(query.getPayPassword()).matches()) {
			return setResultError(I18nUtil.getMessage("wallet.pay_password_invalid"));
		}
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (!StringUtils.isEmpty(account.getPayPassword())) {
			return setResultError(I18nUtil.getMessage("wallet.pay_password_already_set"));
		}
		String hashed = PasswordHashUtils.encode(query.getPayPassword());
		int rows;
		try {
			rows = walletAccountDao.bindPayPassword(account.getId(), hashed);
		} catch (Exception e) {
			log.error("wallet bind pay password failed walletUserId={} localUid={}", user.getId(), localUid, e);
			throw new BaseException("操作失败", e);
		}
		if (rows <= 0) {
			return setResultError(I18nUtil.getMessage("wallet.pay_password_already_set"));
		}
		log.info("wallet pay password bound walletUserId={} localUid={} userType={}",
				user.getId(), localUid, userType);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** 若缺 wallet_account 则补建（幂等保护） */
	private void ensureWalletAccount(WalletUserEntity user) {
		if (walletAccountDao.findByWalletUserId(user.getId()) != null) {
			return;
		}
		insertWalletAccount(user.getId(), user.getWalletUid(), new Date());
	}

	private void insertWalletAccount(Long walletUserId, Long walletUid, Date now) {
		WalletAccountEntity account = new WalletAccountEntity();
		account.setWalletUserId(walletUserId);
		account.setWalletUid(walletUid);
		account.setKycState(WalletKycStateEnums.WAIT_APPROVE.getCode());
		account.setKycStateName(WalletKycStateEnums.WAIT_APPROVE.getLabel());
		account.setKycApiStatus(WalletKycApiStatus.UNCOMMITTED);
		account.setActivationState(ACTIVATION_NOT_YET);
		// 账户余额初始为 0，后续由三方同步刷新
		account.setAvailableBalance(BigDecimal.ZERO);
		account.setFreezeBalance(BigDecimal.ZERO);
		account.setOpenFreezeBalance(BigDecimal.ZERO);
		account.setCurrency(WalletConstants.DEFAULT_CURRENCY);
		account.setSetTime(now);
		account.setGmtModified(now);
		try {
			walletAccountDao.insert(account);
		} catch (DuplicateKeyException e) {
			log.warn("wallet account already exists walletUserId={} walletUid={}", walletUserId, walletUid);
		}
	}

	private static String buildTel(String mobilePrefix, String mobileNumber) {
		if (StringUtils.isEmpty(mobileNumber)) {
			return null;
		}
		String number = mobileNumber.trim();
		if (StringUtils.isEmpty(mobilePrefix)) {
			return number;
		}
		String prefix = mobilePrefix.trim();
		if (prefix.startsWith("+")) {
			return prefix + number;
		}
		return "+" + prefix + number;
	}

	private static String trimToNull(String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static BigDecimal nvlBalance(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	/** 激活成功后回写本地卡记录 */
	private void syncCardAfterActive(WalletUserEntity user, ThirdBankcardActiveResp third, String requestCardNo) {
		if (third == null || third.getUserBankcardId() == null) {
			return;
		}
		WalletBankcardEntity card = findOwnedCard(user, third.getUserBankcardId());
		if (card == null) {
			log.warn("wallet card active local missing walletUserId={} userBankcardId={}",
					user.getId(), third.getUserBankcardId());
			return;
		}
		String cardNo = StringUtils.isEmpty(third.getCardNo()) ? requestCardNo : third.getCardNo();
		if (!StringUtils.isEmpty(cardNo)) {
			walletBankcardDao.updateCardNo(card.getId(), cardNo);
		}
		walletBankcardDao.updateCardStatus(card.getId(),
				WalletCardStatusEnums.ACTIVE.getCode(), WalletCardStatusEnums.ACTIVE.getLabel());
		walletAccountDao.markActivated(user.getId());
	}

	/** 充值提交后落本地流水 */
	private void insertRechargeTransaction(WalletUserEntity user, WalletBankcardEntity card,
			BankcardRechargeRequest query) {
		Date now = new Date();
		BigDecimal amount = new BigDecimal(query.getAmount());
		String currency = StringUtils.isEmpty(card.getCurrency())
				? WalletConstants.DEFAULT_CURRENCY : card.getCurrency();
		WalletCardTransactionEntity txn = new WalletCardTransactionEntity();
		txn.setWalletUserId(user.getId());
		txn.setWalletUid(user.getWalletUid());
		txn.setWalletBankcardId(card.getId());
		txn.setUserBankcardId(card.getUserBankcardId());
		txn.setCardProductId(card.getCardProductId());
		txn.setCardUuid(card.getCardUuid());
		txn.setCardNo(card.getCardNo());
		txn.setRequestOrderId(query.getRequestOrderId());
		txn.setBizType(WalletConstants.BIZ_RECHARGE);
		txn.setTransType(WalletConstants.TRANS_TOPUP);
		txn.setOrderState(WalletConstants.ORDER_STATE_PENDING);
		txn.setOrderStateName(WalletConstants.ORDER_STATE_PENDING_NAME);
		txn.setLocalCurrency(currency);
		txn.setLocalCurrencyAmt(amount);
		txn.setTransCurrency(currency);
		txn.setTransCurrencyAmt(amount);
		txn.setTitle("卡充值");
		txn.setSetTime(now);
		txn.setGmtModified(now);
		try {
			walletCardTransactionDao.insert(txn);
		} catch (DuplicateKeyException e) {
			log.warn("wallet recharge txn duplicate requestOrderId={}", query.getRequestOrderId(), e);
		} catch (Exception e) {
			log.error("wallet recharge txn insert failed requestOrderId={}", query.getRequestOrderId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private WalletUserEntity findWalletUser(Integer userType, Integer localUid) {
		return walletUserDao.findByLocal(userType, localUid);
	}

	/** 校验卡归属当前钱包用户 */
	private WalletBankcardEntity findOwnedCard(WalletUserEntity user, Long userBankcardId) {
		if (user == null || userBankcardId == null) {
			return null;
		}
		WalletBankcardEntity card = walletBankcardDao.findByUserBankcardId(userBankcardId);
		if (card == null || !user.getId().equals(card.getWalletUserId())) {
			return null;
		}
		return card;
	}

	private static BigDecimal parseBalance(String balance) {
		if (StringUtils.isEmpty(balance)) {
			return null;
		}
		try {
			return new BigDecimal(balance.trim());
		} catch (Exception e) {
			return null;
		}
	}

	private static WalletCardItemResp toCardItem(WalletBankcardEntity row, String cardImg) {
		WalletCardItemResp item = new WalletCardItemResp();
		item.setId(row.getId());
		item.setUserBankcardId(row.getUserBankcardId());
		item.setDisplayName(buildCardDisplayName(row));
		item.setCardNo(row.getCardNo());
		item.setCardBrand(row.getCardBrand());
		item.setCardProductId(row.getCardProductId());
		item.setBankcardNature(row.getBankcardNature());
		item.setCurrency(StringUtils.isEmpty(row.getCurrency())
				? WalletConstants.DEFAULT_CURRENCY : row.getCurrency());
		item.setCardStatus(row.getCardStatus());
		item.setCardStatusName(row.getCardStatusName());
		item.setBalance(nvlBalance(row.getBalance()));
		item.setIsDefault(row.getIsDefault() == null ? WalletConstants.CARD_DEFAULT_NO : row.getIsDefault());
		item.setPinSet(row.getPinSet());
		item.setTagName(row.getTagName());
		item.setCardImg(cardImg);
		item.setSetTime(row.getSetTime());
		return item;
	}

	/** 按产品 id 批量加载本地 card_img 缓存 */
	private Map<Integer, String> loadCardImgMap(Set<Integer> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<Integer> ids = new ArrayList<>(productIds);
		List<WalletCardProductEntity> rows = walletCardProductDao.findCardImgByIds(ids);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, String> map = new HashMap<>(rows.size());
		for (WalletCardProductEntity row : rows) {
			if (row.getId() != null && !StringUtils.isEmpty(row.getCardImg())) {
				map.put(row.getId(), row.getCardImg());
			}
		}
		return map;
	}

	private static Set<Integer> collectCardProductIds(List<WalletBankcardEntity> rows) {
		Set<Integer> ids = new HashSet<>();
		for (WalletBankcardEntity row : rows) {
			if (row.getCardProductId() != null) {
				ids.add(row.getCardProductId());
			}
		}
		return ids;
	}

	/** 优先自定义标签，否则 品牌-尾号 */
	private static String buildCardDisplayName(WalletBankcardEntity row) {
		if (!StringUtils.isEmpty(row.getTagName())) {
			return row.getTagName();
		}
		String brand = StringUtils.isEmpty(row.getCardBrand()) ? "Card" : row.getCardBrand();
		String tail = last4(row.getCardNo());
		if (StringUtils.isEmpty(tail)) {
			return brand;
		}
		return brand + "-" + tail;
	}

	private static String last4(String cardNo) {
		if (StringUtils.isEmpty(cardNo)) {
			return "";
		}
		String digits = cardNo.replaceAll("\\D", "");
		if (digits.length() >= 4) {
			return digits.substring(digits.length() - 4);
		}
		String trimmed = cardNo.trim();
		if (trimmed.length() >= 4) {
			return trimmed.substring(trimmed.length() - 4);
		}
		return trimmed;
	}

	private static WalletTransactionItemResp toTransactionItem(WalletCardTransactionEntity row) {
		WalletTransactionItemResp item = new WalletTransactionItemResp();
		item.setId(row.getId());
		item.setWalletBankcardId(row.getWalletBankcardId());
		item.setTitle(StringUtils.isEmpty(row.getTitle()) ? row.getBizType() : row.getTitle());
		item.setBizType(row.getBizType());
		item.setTransType(row.getTransType());
		item.setOrderState(row.getOrderState());
		item.setOrderStateName(row.getOrderStateName());
		item.setAmount(toDisplayAmount(row));
		item.setCurrency(resolveTxnCurrency(row));
		item.setCardNo(row.getCardNo());
		item.setSetTime(row.getSetTime());
		return item;
	}

	/** 展示金额：优先交易币金额，支出类取负 */
	private static BigDecimal toDisplayAmount(WalletCardTransactionEntity row) {
		BigDecimal raw = row.getTransCurrencyAmt() != null ? row.getTransCurrencyAmt() : row.getLocalCurrencyAmt();
		if (raw == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal abs = raw.abs();
		if (isExpenseBiz(row.getBizType())) {
			return abs.negate();
		}
		return abs;
	}

	private static boolean isExpenseBiz(String bizType) {
		if (StringUtils.isEmpty(bizType)) {
			return false;
		}
		return WalletConstants.BIZ_WITHDRAW.equalsIgnoreCase(bizType)
				|| WalletConstants.BIZ_AUTH.equalsIgnoreCase(bizType)
				|| WalletConstants.BIZ_CLOSE.equalsIgnoreCase(bizType)
				|| WalletConstants.BIZ_APPLY.equalsIgnoreCase(bizType);
	}

	private static String resolveTxnCurrency(WalletCardTransactionEntity row) {
		if (!StringUtils.isEmpty(row.getTransCurrency())) {
			return row.getTransCurrency();
		}
		if (!StringUtils.isEmpty(row.getLocalCurrency())) {
			return row.getLocalCurrency();
		}
		return WalletConstants.DEFAULT_CURRENCY;
	}
}
