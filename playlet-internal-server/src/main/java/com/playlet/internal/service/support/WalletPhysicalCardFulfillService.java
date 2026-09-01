package com.playlet.internal.service.support;

import com.playlet.internal.api.request.BankcardActiveRequest;
import com.playlet.internal.api.request.BankcardSetPinRequest;
import com.playlet.internal.api.request.WalletCardShippingRequest;
import com.playlet.internal.api.response.EmsTrackingInfoResp;
import com.playlet.internal.api.response.ThirdBankcardActiveResp;
import com.playlet.internal.api.response.WalletLogisticsEventResp;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.EmsTrackingConstants;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletAccountDao;
import com.playlet.internal.dao.wallet.WalletBankcardDao;
import com.playlet.internal.dao.wallet.WalletCardApplyDao;
import com.playlet.internal.dao.wallet.WalletCardApplyManDao;
import com.playlet.internal.dao.wallet.WalletCardApplySendDao;
import com.playlet.internal.dao.wallet.WalletCardProductDao;
import com.playlet.internal.dao.wallet.WalletCardShippingDao;
import com.playlet.internal.dao.wallet.WalletUserDao;
import com.playlet.internal.entity.wallet.WalletAccountEntity;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import com.playlet.internal.entity.wallet.WalletCardApplyManEntity;
import com.playlet.internal.entity.wallet.WalletCardApplySendEntity;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import com.playlet.internal.entity.wallet.WalletCardShippingEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.WalletCardApplyStateEnums;
import com.playlet.internal.enums.WalletCardStatusEnums;
import com.playlet.internal.enums.WalletKycStateEnums;
import com.playlet.internal.enums.WalletLogisticsStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.third.ThirdService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 实体卡履约：发货 / 绑卡激活 / 改物流 / 查轨迹（对齐 worldpay CardService）。
 */
@Slf4j
@Service
public class WalletPhysicalCardFulfillService {

	@Autowired
	private WalletCardApplyDao walletCardApplyDao;
	@Autowired
	private WalletCardApplyManDao walletCardApplyManDao;
	@Autowired
	private WalletCardApplySendDao walletCardApplySendDao;
	@Autowired
	private WalletCardProductDao walletCardProductDao;
	@Autowired
	private WalletCardShippingDao walletCardShippingDao;
	@Autowired
	private WalletBankcardDao walletBankcardDao;
	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private ThirdService thirdService;
	@Autowired
	private EmsTrackingService emsTrackingService;

