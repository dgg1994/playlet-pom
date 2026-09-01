package com.playlet.internal.service.third;

import cn.hutool.crypto.digest.DigestUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.request.*;
import com.playlet.internal.api.response.*;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.constants.WalletKycApiStatus;
import com.playlet.internal.dao.wallet.*;
import com.playlet.internal.entity.wallet.*;
import com.playlet.internal.enums.WalletCardApplyStateEnums;
import com.playlet.internal.enums.WalletCardStatusEnums;
import com.playlet.internal.enums.WalletKycStateEnums;
import com.playlet.internal.enums.WalletLogisticsStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.support.WalletBankcardSyncSupport;
import com.playlet.internal.service.support.WalletCardProductService;
import com.playlet.internal.service.support.WalletCardholderService;
import com.playlet.internal.service.support.WalletOpenCardSettlementService;
import com.playlet.internal.service.support.WalletPhysicalCardFulfillService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.KycFieldNormalizeUtil;
import com.playlet.internal.utils.PasswordHashUtils;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.WalletRequestOrderIdSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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
	private WalletCardApplyManDao walletCardApplyManDao;
	@Autowired
	private WalletCardApplySendDao walletCardApplySendDao;
	@Autowired
	private WalletCardApplyKycDao walletCardApplyKycDao;
	@Autowired
	private WalletUserHolderDao walletUserHolderDao;
	@Autowired
	private WalletCardTransactionDao walletCardTransactionDao;
	@Autowired
	private WalletCardProductDao walletCardProductDao;
	@Autowired
	private WalletCardProductService walletCardProductService;
	@Autowired
	private WalletKycApplyDao walletKycApplyDao;
	@Autowired
	private WalletKycFileDao walletKycFileDao;
	@Autowired
	private WalletCardholderService walletCardholderService;
	@Autowired
	private WalletPhysicalCardFulfillService walletPhysicalCardFulfillService;
	@Autowired
	private WalletOpenCardSettlementService walletOpenCardSettlementService;
	@Autowired
	private WalletBankcardSyncSupport walletBankcardSyncSupport;

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
		// 本地 card_no 为空时从三方补全（Webhook 未带卡号的兜底）
		walletBankcardSyncSupport.syncMissingCardNos(user.getWalletUid(), rows);
		List<WalletCardItemResp> items = new ArrayList<>(rows.size());
		Map<Integer, WalletCardProductEntity> productMap = loadCardProductMap(collectCardProductIds(rows));
		for (WalletBankcardEntity row : rows) {
			WalletCardProductEntity product = productMap.get(row.getCardProductId());
			items.add(toCardItem(row, product));
		}
		return setResultSuccess(items, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 持有银行卡详情（对齐 onetoken GET /appUserCard/findUserCardInfo）。
	 */
	public ResponseBase findUserCardInfo(Integer userType, Integer localUid, Long id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		WalletBankcardEntity card = findOwnedCardById(user, id);
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		walletBankcardSyncSupport.syncCardNo(card);
		WalletCardProductEntity product = card.getCardProductId() == null
				? null : walletCardProductDao.findById(card.getCardProductId());
		WalletCardApplyEntity apply = card.getCardApplyId() == null
				? null : walletCardApplyDao.selectById(card.getCardApplyId());
		WalletUserHolderEntity holder = null;
		if (apply != null && apply.getHolderId() != null) {
			holder = walletUserHolderDao.findOwned(apply.getHolderId(), user.getId());
		}
		WalletCardDetailResp detail = toCardDetail(card, product, apply, holder);
		fillCardFeeFields(detail, product);
		if (apply != null) {
			List<WalletLogisticsEventResp> events = walletPhysicalCardFulfillService
					.refreshLogisticsForCardDetail(card, apply, user.getId());
			if (!events.isEmpty()) {
				detail.setLogisticsInfo(events);
			}
			fillShippingFields(detail, card, apply);
		}
		log.info("wallet findUserCardInfo walletUserId={} walletBankcardId={} userBankcardId={}",
				user.getId(), card.getId(), card.getUserBankcardId());
		return setResultSuccess(detail, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 卡产品列表（对齐 onetoken POST /card/findList）：可按 VIRTUAL / PHYSICAL 筛选。
	 */
	public ResponseBase findCardProductList(WalletCardProductListRequest query) {
		List<WalletCardProductItemResp> items = walletCardProductService.findList(query);
		log.info("wallet card findList size={} bankCardNature={}",
				items.size(), query == null ? null : query.getBankCardNature());
		return setResultSuccess(items, I18nUtil.getMessage("base_success"));
	}

	/** C 端卡产品详情：按 productId 查询，仅 enable=1 */
	public ResponseBase getCardProductDetail(Integer productId) {
		try {
			WalletCardProductItemResp detail = walletCardProductService.findEnabledItemByProductId(productId);
			log.info("wallet card product detail productId={}", productId);
			return setResultSuccess(detail, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("wallet card product detail error productId={}", productId, e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
	}

	/**
	 * 申请开卡（对齐 onetoken openCardApply）：支付密码 → 校验产品/持卡人/KYC 资料 → 计算费用并冻结余额
	 * → 落申请单 → KYC 已通过时虚拟卡自动三方发卡并标记激活成功。
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
		ensureWalletAccount(user);
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		// 支付密码校验（对齐 onetoken checkPayPasswrod）
		ResponseBase payPwdResult = checkPayPassword(account, query.getPayPassword());
		if (!Constants.HTTP_RES_CODE_200.equals(payPwdResult.getCode())) {
			return payPwdResult;
		}
		WalletCardProductEntity product = walletCardProductDao.findById(query.getProductId());
		if (product == null) {
			return setResultError(I18nUtil.getMessage("wallet.product_not_found"));
		}
		if (!Integer.valueOf(1).equals(product.getEnable())) {
			return setResultError(I18nUtil.getMessage("wallet.product_disabled"));
		}
		// 须已有 KYC 证件资料，但不要求 KYC 已通过
		if (!hasApplyKycMaterial(user, query)) {
			return setResultError(I18nUtil.getMessage("user_kyc_null"));
		}
		boolean physicalCard = isPhysicalCard(product.getBankcardNature());
		// 实体卡须传邮寄地址或三方邮寄地址 id（对齐 onetoken mailingAddress / deliveryAddressId）
		if (physicalCard && !hasPhysicalMailingInfo(query)) {
			return setResultError(I18nUtil.getMessage("mailingAddress_null"));
		}
		String requestOrderId = WalletRequestOrderIdSupport.resolve(query.getRequestOrderId(),
				WalletConstants.REQUEST_ORDER_PREFIX_CARD_APPLY, localUid.longValue());
		// 幂等：同 requestOrderId 直接返回已有申请结果
		WalletCardApplyEntity existedApply = walletCardApplyDao.findByRequestOrderId(requestOrderId);
		if (existedApply != null) {
			WalletBankcardEntity existedCard = walletBankcardDao.findByCardApplyId(existedApply.getId());
			boolean idempotentAutoIssued = !physicalCard && existedCard != null;
			log.info("wallet apply card idempotent walletUserId={} requestOrderId={}",
					user.getId(), requestOrderId);
			String thirdOrderNo = existedCard == null ? null : existedCard.getApplyOrderNo();
			return setResultSuccess(buildApplyCardResp(existedApply, existedCard, product, thirdOrderNo,
					idempotentAutoIssued), I18nUtil.getMessage("base_success"));
		}
		int topupType = query.getTopupType() == null
				? WalletConstants.TOPUP_TYPE_WALLET : query.getTopupType();
		OpenCardFeeBundle fees = resolveOpenCardFees(product, query, physicalCard);
		// 钱包充值方式：校验可用余额是否覆盖开卡总费用
		if (WalletConstants.TOPUP_TYPE_WALLET == topupType && fees.openCardTotal.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal available = account.getAvailableBalance() == null
					? BigDecimal.ZERO : account.getAvailableBalance();
			if (available.compareTo(fees.openCardTotal) < 0) {
				return setResult(Constants.HTTP_RES_CODE_601,
						I18nUtil.getMessage("wallet.balance_not_enough"), null);
			}
		}
		WalletUserHolderEntity holder;
		try {
			holder = resolveOrCreateHolder(user, query);
		} catch (BaseException e) {
			return setResultError(e.getMessage());
		}
		Date now = new Date();
		boolean kycApproved = isKycApproved(account);
		// 落申请单 + 持卡人/KYC/邮寄地址快照（对齐 onetoken addApply）
		WalletCardApplyEntity apply = buildApplyEntity(user, account, product, query, holder.getId(),
				requestOrderId, topupType, physicalCard, false, fees, now);
		try {
			walletCardApplyDao.insert(apply);
			persistApplySnapshots(user, apply.getId(), holder, query, now);
		} catch (DuplicateKeyException e) {
			log.warn("wallet apply card duplicate requestOrderId={}", requestOrderId, e);
			WalletCardApplyEntity again = walletCardApplyDao.findByRequestOrderId(requestOrderId);
			if (again != null) {
				WalletBankcardEntity existedCard = walletBankcardDao.findByCardApplyId(again.getId());
				String thirdOrderNo = existedCard == null ? null : existedCard.getApplyOrderNo();
				return setResultSuccess(buildApplyCardResp(again, existedCard, product, thirdOrderNo,
						!physicalCard && existedCard != null), I18nUtil.getMessage("base_success"));
			}
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		} catch (Exception e) {
			log.error("wallet apply card insert apply failed requestOrderId={}", requestOrderId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		if (walletCardApplyKycDao.findByApplyId(apply.getId()) == null) {
			throw new BaseException(I18nUtil.getMessage("user_kyc_null"));
		}
		// 冻结开卡费用：月费 + 开卡费 + 预存费 + 邮费（对齐 onetoken 冻结 walletBalance）
		if (fees.openCardTotal.compareTo(BigDecimal.ZERO) > 0) {
			freezeOpenCardBalance(account, fees.openCardTotal, apply.getId());
		}
		// 虚拟卡 + KYC 已通过：自动调三方开卡；否则待激活待审 KYC
		ThirdBankcardApplyResp third = null;
		boolean autoIssued = false;
		if (!physicalCard && kycApproved) {
			third = issueVirtualCardThird(user, apply, product, query.getDeliveryAddressId());
			autoIssued = third != null && third.getUserBankcardId() != null;
		}

		WalletBankcardEntity card = persistIssuedBankcard(user, apply, product, third, now);
		if (card != null) {
			walletAccountDao.markActivated(user.getId());
			if (autoIssued) {
				finalizeVirtualCardAfterAutoIssue(user, apply, card, third);
			}
		}
		String thirdOrderNo = third == null ? null : third.getOrderNo();
		log.info("wallet apply card success walletUserId={} productId={} applyId={} holderId={} openCardTotal={} userBankcardId={} autoIssued={} kycApproved={}",
				user.getId(), query.getProductId(), apply.getId(), holder.getId(), fees.openCardTotal,
				third == null ? null : third.getUserBankcardId(), autoIssued, kycApproved);
		return setResultSuccess(buildApplyCardResp(apply, card, product, thirdOrderNo, autoIssued),
				I18nUtil.getMessage("base_success"));
	}

	/**
	 * C 端查询实体卡物流（校验申请单归属）。
	 */
	public ResponseBase findLogistics(Integer userType, Integer localUid, String logisticsNum, Long applyId) {
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		return walletPhysicalCardFulfillService.findLogistics(logisticsNum, applyId, user.getId());
	}

	/** 解析已有持卡人或新建持卡人 */
	private WalletUserHolderEntity resolveOrCreateHolder(WalletUserEntity user, WalletApplyCardRequest query) {
		if (query.getHolderId() != null) {
			WalletUserHolderEntity existed = walletUserHolderDao.findOwned(query.getHolderId(), user.getId());
			if (existed == null) {
				throw new BaseException(I18nUtil.getMessage("holder_null"));
			}
			return existed;
		}
		if (query.getHolderData() == null) {
			throw new BaseException(I18nUtil.getMessage("wallet.apply_holder_required"));
		}
		return walletCardholderService.createForApply(user, query.getHolderData());
	}

	/** 落申请关联快照：持卡人 / 邮寄地址 / KYC */
	private void persistApplySnapshots(WalletUserEntity user, Long applyId, WalletUserHolderEntity holder,
			WalletApplyCardRequest query, Date now) {
		insertApplyMan(user, applyId, holder, now);
		if (query.getMailingAddress() != null && isMailingAddressValid(query.getMailingAddress())) {
			insertApplySend(user, applyId, query.getMailingAddress(), now);
		} else {
			// 仅 deliveryAddressId：落最小邮寄快照供后续发货/绑卡校验
			Integer addressId = resolveDeliveryAddressId(query);
			if (addressId != null) {
				insertApplySendByAddressId(user, applyId, addressId, now);
			}
		}
		WalletCardApplyKycEntity kycSnapshot = resolveApplyKyc(user, applyId, query.getKycData(), now);
		if (kycSnapshot != null) {
			try {
				walletCardApplyKycDao.insert(kycSnapshot);
			} catch (Exception e) {
				log.error("wallet apply kyc insert failed applyId={}", applyId, e);
				throw new BaseException(I18nUtil.getMessage("base_error"), e);
			}
		}
	}

	private void insertApplyMan(WalletUserEntity user, Long applyId, WalletUserHolderEntity holder, Date now) {
		WalletCardApplyManEntity man = new WalletCardApplyManEntity();
		man.setApplyId(applyId);
		man.setWalletUserId(user.getId());
		man.setWalletUid(user.getWalletUid());
		man.setUserName(holder.getUserName());
		man.setUserSurname(holder.getUserSurname());
		man.setUserTelDialCode(holder.getUserTelDialCode());
		man.setUserTelCode(holder.getUserTelCode());
		man.setUserTel(holder.getUserTel());
		man.setUserEmail(holder.getUserEmail());
		man.setUserNumber(holder.getUserNumber());
		man.setUserSex(holder.getUserSex());
		man.setUserAddress(holder.getUserAddress());
		man.setUserBirthday(holder.getUserBirthday());
		man.setSetTime(now);
		man.setGmtModified(now);
		try {
			walletCardApplyManDao.insert(man);
		} catch (Exception e) {
			log.error("wallet apply man insert failed applyId={}", applyId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private void insertApplySend(WalletUserEntity user, Long applyId,
			WalletCardMailingAddressRequest mailing, Date now) {
		WalletCardApplySendEntity send = new WalletCardApplySendEntity();
		send.setApplyId(applyId);
		send.setWalletUserId(user.getId());
		send.setWalletUid(user.getWalletUid());
		if (mailing.getAddressId() != null) {
			send.setAddressId(mailing.getAddressId());
		}
		send.setNation(mailing.getNation().trim());
		send.setProvince(mailing.getProvince());
		send.setCity(mailing.getCity());
		send.setAddressInfo(mailing.getAddressInfo().trim());
		send.setCollectMan(mailing.getCollectMan().trim());
		send.setCollectTel(mailing.getCollectTel().trim());
		send.setPostCode(mailing.getPostCode());
		send.setSetTime(now);
		send.setGmtModified(now);
		try {
			walletCardApplySendDao.insert(send);
		} catch (Exception e) {
			log.error("wallet apply send insert failed applyId={}", applyId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** 仅三方 addressId 时写入邮寄快照（占位字段满足非空约束） */
	private void insertApplySendByAddressId(WalletUserEntity user, Long applyId, Integer addressId, Date now) {
		WalletCardApplySendEntity send = new WalletCardApplySendEntity();
		send.setApplyId(applyId);
		send.setWalletUserId(user.getId());
		send.setWalletUid(user.getWalletUid());
		send.setAddressId(addressId);
		String placeholder = WalletOpenCardSettlementService.addressPlaceholder();
		send.setNation(placeholder);
		send.setAddressInfo(placeholder);
		send.setCollectMan(placeholder);
		send.setCollectTel(placeholder);
		send.setSetTime(now);
		send.setGmtModified(now);
		try {
			walletCardApplySendDao.insert(send);
		} catch (Exception e) {
			log.error("wallet apply send by addressId insert failed applyId={} addressId={}", applyId, addressId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/**
	 * 解析开卡 KYC 快照：优先入参；否则从账户 KYC / 历史开卡 KYC / 证件文件回填。
	 */
	private WalletCardApplyKycEntity resolveApplyKyc(WalletUserEntity user, Long applyId,
			WalletCardApplyKycRequest kycData, Date now) {
		if (kycData != null && hasKycContent(kycData)) {
			WalletCardApplyKycEntity row = new WalletCardApplyKycEntity();
			row.setApplyId(applyId);
			row.setWalletUserId(user.getId());
			row.setWalletUid(user.getWalletUid());
			row.setPaperworkType(kycData.getPaperworkType());
			row.setPaperworkNum(kycData.getPaperworkNum());
			row.setExpirationTime(kycData.getExpirationTime());
			row.setFrontPhotoId(kycData.getFrontPhotoId());
			row.setFrontPhotoUrl(kycData.getFrontPhotoUrl());
			row.setBackPhotoId(kycData.getBackPhotoId());
			row.setBackPhotoUrl(kycData.getBackPhotoUrl());
			row.setHandheldPhotoId(kycData.getHandheldPhotoId());
			row.setHandheldPhotoUrl(kycData.getHandheldPhotoUrl());
			row.setSetTime(now);
			row.setGmtModified(now);
			return row;
		}
		WalletCardApplyKycEntity history = walletCardApplyKycDao.findLatestByWalletUserId(user.getId());
		if (history != null) {
			history.setId(null);
			history.setApplyId(applyId);
			history.setSetTime(now);
			history.setGmtModified(now);
			return history;
		}
		WalletKycApplyEntity latestKyc = walletKycApplyDao.findLatestByWalletUserId(user.getId());
		if (latestKyc == null) {
			return null;
		}
		WalletCardApplyKycEntity row = new WalletCardApplyKycEntity();
		row.setApplyId(applyId);
		row.setWalletUserId(user.getId());
		row.setWalletUid(user.getWalletUid());
		row.setPaperworkType(mapPaperworkType(latestKyc.getCertType()));
		row.setFrontPhotoUrl(latestKyc.getIdUrl());
		row.setBackPhotoUrl(latestKyc.getIdBackUrl());
		row.setHandheldPhotoUrl(latestKyc.getSelfieUrl());
		WalletKycFileEntity front = walletKycFileDao.findLatestByType(user.getId(), WalletConstants.KYC_DOC_FRONT);
		WalletKycFileEntity back = walletKycFileDao.findLatestByType(user.getId(), WalletConstants.KYC_DOC_BACK);
		WalletKycFileEntity handheld = walletKycFileDao.findLatestByType(user.getId(),
				WalletConstants.KYC_DOC_HANDHELD);
		if (front != null) {
			row.setFrontPhotoId(front.getDocumentFileId());
			if (StringUtils.isEmpty(row.getFrontPhotoUrl())) {
				row.setFrontPhotoUrl(front.getDocumentFileUrl());
			}
		}
		if (back != null) {
			row.setBackPhotoId(back.getDocumentFileId());
			if (StringUtils.isEmpty(row.getBackPhotoUrl())) {
				row.setBackPhotoUrl(back.getDocumentFileUrl());
			}
		}
		if (handheld != null) {
			row.setHandheldPhotoId(handheld.getDocumentFileId());
			if (StringUtils.isEmpty(row.getHandheldPhotoUrl())) {
				row.setHandheldPhotoUrl(handheld.getDocumentFileUrl());
			}
		}
		row.setSetTime(now);
		row.setGmtModified(now);
		return row;
	}

	private static boolean hasKycContent(WalletCardApplyKycRequest kycData) {
		if (kycData == null) {
			return false;
		}
		return !StringUtils.isEmpty(kycData.getPaperworkType())
				|| !StringUtils.isEmpty(kycData.getPaperworkNum())
				|| !StringUtils.isEmpty(kycData.getFrontPhotoUrl())
				|| !StringUtils.isEmpty(kycData.getFrontPhotoId())
				|| !StringUtils.isEmpty(kycData.getBackPhotoUrl())
				|| !StringUtils.isEmpty(kycData.getHandheldPhotoUrl());
	}

	private static boolean isMailingAddressValid(WalletCardMailingAddressRequest mailing) {
		return mailing != null
				&& !StringUtils.isEmpty(mailing.getNation())
				&& !StringUtils.isEmpty(mailing.getAddressInfo())
				&& !StringUtils.isEmpty(mailing.getCollectMan())
				&& !StringUtils.isEmpty(mailing.getCollectTel());
	}

	/** 实体卡是否已有邮寄信息：本地地址明细或三方 deliveryAddressId */
	private static boolean hasPhysicalMailingInfo(WalletApplyCardRequest query) {
		if (query == null) {
			return false;
		}
		if (resolveDeliveryAddressId(query) != null) {
			return true;
		}
		return isMailingAddressValid(query.getMailingAddress());
	}

	/** 解析三方邮寄地址 id：优先顶层 deliveryAddressId，其次 mailingAddress.addressId */
	private static Integer resolveDeliveryAddressId(WalletApplyCardRequest query) {
		if (query == null) {
			return null;
		}
		if (query.getDeliveryAddressId() != null) {
			return query.getDeliveryAddressId();
		}
		if (query.getMailingAddress() != null && query.getMailingAddress().getAddressId() != null) {
			return query.getMailingAddress().getAddressId();
		}
		return null;
	}

	private static String mapPaperworkType(Integer certType) {
		if (certType == null) {
			return null;
		}
		if (certType == WalletConstants.KYC_CERT_PASSPORT) {
			return WalletConstants.PAPERWORK_PASSPORT;
		}
		return WalletConstants.PAPERWORK_NATIONAL_ID;
	}

	private static WalletCardApplyEntity buildApplyEntity(WalletUserEntity user, WalletAccountEntity account,
			WalletCardProductEntity product, WalletApplyCardRequest query, Long holderId, String requestOrderId,
			int topupType, boolean physicalCard, boolean autoIssued, OpenCardFeeBundle fees, Date now) {
		WalletCardApplyEntity apply = new WalletCardApplyEntity();
		apply.setWalletUserId(user.getId());
		apply.setWalletUid(user.getWalletUid());
		apply.setHolderId(holderId);
		apply.setCardProductId(query.getProductId());
		apply.setCardUuid(product.getProductUuid());
		apply.setCardType(product.getBankcardNature());
		apply.setTopupType(topupType);
		apply.setOpenCardCost(fees.openCardCost);
		apply.setPreSaveCost(fees.preSaveCost);
		apply.setLogisticsMonery(fees.logisticsMonery);
		apply.setOpenCardTotal(fees.openCardTotal);
		if (autoIssued) {
			apply.setApplyState(WalletCardApplyStateEnums.SUCCESS_ACTIVATION.getCode());
			apply.setApplyStateName(WalletCardApplyStateEnums.SUCCESS_ACTIVATION.getLabel());
		} else {
			apply.setApplyState(WalletCardApplyStateEnums.WAIT_ACTIVATION.getCode());
			apply.setApplyStateName(WalletCardApplyStateEnums.WAIT_ACTIVATION.getLabel());
		}
		apply.setKycState(account.getKycState());
		apply.setKycStateName(account.getKycStateName());
		apply.setKycAuditResult(account.getKycAuditResult());
		if (apply.getKycState() == null) {
			apply.setKycState(WalletKycStateEnums.WAIT_APPROVE.getCode());
			apply.setKycStateName(WalletKycStateEnums.WAIT_APPROVE.getLabel());
		}
		apply.setRequestOrderId(requestOrderId);
		// KYC 已通过时实体卡进入待发货
		if (physicalCard
				&& account.getKycState() != null
				&& account.getKycState() == WalletKycStateEnums.SUCCESS_APPROVE.getCode()) {
			apply.setShippingState(WalletLogisticsStateEnums.WAIT_SUCCESS.getCode());
			apply.setShippingStateName(WalletLogisticsStateEnums.WAIT_SUCCESS.getLabel());
		}
		apply.setSetTime(now);
		apply.setGmtModified(now);
		return apply;
	}

	private static WalletApplyCardResp buildApplyCardResp(WalletCardApplyEntity apply, WalletBankcardEntity card,
			WalletCardProductEntity product, String thirdOrderNo, boolean autoIssued) {
		WalletApplyCardResp resp = new WalletApplyCardResp();
		if (apply != null) {
			resp.setApplyId(apply.getId());
			resp.setHolderId(apply.getHolderId());
			resp.setRequestOrderId(apply.getRequestOrderId());
			resp.setProductId(apply.getCardProductId());
			resp.setCardType(apply.getCardType());
			resp.setApplyState(apply.getApplyState());
			resp.setApplyStateName(apply.getApplyStateName());
			resp.setKycState(apply.getKycState());
			resp.setKycStateName(apply.getKycStateName());
			resp.setOpenCardCost(apply.getOpenCardCost());
			resp.setPreSaveCost(apply.getPreSaveCost());
			resp.setLogisticsMonery(apply.getLogisticsMonery());
			resp.setOpenCardTotal(apply.getOpenCardTotal());
			boolean kycApproved = apply.getKycState() != null
					&& apply.getKycState() == WalletKycStateEnums.SUCCESS_APPROVE.getCode();
			resp.setKycSubmitRequired(!kycApproved && card == null);
		}
		if (product != null && resp.getProductId() == null) {
			resp.setProductId(product.getId());
			resp.setCardType(product.getBankcardNature());
		}
		if (!StringUtils.isEmpty(thirdOrderNo)) {
			resp.setOrderNo(thirdOrderNo);
		}
		if (card != null) {
			resp.setUserBankcardId(card.getUserBankcardId());
			resp.setCardNo(card.getCardNo());
			resp.setWalletBankcardId(card.getId());
			if (!StringUtils.isEmpty(card.getApplyOrderNo())) {
				resp.setOrderNo(card.getApplyOrderNo());
			}
			resp.setKycSubmitRequired(false);
		}
		resp.setAutoIssued(autoIssued);
		return resp;
	}

	/** 账户 KYC 是否已通过 */
	private static boolean isKycApproved(WalletAccountEntity account) {
		return account != null
				&& account.getKycState() != null
				&& account.getKycState() == WalletKycStateEnums.SUCCESS_APPROVE.getCode();
	}

	/**
	 * 是否已有开卡所需 KYC 资料（对齐 worldpay user_kyc_null：须上传证件，不要求已通过）。
	 */
	private boolean hasApplyKycMaterial(WalletUserEntity user, WalletApplyCardRequest query) {
		if (query != null && hasKycContent(query.getKycData())) {
			return true;
		}
		List<WalletKycFileEntity> files = walletKycFileDao.findByWalletUserId(user.getId());
		if (files != null && !files.isEmpty()) {
			return true;
		}
		WalletKycApplyEntity latestKyc = walletKycApplyDao.findLatestByWalletUserId(user.getId());
		if (latestKyc != null && !StringUtils.isEmpty(latestKyc.getIdUrl())) {
			return true;
		}
		WalletCardApplyKycEntity history = walletCardApplyKycDao.findLatestByWalletUserId(user.getId());
		return history != null && !StringUtils.isEmpty(history.getFrontPhotoUrl());
	}

	/** 调三方虚拟/实体卡开卡申请 */
	private ThirdBankcardApplyResp issueVirtualCardThird(WalletUserEntity user, WalletCardApplyEntity apply,
			WalletCardProductEntity product, Integer deliveryAddressId) {
		BankcardApplyRequest thirdReq = new BankcardApplyRequest();
		thirdReq.setProductId(product.getId());
		thirdReq.setDeliveryAddressId(deliveryAddressId);
		try {
			return thirdService.applyBankcard(user.getWalletUid(), thirdReq);
		} catch (BaseException e) {
			log.error("wallet issue card third failed applyId={} walletUid={} productId={}",
					apply.getId(), user.getWalletUid(), product.getId(), e);
			throw e;
		} catch (Exception e) {
			log.error("wallet issue card third error applyId={} walletUid={} productId={}",
					apply.getId(), user.getWalletUid(), product.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** 三方开卡成功后落本地卡记录 */
	private WalletBankcardEntity persistIssuedBankcard(WalletUserEntity user, WalletCardApplyEntity apply,
			WalletCardProductEntity product, ThirdBankcardApplyResp third, Date now) {
		if (third == null || third.getUserBankcardId() == null) {
			return null;
		}
		WalletBankcardEntity card = walletBankcardDao.findByUserBankcardId(third.getUserBankcardId());
		if (card == null) {
			Long walletBankcardId = insertAppliedBankcard(user, apply.getId(), product, third, now);
			if (walletBankcardId != null) {
				card = walletBankcardDao.selectById(walletBankcardId);
			}
		}
		return card;
	}

	/**
	 * 按开卡申请单提交 KYC（对齐 worldpay GET /kyc/apply?applyId=）。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase applyKycByCardApply(Integer userType, Integer localUid, Long applyId) {
		if (applyId == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		WalletCardApplyEntity apply = walletCardApplyDao.selectById(applyId);
		if (apply == null || !user.getId().equals(apply.getWalletUserId())) {
			return setResultError(I18nUtil.getMessage("wallet.apply_not_found"));
		}
		ensureWalletAccount(user);
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (isKycApproved(account)) {
			return setResultError(I18nUtil.getMessage("wallet.kyc_already_success"));
		}
		if (account.getKycState() != null
				&& account.getKycState() == WalletKycStateEnums.PROCESS_APPROVE.getCode()) {
			return setResultError(I18nUtil.getMessage("wallet.kyc_processing"));
		}
		WalletCardApplyManEntity man = walletCardApplyManDao.findByApplyId(applyId);
		if (man == null) {
			return setResultError(I18nUtil.getMessage("holder_null"));
		}
		WalletCardApplyKycEntity kyc = walletCardApplyKycDao.findByApplyId(applyId);
		if (kyc == null) {
			return setResultError(I18nUtil.getMessage("kyc_info_null"));
		}
		KycApplyRequest kycReq = buildKycApplyFromCardApply(user, man, kyc);
		try {
			thirdService.applyKyc(user.getWalletUid(), kycReq);
		} catch (BaseException e) {
			log.error("wallet kyc apply by cardApply third failed applyId={} walletUid={}",
					applyId, user.getWalletUid(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet kyc apply by cardApply third error applyId={} walletUid={}",
					applyId, user.getWalletUid(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		Date now = new Date();
		try {
			WalletKycApplyEntity row = buildKycApply(user, kycReq, now);
			walletKycApplyDao.insert(row);
			syncKycLocal(user.getId(), WalletKycApiStatus.WAITING,
					WalletKycStateEnums.PROCESS_APPROVE, null);
			walletCardApplyDao.updateKycSnapshot(applyId,
					WalletKycStateEnums.PROCESS_APPROVE.getCode(),
					WalletKycStateEnums.PROCESS_APPROVE.getLabel(), null);
		} catch (Exception e) {
			log.error("wallet kyc apply by cardApply persist failed applyId={}", applyId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet kyc apply by cardApply success applyId={} walletUserId={}", applyId, user.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** 由开卡申请快照组装三方 KYC 提交参数 */
	private static KycApplyRequest buildKycApplyFromCardApply(WalletUserEntity user,
			WalletCardApplyManEntity man, WalletCardApplyKycEntity kyc) {
		KycApplyRequest req = new KycApplyRequest();
		req.setFirstName(man.getUserName());
		req.setLastName(man.getUserSurname());
		req.setIdNo(StringUtils.isEmpty(kyc.getPaperworkNum()) ? man.getUserNumber() : kyc.getPaperworkNum());
		req.setEmail(StringUtils.isEmpty(man.getUserEmail()) ? user.getEmail() : man.getUserEmail());
		req.setNationCode(man.getUserTelCode());
		req.setCertType(mapPaperworkTypeToCertType(kyc.getPaperworkType()));
		req.setIdUrl(kyc.getFrontPhotoUrl());
		req.setIdBackUrl(kyc.getBackPhotoUrl());
		req.setBirthday(man.getUserBirthday());
		req.setCountryCode(man.getUserTelCode());
		req.setAreaCode(man.getUserTelDialCode());
		req.setPhone(man.getUserTel());
		req.setSelfieUrl(kyc.getHandheldPhotoUrl());
		KycFieldNormalizeUtil.normalizeKycApply(req);
		return req;
	}

	private static Integer mapPaperworkTypeToCertType(String paperworkType) {
		if (WalletConstants.PAPERWORK_PASSPORT.equalsIgnoreCase(paperworkType)) {
			return WalletConstants.KYC_CERT_PASSPORT;
		}
		return WalletConstants.KYC_CERT_ID_CARD;
	}

	/** KYC 通过后尝试自动发放待处理的虚拟卡申请 */
	private void tryAutoIssuePendingVirtualCards(Long walletUserId) {
		WalletUserEntity user = walletUserDao.selectById(walletUserId);
		if (user == null) {
			return;
		}
		List<WalletCardApplyEntity> pendingList = walletCardApplyDao.findPendingVirtualByWalletUserId(walletUserId);
		if (pendingList == null || pendingList.isEmpty()) {
			return;
		}
		for (WalletCardApplyEntity apply : pendingList) {
			if (walletBankcardDao.findByCardApplyId(apply.getId()) != null) {
				continue;
			}
			WalletCardProductEntity product = walletCardProductDao.findById(apply.getCardProductId());
			if (product == null) {
				log.warn("wallet auto issue skip product missing applyId={} productId={}",
						apply.getId(), apply.getCardProductId());
				continue;
			}
			try {
				ThirdBankcardApplyResp third = issueVirtualCardThird(user, apply, product, null);
				if (third == null || third.getUserBankcardId() == null) {
					continue;
				}
				Date now = new Date();
				WalletBankcardEntity card = persistIssuedBankcard(user, apply, product, third, now);
				apply.setKycState(WalletKycStateEnums.SUCCESS_APPROVE.getCode());
				apply.setKycStateName(WalletKycStateEnums.SUCCESS_APPROVE.getLabel());
				apply.setGmtModified(now);
				walletCardApplyDao.updateById(apply);
				walletAccountDao.markActivated(walletUserId);
				if (card != null) {
					finalizeVirtualCardAfterAutoIssue(user, apply, card, third);
				}
				log.info("wallet auto issue virtual card success applyId={} walletUserId={} userBankcardId={}",
						apply.getId(), walletUserId, third.getUserBankcardId());
			} catch (Exception e) {
				log.error("wallet auto issue virtual card failed applyId={} walletUserId={}",
						apply.getId(), walletUserId, e);
			}
		}
	}

	private static boolean isPhysicalCard(String bankcardNature) {
		return WalletConstants.BANKCARD_NATURE_PHYSICAL.equalsIgnoreCase(bankcardNature);
	}

	/** 卡产品是否为虚拟卡 */
	private static boolean isVirtualCardProduct(WalletCardProductEntity product) {
		return product != null
				&& WalletConstants.BANKCARD_NATURE_VIRTUAL.equalsIgnoreCase(product.getBankcardNature());
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
	 * 银行卡充值：从 wallet_account.available_balance 扣款后调三方充卡，结果由 Webhook 回写。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase rechargeCard(Integer userType, Integer localUid, BankcardRechargeRequest query) {
		WalletUserEntity user = findWalletUser(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (query == null || query.getUserBankcardId() == null || query.getAmount() == null
				|| query.getAmount() <= 0) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		String requestOrderId = WalletRequestOrderIdSupport.resolve(query.getRequestOrderId(),
				WalletConstants.REQUEST_ORDER_PREFIX_CARD_RECHARGE, user.getWalletUid());
		query.setRequestOrderId(requestOrderId);
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		BigDecimal amount = BigDecimal.valueOf(query.getAmount());
		// 幂等：同一 requestOrderId 不重复扣款
		WalletCardTransactionEntity existed = walletCardTransactionDao.findByRequestOrderId(requestOrderId);
		if (existed != null) {
			log.info("wallet card recharge idempotent skip deduct requestOrderId={} walletAccountId={}",
					requestOrderId, account.getId());
			return setResultSuccess(buildCardRechargeResp(requestOrderId, amount, account.getAvailableBalance(), true),
					I18nUtil.getMessage("base_success"));
		}
		WalletBankcardEntity card = findOwnedCard(user, query.getUserBankcardId());
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		BigDecimal balanceBefore = nvlBalance(account.getAvailableBalance());
		// 先扣本地钱包可用余额，再调三方充卡
		int deducted = walletAccountDao.deductAvailableBalance(account.getId(), amount);
		if (deducted <= 0) {
			log.warn("wallet card recharge deduct failed walletAccountId={} requestOrderId={} amount={} balanceBefore={}",
					account.getId(), query.getRequestOrderId(), amount, balanceBefore);
			return setResultError(I18nUtil.getMessage("wallet.balance_not_enough"));
		}
		log.info("wallet card recharge deducted walletAccountId={} requestOrderId={} amount={} balanceBefore={}",
				account.getId(), query.getRequestOrderId(), amount, balanceBefore);
		try {
			thirdService.rechargeBankcard(user.getWalletUid(), query);
		} catch (BaseException e) {
			log.error("wallet card recharge rejected walletUid={} requestOrderId={}",
					user.getWalletUid(), query.getRequestOrderId(), e);
			refundAvailableBalance(account.getId(), amount, query.getRequestOrderId());
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card recharge error walletUid={} requestOrderId={}",
					user.getWalletUid(), query.getRequestOrderId(), e);
			refundAvailableBalance(account.getId(), amount, query.getRequestOrderId());
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		insertRechargeTransaction(user, card, query);
		WalletAccountEntity refreshedAccount = walletAccountDao.findByWalletUserId(user.getId());
		BigDecimal availableAfter = refreshedAccount == null ? null : refreshedAccount.getAvailableBalance();
		log.info("wallet card recharge submitted walletUserId={} userBankcardId={} requestOrderId={} amount={}",
				user.getId(), query.getUserBankcardId(), requestOrderId, amount);
		return setResultSuccess(buildCardRechargeResp(requestOrderId, amount, availableAfter, false),
				I18nUtil.getMessage("base_success"));
	}

	private static WalletCardRechargeResp buildCardRechargeResp(String requestOrderId, BigDecimal amount,
			BigDecimal availableBalance, boolean idempotent) {
		WalletCardRechargeResp resp = new WalletCardRechargeResp();
		resp.setRequestOrderId(requestOrderId);
		resp.setAmount(amount);
		resp.setAvailableBalance(availableBalance);
		resp.setIdempotent(idempotent);
		return resp;
	}

	/** 三方充卡失败时退回已扣的 available_balance */
	private void refundAvailableBalance(Long accountId, BigDecimal amount, String requestOrderId) {
		int rows = walletAccountDao.addAvailableBalance(accountId, amount);
		if (rows <= 0) {
			log.error("wallet card recharge refund failed accountId={} requestOrderId={} amount={}",
					accountId, requestOrderId, amount);
			throw new BaseException(I18nUtil.getMessage("base_error"));
		}
		log.info("wallet card recharge refunded accountId={} requestOrderId={} amount={}",
				accountId, requestOrderId, amount);
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
			// 查询成功时回写本地 card_no，避免列表长期为空
			if (resp != null && !StringUtils.isEmpty(resp.getCardNumber())) {
				walletBankcardDao.updateCardNo(card.getId(), resp.getCardNumber().trim());
			}
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

	/** 虚拟卡 KYC 已通过且三方开卡成功：首充、回写卡号、标记激活成功并核销冻结 */
	public void finalizeVirtualCardAfterAutoIssue(WalletUserEntity user, WalletCardApplyEntity apply,
			WalletBankcardEntity card, ThirdBankcardApplyResp third) {
		if (user == null || apply == null || card == null) {
			return;
		}
		walletOpenCardSettlementService.afterVirtualCardIssued(user, apply, card);
		if (third != null && !StringUtils.isEmpty(third.getCardNo())) {
			walletBankcardDao.updateCardNo(card.getId(), third.getCardNo());
			card.setCardNo(third.getCardNo());
		}
		walletBankcardSyncSupport.syncCardNo(card);
		walletBankcardDao.updateCardStatus(card.getId(),
				WalletCardStatusEnums.ACTIVE.getCode(), WalletCardStatusEnums.ACTIVE.getLabel());
		card.setCardStatus(WalletCardStatusEnums.ACTIVE.getCode());
		card.setCardStatusName(WalletCardStatusEnums.ACTIVE.getLabel());
		walletOpenCardSettlementService.onCardActivated(card);
		apply.setApplyState(WalletCardApplyStateEnums.SUCCESS_ACTIVATION.getCode());
		apply.setApplyStateName(WalletCardApplyStateEnums.SUCCESS_ACTIVATION.getLabel());
		log.info("wallet virtual card finalized applyId={} userBankcardId={}",
				apply.getId(), card.getUserBankcardId());
	}

	/** 申请成功后写入本地卡记录 */
	private Long insertAppliedBankcard(WalletUserEntity user, Long applyId, WalletCardProductEntity product,
			ThirdBankcardApplyResp third, Date now) {
		WalletBankcardEntity card = new WalletBankcardEntity();
		card.setWalletUserId(user.getId());
		card.setWalletUid(user.getWalletUid());
		card.setCardApplyId(applyId);
		card.setCardProductId(product == null ? null : product.getId());
		card.setUserBankcardId(third.getUserBankcardId());
		card.setCardNo(third.getCardNo());
		card.setBankcardNature(product == null ? null : product.getBankcardNature());
		card.setCardBrand(product == null ? null : product.getCardBrand());
		card.setCurrency(WalletConstants.DEFAULT_CURRENCY);
		// 虚拟卡三方开卡后：KYC 已通过则直接标记正常，否则激活中
		if (isVirtualCardProduct(product)) {
			card.setCardStatus(WalletCardStatusEnums.ACTIVE.getCode());
			card.setCardStatusName(WalletCardStatusEnums.ACTIVE.getLabel());
		} else {
			card.setCardStatus(WalletCardStatusEnums.WAIT_ACTIVE.getCode());
			card.setCardStatusName(WalletCardStatusEnums.WAIT_ACTIVE.getLabel());
		}
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
	 * KYC 证件上传：透传三方 POST /api/file/upload，可选落 wallet_kyc_file 留痕。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase uploadKycFile(Integer userType, Integer localUid, MultipartFile idCard,
			Integer certType, Integer documentType) {
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		if (user.getWalletUid() == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		WalletKycFileUploadResp uploadResp;
		try {
			uploadResp = thirdService.uploadKycFile(user.getWalletUid(), idCard);
		} catch (Exception e) {
			log.error("wallet kyc file upload third error walletUid={}", user.getWalletUid(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (certType != null && documentType != null) {
			persistKycFileRecord(user, certType, documentType, uploadResp.getFileUrl());
		}
		log.info("wallet kyc file upload success walletUserId={} walletUid={} documentType={}",
				user.getId(), user.getWalletUid(), documentType);
		return setResultSuccess(uploadResp, I18nUtil.getMessage("base_success"));
	}

	private void persistKycFileRecord(WalletUserEntity user, Integer certType, Integer documentType,
			String fileUrl) {
		if (StringUtils.isEmpty(fileUrl)) {
			return;
		}
		Date now = new Date();
		WalletKycFileEntity row = new WalletKycFileEntity();
		row.setWalletUserId(user.getId());
		row.setWalletUid(user.getWalletUid());
		row.setCertType(certType);
		row.setDocumentType(documentType);
		row.setDocumentFileUrl(fileUrl.trim());
		row.setSetTime(now);
		row.setGmtModified(now);
		try {
			walletKycFileDao.insert(row);
		} catch (Exception e) {
			log.error("wallet kyc file persist failed walletUserId={} documentType={}",
					user.getId(), documentType, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
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
		if (localState == WalletKycStateEnums.SUCCESS_APPROVE) {
			// KYC 通过：实体卡待发货 + 虚拟卡自动开卡
			walletOpenCardSettlementService.markPhysicalWaitShippingOnKycSuccess(walletUserId);
			tryAutoIssuePendingVirtualCards(walletUserId);
		} else if (localState == WalletKycStateEnums.ERROR_APPROVE) {
			// KYC 失败：认证中申请单标记失败并解冻
			walletOpenCardSettlementService.failKycPendingApplies(walletUserId, failedReason);
		}
	}

	/**
	 * 定时任务：轮询 KYC 认证中的开卡申请（对齐 onetoken ScheduledTasks.findKycState）。
	 */
	public void pollPendingKycApplies() {
		List<WalletCardApplyEntity> pendingList = walletCardApplyDao.findByKycState(
				WalletKycStateEnums.PROCESS_APPROVE.getCode());
		if (pendingList == null || pendingList.isEmpty()) {
			return;
		}
		for (WalletCardApplyEntity apply : pendingList) {
			if (apply.getWalletUserId() == null) {
				continue;
			}
			WalletUserEntity user = walletUserDao.selectById(apply.getWalletUserId());
			if (user == null || user.getWalletUid() == null) {
				continue;
			}
			KycStatusResp third;
			try {
				third = thirdService.getKycStatus(user.getWalletUid());
			} catch (Exception e) {
				log.error("wallet kyc poll third error applyId={} walletUid={}",
						apply.getId(), user.getWalletUid(), e);
				continue;
			}
			if (third == null || StringUtils.isEmpty(third.getStatus())) {
				continue;
			}
			WalletKycStateEnums localState = WalletKycStateEnums.fromApiStatus(third.getStatus());
			if (localState == WalletKycStateEnums.PROCESS_APPROVE
					|| localState == WalletKycStateEnums.WAIT_APPROVE) {
				continue;
			}
			try {
				syncKycLocal(user.getId(), third.getStatus(), localState, third.getFailedReason());
				if (localState == WalletKycStateEnums.SUCCESS_APPROVE
						&& WalletConstants.BANKCARD_NATURE_VIRTUAL.equalsIgnoreCase(apply.getCardType())
						&& Integer.valueOf(WalletCardApplyStateEnums.WAIT_ACTIVATION.getCode())
								.equals(apply.getApplyState())) {
					// 单条虚拟卡申请：KYC 通过后补开卡（tryAutoIssue 已覆盖大部分场景）
					tryAutoIssuePendingVirtualCards(user.getId());
				}
			} catch (Exception e) {
				log.error("wallet kyc poll sync failed applyId={} walletUserId={} status={}",
						apply.getId(), user.getId(), third.getStatus(), e);
			}
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

	/** 支付密码校验（对齐 onetoken checkPayPasswrod） */
	public ResponseBase verifyPayPassword(WalletAccountEntity account, String password) {
		return checkPayPassword(account, password);
	}

	/** 支付密码校验（对齐 onetoken checkPayPasswrod） */
	private ResponseBase checkPayPassword(WalletAccountEntity account, String password) {
		if (StringUtils.isEmpty(password)) {
			return setResultError(I18nUtil.getMessage("pay_password_entry"));
		}
		if (StringUtils.isEmpty(account.getPayPassword())) {
			return setResult(Constants.HTTP_RES_CODE_602, I18nUtil.getMessage("pay_password_null"), null);
		}
		if (!PasswordHashUtils.matches(password, account.getPayPassword())) {
			return setResultError(I18nUtil.getMessage("pay_password_error"));
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** 开卡费用：月费 + 开卡费 + 预存费 + 邮费 */
	private static OpenCardFeeBundle resolveOpenCardFees(WalletCardProductEntity product,
			WalletApplyCardRequest query, boolean physicalCard) {
		BigDecimal monthFee = nz(product.getMonthFee());
		BigDecimal openCardCost = query.getOpenCardCost() != null
				? query.getOpenCardCost() : WalletCardProductService.resolveOpenCardCost(product);
		BigDecimal logisticsMonery = BigDecimal.ZERO;
		if (physicalCard) {
			if (query.getLogisticsMonery() != null) {
				logisticsMonery = query.getLogisticsMonery();
			} else {
				logisticsMonery = WalletCardProductService.resolveLogisticsMonery(product);
			}
		}
		BigDecimal preSaveCost = physicalCard
				? BigDecimal.ZERO : WalletCardProductService.resolvePreSaveCost(product);
		BigDecimal openCardTotal = monthFee.add(openCardCost).add(preSaveCost).add(logisticsMonery);
		OpenCardFeeBundle bundle = new OpenCardFeeBundle();
		bundle.openCardCost = openCardCost;
		bundle.preSaveCost = preSaveCost;
		bundle.logisticsMonery = logisticsMonery;
		bundle.openCardTotal = openCardTotal;
		return bundle;
	}

	/** 申请开卡冻结：可用余额 → 开卡冻结余额 */
	private void freezeOpenCardBalance(WalletAccountEntity account, BigDecimal amount, Long applyId) {
		try {
			int rows = walletAccountDao.freezeOpenCardBalance(account.getId(), amount);
			if (rows <= 0) {
				throw new BaseException(I18nUtil.getMessage("wallet.balance_not_enough"));
			}
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("wallet apply card freeze balance failed applyId={} walletUserId={} amount={}",
					applyId, account.getWalletUserId(), amount, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet apply card freeze balance applyId={} walletUserId={} amount={}",
				applyId, account.getWalletUserId(), amount);
	}

	private static BigDecimal nz(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	/** 开卡费用明细 */
	private static final class OpenCardFeeBundle {
		private BigDecimal openCardCost;
		private BigDecimal preSaveCost;
		private BigDecimal logisticsMonery;
		private BigDecimal openCardTotal;
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
		walletBankcardSyncSupport.syncCardNo(card);
		walletOpenCardSettlementService.onCardActivated(card);
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

	/** 按本地 wallet_bankcard.id 校验归属 */
	private WalletBankcardEntity findOwnedCardById(WalletUserEntity user, Long walletBankcardId) {
		if (user == null || walletBankcardId == null) {
			return null;
		}
		WalletBankcardEntity card = walletBankcardDao.selectById(walletBankcardId);
		if (card == null || !user.getId().equals(card.getWalletUserId())) {
			return null;
		}
		return card;
	}

	private static WalletCardDetailResp toCardDetail(WalletBankcardEntity row, WalletCardProductEntity product,
			WalletCardApplyEntity apply, WalletUserHolderEntity holder) {
		WalletCardDetailResp detail = new WalletCardDetailResp();
		WalletCardItemResp item = toCardItem(row, product);
		detail.setId(item.getId());
		detail.setUserBankcardId(item.getUserBankcardId());
		detail.setDisplayName(item.getDisplayName());
		detail.setCardTitle(item.getCardTitle());
		detail.setCardNo(item.getCardNo());
		detail.setCardBrand(item.getCardBrand());
		detail.setCardProductId(item.getCardProductId());
		detail.setBankcardNature(item.getBankcardNature());
		detail.setCurrency(item.getCurrency());
		detail.setCardStatus(item.getCardStatus());
		detail.setCardStatusName(item.getCardStatusName());
		detail.setBalance(item.getBalance());
		detail.setIsDefault(item.getIsDefault());
		detail.setPinSet(item.getPinSet());
		detail.setTagName(item.getTagName());
		detail.setCardImg(item.getCardImg());
		detail.setSetTime(item.getSetTime());
		detail.setWalletUid(row.getWalletUid());
		detail.setCardApplyId(row.getCardApplyId());
		detail.setCardUuid(product == null ? row.getCardUuid() : product.getProductUuid());
		detail.setHolderData(holder);
		detail.setCardData(product);
		if (apply != null) {
			fillShippingFields(detail, row, apply);
		}
		return detail;
	}

	/** 卡片详情页费用展示：月服务费 + 充值/提现费率文案 */
	private static void fillCardFeeFields(WalletCardDetailResp detail, WalletCardProductEntity product) {
		if (product == null) {
			detail.setMonthServiceFee("0.00");
			detail.setUsdRechargeFee("0%");
			detail.setUsdtRechargeFee("0%");
			detail.setWithdrawFee("0%");
			return;
		}
		detail.setMonthServiceFee(formatAmount(product.getMonthFee()));
		String rechargePercent = formatPercent(product.getRechargeFee());
		detail.setUsdRechargeFee(rechargePercent);
		// 产品未单独配置 USDT/提现费率时，与 USD 充值费率保持一致
		detail.setUsdtRechargeFee(rechargePercent);
		detail.setWithdrawFee(rechargePercent);
	}

	private static String formatAmount(BigDecimal amount) {
		if (amount == null) {
			return "0.00";
		}
		return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

	/** 费率 0.01 → 1.0% */
	private static String formatPercent(BigDecimal rate) {
		if (rate == null) {
			return "0%";
		}
		BigDecimal percent = rate.multiply(new BigDecimal("100"));
		String text = percent.stripTrailingZeros().toPlainString();
		if (!text.contains(".")) {
			return text + ".0%";
		}
		return text + "%";
	}

	private static void fillShippingFields(WalletCardDetailResp detail, WalletBankcardEntity card,
			WalletCardApplyEntity apply) {
		Integer shippingState = card.getShippingState() != null ? card.getShippingState() : apply.getShippingState();
		detail.setShippingState(shippingState);
		if (!StringUtils.isEmpty(apply.getShippingStateName())) {
			detail.setShippingStateName(apply.getShippingStateName());
		} else {
			detail.setShippingStateName(WalletLogisticsStateEnums.fromCode(shippingState).getLabel());
		}
		detail.setShippingTime(apply.getShippingTime());
		String logisticsNum = !StringUtils.isEmpty(card.getLogisticsNum())
				? card.getLogisticsNum() : apply.getLogisticsNum();
		detail.setLogisticsNum(logisticsNum);
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

	private static WalletCardItemResp toCardItem(WalletBankcardEntity row, WalletCardProductEntity product) {
		String cardTitle = product == null ? null : product.getCardTitle();
		WalletCardItemResp item = new WalletCardItemResp();
		item.setId(row.getId());
		item.setUserBankcardId(row.getUserBankcardId());
		item.setCardTitle(cardTitle);
		item.setDisplayName(buildCardDisplayName(row, cardTitle));
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
		item.setCardImg(product == null ? null : product.getCardImg());
		item.setSetTime(row.getSetTime());
		return item;
	}

	/** 按产品 id 批量加载卡图、卡名称等展示字段 */
	private Map<Integer, WalletCardProductEntity> loadCardProductMap(Set<Integer> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<Integer> ids = new ArrayList<>(productIds);
		List<WalletCardProductEntity> rows = walletCardProductDao.findCardImgByIds(ids);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, WalletCardProductEntity> map = new HashMap<>(rows.size());
		for (WalletCardProductEntity row : rows) {
			if (row.getId() != null) {
				map.put(row.getId(), row);
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

	/** 展示名：自定义标签 > 卡产品 cardTitle > 品牌-尾号 */
	private static String buildCardDisplayName(WalletBankcardEntity row, String cardTitle) {
		if (!StringUtils.isEmpty(row.getTagName())) {
			return row.getTagName();
		}
		if (!StringUtils.isEmpty(cardTitle)) {
			return cardTitle;
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