	/**
	 * 实体卡分配激活：绑定卡号并调三方激活。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase cardBinding(Long applyId, String cardNumber, String pinNum) {
		if (applyId == null || StringUtils.isEmpty(cardNumber)) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		if (StringUtils.isEmpty(pinNum)) {
			return setResultError(I18nUtil.getMessage("pin_null"));
		}
		WalletCardApplyEntity apply = walletCardApplyDao.selectById(applyId);
		if (apply == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_not_found"));
		}
		ResponseBase check = validatePhysicalBinding(apply, applyId);
		if (check != null) {
			return check;
		}
		WalletUserEntity user = walletUserDao.selectById(apply.getWalletUserId());
		if (user == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		WalletCardProductEntity product = walletCardProductDao.findById(apply.getCardProductId());
		if (product == null) {
			return setResultError(I18nUtil.getMessage("bank_card_null"));
		}
		// 开卡费扣减（对齐 worldpay 实体卡绑卡扣费）
		BigDecimal openCardCost = apply.getOpenCardCost() == null ? BigDecimal.ZERO : apply.getOpenCardCost();
		if (openCardCost.compareTo(BigDecimal.ZERO) > 0) {
			WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
			if (account == null || account.getAvailableBalance() == null
					|| account.getAvailableBalance().compareTo(openCardCost) < 0) {
				return setResultError(I18nUtil.getMessage("wallet.balance_not_enough"));
			}
		}
		BankcardActiveRequest activeReq = buildActiveRequest(apply, product, cardNumber.trim());
		ThirdBankcardActiveResp third;
		try {
			third = thirdService.activeBankcard(user.getWalletUid(), activeReq);
		} catch (BaseException e) {
			log.error("physical card binding third failed applyId={} walletUid={}", applyId, user.getWalletUid(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("physical card binding third error applyId={}", applyId, e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (third == null || third.getUserBankcardId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		Date now = new Date();
		insertPhysicalBankcard(user, apply, product, third, cardNumber.trim(), now);
		setPinAfterBinding(user, third.getUserBankcardId(), pinNum.trim());
		if (openCardCost.compareTo(BigDecimal.ZERO) > 0) {
			deductOpenCardCost(user.getId(), openCardCost);
		}
		apply.setApplyState(WalletCardApplyStateEnums.PROCESS_ACTIVATION.getCode());
		apply.setApplyStateName(WalletCardApplyStateEnums.PROCESS_ACTIVATION.getLabel());
		apply.setGmtModified(now);
		try {
			walletCardApplyDao.updateById(apply);
			walletAccountDao.markActivated(user.getId());
		} catch (Exception e) {
			log.error("physical card binding update apply failed applyId={}", applyId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("physical card binding success applyId={} walletUid={} userBankcardId={}",
				applyId, user.getWalletUid(), third.getUserBankcardId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 实体卡首次发货。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase shipping(WalletCardShippingRequest request, String operateUserIp) {
		if (request == null || request.getApplyId() == null || StringUtils.isEmpty(request.getLogisticsNum())) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WalletCardApplyEntity apply = walletCardApplyDao.selectById(request.getApplyId());
		if (apply == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_not_found"));
		}
		if (!isPhysicalApply(apply)) {
			return setResultError(I18nUtil.getMessage("physical_card_no"));
		}
		if (apply.getShippingState() != null
				&& apply.getShippingState() != WalletLogisticsStateEnums.WAIT_SUCCESS.getCode()) {
			return setResultError(I18nUtil.getMessage("shipping_already"));
		}
		String logisticsNum = request.getLogisticsNum().trim();
		if (!emsTrackingService.isValidTrackingNumber(logisticsNum)) {
			return setResultError(I18nUtil.getMessage("logistics_error"));
		}
		String orderNo = resolveShippingOrderNo(apply);
		try {
			emsTrackingService.registerTrackingNumber(logisticsNum, orderNo);
		} catch (BaseException e) {
			log.error("physical card shipping register ems failed applyId={} logisticsNum={}",
					request.getApplyId(), logisticsNum, e);
			return setResultError(e.getMessage());
		}
		BigDecimal freight = request.getLogisticsMonery() == null ? BigDecimal.ZERO : request.getLogisticsMonery();
		Date now = new Date();
		WalletCardShippingEntity shipping = buildShippingEntityWithMan(request, apply, logisticsNum, freight, operateUserIp, now);
		try {
			walletCardShippingDao.insert(shipping);
		} catch (DuplicateKeyException e) {
			log.warn("physical card shipping duplicate applyId={}", request.getApplyId(), e);
			return setResultError(I18nUtil.getMessage("shipping_already"));
		} catch (Exception e) {
			log.error("physical card shipping insert failed applyId={}", request.getApplyId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		try {
			walletCardApplyDao.updateShipping(apply.getId(),
					WalletLogisticsStateEnums.ALREADY_SHIPPING.getCode(),
					WalletLogisticsStateEnums.ALREADY_SHIPPING.getLabel(),
					logisticsNum, now, freight);
		} catch (Exception e) {
			log.error("physical card shipping update apply failed applyId={}", apply.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		deductShippingFreight(apply, freight);
		log.info("physical card shipping success applyId={} logisticsNum={}", apply.getId(), logisticsNum);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 修改已发货订单的物流单号。
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase upLogisticsNum(Long applyId, String logisticsNum) {
		if (applyId == null || StringUtils.isEmpty(logisticsNum)) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WalletCardApplyEntity apply = walletCardApplyDao.selectById(applyId);
		if (apply == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_not_found"));
		}
		WalletBankcardEntity userCard = walletBankcardDao.findByCardApplyId(applyId);
		if (userCard == null) {
			return setResultError(I18nUtil.getMessage("bank_not_binding"));
		}
		if (StringUtils.isEmpty(userCard.getLogisticsNum()) && StringUtils.isEmpty(apply.getLogisticsNum())) {
			return setResultError(I18nUtil.getMessage("shipping_not"));
		}
		String newNum = logisticsNum.trim();
		if (!emsTrackingService.isValidTrackingNumber(newNum)) {
			return setResultError(I18nUtil.getMessage("logistics_error"));
		}
		try {
			emsTrackingService.registerTrackingNumber(newNum,
					StringUtils.isEmpty(userCard.getCardNo()) ? String.valueOf(applyId) : userCard.getCardNo());
		} catch (BaseException e) {
			log.error("up logistics register ems failed applyId={} logisticsNum={}", applyId, newNum, e);
			return setResultError(e.getMessage());
		}
		Date now = new Date();
		try {
			walletBankcardDao.updateLogistics(userCard.getId(),
					WalletLogisticsStateEnums.ALREADY_SHIPPING.getCode(), newNum);
			walletCardApplyDao.updateShipping(apply.getId(),
					WalletLogisticsStateEnums.ALREADY_SHIPPING.getCode(),
					WalletLogisticsStateEnums.ALREADY_SHIPPING.getLabel(),
					newNum, apply.getShippingTime(), apply.getLogisticsMonery());
			WalletCardShippingEntity shipping = walletCardShippingDao.findByApplyId(applyId);
			if (shipping != null) {
				shipping.setLogisticsNum(newNum);
				shipping.setGmtModified(now);
				walletCardShippingDao.updateById(shipping);
			}
		} catch (Exception e) {
			log.error("up logistics update failed applyId={}", applyId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("up logistics success applyId={} logisticsNum={}", applyId, newNum);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 查询物流轨迹并回写发货状态。
	 *
	 * @param ownerWalletUserId 非空时校验申请单归属（C 端）；管理端传 null
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase findLogistics(String logisticsNum, Long applyId, Long ownerWalletUserId) {
		if (StringUtils.isEmpty(logisticsNum)) {
			return setResultError(I18nUtil.getMessage("logistics_not_binding"));
		}
		String num = logisticsNum.trim();
		List<WalletCardApplyEntity> applyList = walletCardApplyDao.findByLogisticsNum(num);
		if (applyList == null || applyList.isEmpty()) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WalletCardApplyEntity result = resolveLogisticsApply(applyList, applyId, ownerWalletUserId);
		if (result == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_not_found"));
		}
		EmsTrackingInfoResp tracking;
		try {
			tracking = emsTrackingService.queryTrackingInfo(num);
		} catch (BaseException e) {
			log.error("find logistics ems failed logisticsNum={} applyId={}", num, applyId, e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("find logistics ems error logisticsNum={}", num, e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletLogisticsStateEnums shippingState = resolveShippingStateFromEms(tracking);
		List<WalletLogisticsEventResp> events = extractLogisticsEvents(tracking, result);
		if (shippingState != null) {
			syncLogisticsState(applyList, num, shippingState);
			result.setShippingState(shippingState.getCode());
			result.setShippingStateName(shippingState.getLabel());
		}
		result.setLogisticsNum(num);
		if (result.getCardProductId() != null) {
			result.setCardData(walletCardProductDao.findById(result.getCardProductId()));
		}
		WalletBankcardEntity userCard = walletBankcardDao.findByCardApplyId(result.getId());
		if (userCard != null) {
			result.setUserCardData(userCard);
		}
		result.setLogisticsInfo(events);
		return setResultSuccess(result, I18nUtil.getMessage("base_success"));
	}

	private ResponseBase validatePhysicalBinding(WalletCardApplyEntity apply, Long applyId) {
		if (!isPhysicalApply(apply)) {
			return setResultError(I18nUtil.getMessage("physical_card_no"));
		}
		if (apply.getKycState() == null
				|| apply.getKycState() != WalletKycStateEnums.SUCCESS_APPROVE.getCode()) {
			return setResultError(I18nUtil.getMessage("user_kyc_state"));
		}
		if (Integer.valueOf(WalletCardApplyStateEnums.SUCCESS_ACTIVATION.getCode()).equals(apply.getApplyState())) {
			return setResultError(I18nUtil.getMessage("bank_card_binding"));
		}
		if (walletBankcardDao.findByCardApplyId(applyId) != null) {
			return setResultError(I18nUtil.getMessage("bank_card_binding"));
		}
		if (walletCardApplySendDao.findByApplyId(applyId) == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_physical_address_required"));
		}
		return null;
	}

	private static boolean isPhysicalApply(WalletCardApplyEntity apply) {
		return WalletConstants.BANKCARD_NATURE_PHYSICAL.equalsIgnoreCase(apply.getCardType());
	}

	private BankcardActiveRequest buildActiveRequest(WalletCardApplyEntity apply,
			WalletCardProductEntity product, String cardNumber) {
		WalletCardApplyManEntity man = walletCardApplyManDao.findByApplyId(apply.getId());
		WalletCardApplySendEntity send = walletCardApplySendDao.findByApplyId(apply.getId());
		BankcardActiveRequest req = new BankcardActiveRequest();
		req.setProductId(product.getId());
		req.setCardNo(cardNumber);
		if (man != null) {
			req.setMobilePrefix(man.getUserTelDialCode());
			req.setMobile(man.getUserTel());
			req.setCountryCode(man.getUserTelCode());
		}
		if (send != null) {
			req.setAddress(send.getAddressInfo());
			req.setCity(StringUtils.isEmpty(send.getCity()) ? send.getProvince() : send.getCity());
			req.setState(send.getProvince());
			req.setPostCode(send.getPostCode());
		}
		return req;
	}

	private void insertPhysicalBankcard(WalletUserEntity user, WalletCardApplyEntity apply,
			WalletCardProductEntity product, ThirdBankcardActiveResp third, String cardNumber, Date now) {
		WalletBankcardEntity card = new WalletBankcardEntity();
		card.setWalletUserId(user.getId());
		card.setWalletUid(user.getWalletUid());
		card.setCardApplyId(apply.getId());
		card.setCardProductId(product.getId());
		card.setCardUuid(product.getProductUuid());
		card.setUserBankcardId(third.getUserBankcardId());
		card.setCardNo(StringUtils.isEmpty(third.getCardNo()) ? cardNumber : third.getCardNo());
		card.setCardType(product.getBankcardNature());
		card.setBankcardNature(product.getBankcardNature());
		card.setCardBrand(product.getCardBrand());
		card.setCurrency(WalletConstants.DEFAULT_CURRENCY);
		card.setCardStatus(WalletCardStatusEnums.WAIT_ACTIVE.getCode());
		card.setCardStatusName(WalletCardStatusEnums.WAIT_ACTIVE.getLabel());
		card.setBalance(BigDecimal.ZERO);
		card.setPinSet(0);
		card.setIsDefault(WalletConstants.CARD_DEFAULT_NO);
		card.setLogisticsNum(apply.getLogisticsNum());
		card.setShippingState(apply.getShippingState());
		card.setSetTime(now);
		card.setGmtModified(now);
		if (walletBankcardDao.findDefaultByWalletUserId(user.getId()) == null) {
			card.setIsDefault(WalletConstants.CARD_DEFAULT_YES);
		}
		try {
			walletBankcardDao.insert(card);
		} catch (Exception e) {
			log.error("physical bankcard insert failed applyId={}", apply.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private void setPinAfterBinding(WalletUserEntity user, Long userBankcardId, String pinNum) {
		BankcardSetPinRequest pinReq = new BankcardSetPinRequest();
		pinReq.setUserBankcardId(userBankcardId);
		pinReq.setPin(pinNum);
		try {
			thirdService.setBankcardPin(user.getWalletUid(), pinReq);
			WalletBankcardEntity card = walletBankcardDao.findByUserBankcardId(userBankcardId);
			if (card != null) {
				walletBankcardDao.updatePinSet(card.getId(), 1);
			}
		} catch (Exception e) {
			log.error("physical card set pin failed walletUid={} userBankcardId={}",
					user.getWalletUid(), userBankcardId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private void deductOpenCardCost(Long walletUserId, BigDecimal amount) {
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(walletUserId);
		if (account == null) {
			return;
		}
		try {
			int rows = walletAccountDao.deductAvailableBalance(account.getId(), amount);
			if (rows <= 0) {
				throw new BaseException(I18nUtil.getMessage("wallet.balance_not_enough"));
			}
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("deduct open card cost failed walletUserId={}", walletUserId, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private void deductShippingFreight(WalletCardApplyEntity apply, BigDecimal freight) {
		if (freight == null || freight.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		if (!Integer.valueOf(WalletConstants.TOPUP_TYPE_WALLET).equals(apply.getTopupType())) {
			return;
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(apply.getWalletUserId());
		if (account == null) {
			return;
		}
		try {
			int rows = walletAccountDao.deductAvailableBalance(account.getId(), freight);
			if (rows <= 0) {
				log.warn("deduct shipping freight skipped applyId={} amount={}", apply.getId(), freight);
			}
		} catch (Exception e) {
			log.error("deduct shipping freight failed applyId={}", apply.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private static WalletCardShippingEntity buildShippingEntity(WalletCardShippingRequest request,
			WalletCardApplyEntity apply, String logisticsNum, BigDecimal freight, String operateUserIp, Date now) {
		WalletCardShippingEntity entity = new WalletCardShippingEntity();
		entity.setApplyId(apply.getId());
		entity.setWalletUserId(apply.getWalletUserId());
		entity.setWalletUid(apply.getWalletUid());
		entity.setLogisticsNum(logisticsNum);
		entity.setLogisticsMonery(freight);
		entity.setLogisticsProviders(request.getLogisticsProviders());
		entity.setLogisticsState(WalletLogisticsStateEnums.ALREADY_SHIPPING.getCode());
		entity.setLogisticsStateName(WalletLogisticsStateEnums.ALREADY_SHIPPING.getLabel());
		entity.setOperateUserId(request.getOperateUserId());
		entity.setOperateUserName(request.getOperateUserName());
		entity.setOperateUserIp(operateUserIp);
		entity.setSetTime(now);
		entity.setGmtModified(now);
		return entity;
	}

	private WalletCardShippingEntity buildShippingEntityWithMan(WalletCardShippingRequest request,
			WalletCardApplyEntity apply, String logisticsNum, BigDecimal freight, String operateUserIp, Date now) {
		WalletCardShippingEntity entity = buildShippingEntity(request, apply, logisticsNum, freight, operateUserIp, now);
		WalletCardApplyManEntity man = walletCardApplyManDao.findByApplyId(apply.getId());
		if (man != null) {
			entity.setUserEmail(man.getUserEmail());
			entity.setUserName(man.getUserName());
			entity.setUserTel(man.getUserTel());
		}
		return entity;
	}

	private static String resolveShippingOrderNo(WalletCardApplyEntity apply) {
		if (!StringUtils.isEmpty(apply.getRequestOrderId())) {
			return apply.getRequestOrderId();
		}
		return "SP" + OrderCodeFactory.getOrderCode(apply.getWalletUid());
	}

	private WalletCardApplyEntity resolveLogisticsApply(List<WalletCardApplyEntity> applyList, Long applyId,
			Long ownerWalletUserId) {
		WalletCardApplyEntity target = null;
		if (applyId != null) {
			for (WalletCardApplyEntity row : applyList) {
				if (applyId.equals(row.getId())) {
					target = row;
					break;
				}
			}
			if (target == null) {
				target = walletCardApplyDao.selectById(applyId);
			}
		} else {
			target = applyList.get(0);
		}
		if (target == null) {
			return null;
		}
		if (ownerWalletUserId != null && !ownerWalletUserId.equals(target.getWalletUserId())) {
			return null;
		}
		return target;
	}

	private WalletLogisticsStateEnums resolveShippingStateFromEms(EmsTrackingInfoResp tracking) {
		if (tracking == null || tracking.getData() == null
				|| tracking.getData().getAccepted() == null
				|| tracking.getData().getAccepted().isEmpty()) {
			return null;
		}
		EmsTrackingInfoResp.AcceptedItem item = tracking.getData().getAccepted().get(0);
		if (item.getTrackInfo() == null || item.getTrackInfo().getLatestStatus() == null) {
			return null;
		}
		String state = item.getTrackInfo().getLatestStatus().getStatus();
		if (EmsTrackingConstants.EMS_IN_TRANSIT.equals(state)) {
			return WalletLogisticsStateEnums.INTRANSIT;
		}
		if (EmsTrackingConstants.EMS_OUT_FOR_DELIVERY.equals(state)) {
			return WalletLogisticsStateEnums.OUTFORDELIVERY;
		}
		if (EmsTrackingConstants.EMS_DELIVERED.equals(state)) {
			return WalletLogisticsStateEnums.DELIVERED;
		}
		return null;
	}

	private List<WalletLogisticsEventResp> extractLogisticsEvents(EmsTrackingInfoResp tracking,
			WalletCardApplyEntity apply) {
		List<WalletLogisticsEventResp> events = new ArrayList<>();
		if (tracking == null || tracking.getData() == null
				|| tracking.getData().getAccepted() == null
				|| tracking.getData().getAccepted().isEmpty()) {
			return events;
		}
		EmsTrackingInfoResp.TrackInfo trackInfo = tracking.getData().getAccepted().get(0).getTrackInfo();
		if (trackInfo == null || trackInfo.getTracking() == null
				|| trackInfo.getTracking().getProviders() == null
				|| trackInfo.getTracking().getProviders().isEmpty()) {
			return events;
		}
		List<EmsTrackingInfoResp.Event> rawEvents = trackInfo.getTracking().getProviders().get(0).getEvents();
		if (rawEvents == null) {
			return events;
		}
		for (EmsTrackingInfoResp.Event raw : rawEvents) {
			WalletLogisticsEventResp event = new WalletLogisticsEventResp();
			event.setDescription(raw.getDescription());
			event.setLocation(raw.getLocation());
			event.setTimeUtc(raw.getTimeUtc());
			event.setSubStatus(StringUtils.isEmpty(raw.getSubStatus())
					? apply.getShippingStateName() : raw.getSubStatus());
			events.add(event);
		}
		return events;
	}

	private void syncLogisticsState(List<WalletCardApplyEntity> applyList, String logisticsNum,
			WalletLogisticsStateEnums shippingState) {
		for (WalletCardApplyEntity row : applyList) {
			try {
				walletCardApplyDao.updateShipping(row.getId(), shippingState.getCode(), shippingState.getLabel(),
						logisticsNum, row.getShippingTime(), row.getLogisticsMonery());
				WalletCardShippingEntity shipping = walletCardShippingDao.findByApplyId(row.getId());
				if (shipping != null) {
					shipping.setLogisticsState(shippingState.getCode());
					shipping.setLogisticsStateName(shippingState.getLabel());
					shipping.setGmtModified(new Date());
					walletCardShippingDao.updateById(shipping);
				}
				WalletBankcardEntity card = walletBankcardDao.findByCardApplyId(row.getId());
				if (card != null) {
					walletBankcardDao.updateLogistics(card.getId(), shippingState.getCode(), logisticsNum);
				}
			} catch (Exception e) {
				log.error("sync logistics state failed applyId={}", row.getId(), e);
			}
		}
	}
}
