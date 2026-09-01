package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.BankcardApplyRequest;
import com.playlet.internal.api.request.WalletCardApplyRejectRequest;
import com.playlet.internal.api.request.WalletCardShippingRequest;
import com.playlet.internal.api.response.ThirdBankcardApplyResp;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletAccountDao;
import com.playlet.internal.dao.wallet.WalletBankcardDao;
import com.playlet.internal.dao.wallet.WalletCardApplyDao;
import com.playlet.internal.dao.wallet.WalletCardApplyKycDao;
import com.playlet.internal.dao.wallet.WalletCardApplyManDao;
import com.playlet.internal.dao.wallet.WalletCardApplySendDao;
import com.playlet.internal.dao.wallet.WalletCardProductDao;
import com.playlet.internal.dao.wallet.WalletUserDao;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import com.playlet.internal.entity.wallet.WalletCardApplyKycEntity;
import com.playlet.internal.entity.wallet.WalletCardApplyManEntity;
import com.playlet.internal.entity.wallet.WalletCardApplySendEntity;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.WalletCardApplyStateEnums;
import com.playlet.internal.enums.WalletCardStatusEnums;
import com.playlet.internal.enums.WalletKycStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.WalletCardApplyManageService;
import com.playlet.internal.service.support.WalletOpenCardSettlementService;
import com.playlet.internal.service.support.WalletPhysicalCardFulfillService;
import com.playlet.internal.service.third.ThirdService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
 * 管理端银行卡申请记录：列表 / 详情 / 审核开卡 / 拒绝。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WalletCardApplyManageServiceImpl implements WalletCardApplyManageService {

	@Autowired
	private WalletCardApplyDao walletCardApplyDao;
	@Autowired
	private WalletCardApplyManDao walletCardApplyManDao;
	@Autowired
	private WalletCardApplySendDao walletCardApplySendDao;
	@Autowired
	private WalletCardApplyKycDao walletCardApplyKycDao;
	@Autowired
	private WalletCardProductDao walletCardProductDao;
	@Autowired
	private WalletBankcardDao walletBankcardDao;
	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private ThirdService thirdService;
	@Autowired
	private WalletPhysicalCardFulfillService walletPhysicalCardFulfillService;
	@Autowired
	private WalletOpenCardSettlementService walletOpenCardSettlementService;
	@Autowired
	private IpUtil ipUtil;

	@Override
	@SysLogAnnotation(module = "银行卡申请记录", type = "POST", remark = "查询申请记录")
	public ResponseBase openCardApply(@RequestBody(required = false) WalletCardApplyEntity entity) {
		if (entity == null) {
			entity = new WalletCardApplyEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletCardApplyEntity> list = walletCardApplyDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (WalletCardApplyEntity row : list) {
			enrichApplyDetail(row);
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "银行卡申请记录", type = "GET", remark = "申请记录详情")
	public ResponseBase openCardApplyInfo(Long id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WalletCardApplyEntity entity = walletCardApplyDao.selectById(id);
		if (entity == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_not_found"));
		}
		enrichApplyDetail(entity);
		return setResultSuccess(entity, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "银行卡申请记录", type = "GET", remark = "开卡激活")
	public synchronized ResponseBase openCard(Long id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WalletCardApplyEntity apply = walletCardApplyDao.selectById(id);
		if (apply == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_not_found"));
		}
		// 仅虚拟卡支持管理端开卡（实体卡走发货流程）
		if (!WalletConstants.BANKCARD_NATURE_VIRTUAL.equalsIgnoreCase(apply.getCardType())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (apply.getApplyState() == null
				|| apply.getApplyState() != WalletCardApplyStateEnums.WAIT_ACTIVATION.getCode()) {
			return setResultError(I18nUtil.getMessage("card_open_state"));
		}
		if (apply.getKycState() == null
				|| apply.getKycState() != WalletKycStateEnums.SUCCESS_APPROVE.getCode()) {
			return setResultError(I18nUtil.getMessage("user_kyc_state"));
		}
		WalletBankcardEntity existedCard = walletBankcardDao.findByCardApplyId(apply.getId());
		if (existedCard != null) {
			return setResultError(I18nUtil.getMessage("wallet.card_already_exists"));
		}
		WalletCardProductEntity product = walletCardProductDao.findById(apply.getCardProductId());
		if (product == null) {
			return setResultError(I18nUtil.getMessage("bank_card_null"));
		}
		// 预存费须满足产品最小激活金额
		if (product.getActiveMinLimit() != null && apply.getPreSaveCost() != null) {
			BigDecimal minLimit = BigDecimal.valueOf(product.getActiveMinLimit());
			if (apply.getPreSaveCost().compareTo(minLimit) < 0) {
				return setResultError(I18nUtil.getMessage("topup_money_min"));
			}
		}
		WalletUserEntity user = walletUserDao.selectById(apply.getWalletUserId());
		if (user == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		BankcardApplyRequest thirdReq = new BankcardApplyRequest();
		thirdReq.setProductId(apply.getCardProductId());
		ThirdBankcardApplyResp third;
		try {
			third = thirdService.applyBankcard(user.getWalletUid(), thirdReq);
		} catch (BaseException e) {
			log.error("admin open card third failed applyId={} walletUid={}", id, user.getWalletUid(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("admin open card third error applyId={} walletUid={}", id, user.getWalletUid(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (third == null || third.getUserBankcardId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		Date now = new Date();
		insertAppliedBankcard(user, apply.getId(), product, third, now);
		WalletBankcardEntity card = walletBankcardDao.findByUserBankcardId(third.getUserBankcardId());
		walletAccountDao.markActivated(user.getId());
		apply.setApplyState(WalletCardApplyStateEnums.PROCESS_ACTIVATION.getCode());
		apply.setApplyStateName(WalletCardApplyStateEnums.PROCESS_ACTIVATION.getLabel());
		apply.setGmtModified(now);
		try {
			walletCardApplyDao.updateById(apply);
		} catch (Exception e) {
			log.error("admin open card update apply failed applyId={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		if (card != null) {
			walletOpenCardSettlementService.afterVirtualCardIssued(user, apply, card);
		}
		log.info("admin open card success applyId={} walletUid={} userBankcardId={}",
				id, user.getWalletUid(), third.getUserBankcardId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "银行卡申请记录", type = "POST", remark = "拒绝开卡申请")
	public ResponseBase reject(@RequestBody WalletCardApplyRejectRequest entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		if (!StringUtils.hasText(entity.getRejectInfo())) {
			return setResultError(I18nUtil.getMessage("wallet.apply_reject_reason_required"));
		}
		WalletCardApplyEntity apply = walletCardApplyDao.selectById(entity.getId());
		if (apply == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_not_found"));
		}
		if (apply.getApplyState() == null
				|| apply.getApplyState() != WalletCardApplyStateEnums.WAIT_ACTIVATION.getCode()) {
			return setResultError(I18nUtil.getMessage("wallet.apply_state_invalid"));
		}
		Date now = new Date();
		apply.setApplyState(WalletCardApplyStateEnums.ERROR_ACTIVATION.getCode());
		apply.setApplyStateName(WalletCardApplyStateEnums.ERROR_ACTIVATION.getLabel());
		apply.setRejectInfo(entity.getRejectInfo().trim());
		apply.setGmtModified(now);
		try {
			walletCardApplyDao.updateById(apply);
		} catch (Exception e) {
			log.error("admin reject apply update failed applyId={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		// 解冻开卡冻结金额
		walletOpenCardSettlementService.unfreezeApplyTotal(apply);
		log.info("admin reject apply success applyId={} walletUserId={}", entity.getId(), apply.getWalletUserId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "银行卡申请记录", type = "GET", remark = "实体卡分配激活")
	public ResponseBase cardBinding(Long applyId, String cardNumber, String pinNum) {
		return walletPhysicalCardFulfillService.cardBinding(applyId, cardNumber, pinNum);
	}

	@Override
	@SysLogAnnotation(module = "银行卡申请记录", type = "POST", remark = "实体卡发货")
	public ResponseBase shipping(@RequestBody WalletCardShippingRequest entity, HttpServletRequest request) {
		String clientIp = request == null ? null : ipUtil.getClientIp(request);
		return walletPhysicalCardFulfillService.shipping(entity, clientIp);
	}

	@Override
	@SysLogAnnotation(module = "银行卡申请记录", type = "GET", remark = "修改物流单号")
	public ResponseBase upLogisticsNum(Long applyId, String logisticsNum) {
		return walletPhysicalCardFulfillService.upLogisticsNum(applyId, logisticsNum);
	}

	@Override
	@SysLogAnnotation(module = "银行卡申请记录", type = "GET", remark = "查询物流跟踪")
	public ResponseBase findLogistics(String logisticsNum, Long applyId) {
		return walletPhysicalCardFulfillService.findLogistics(logisticsNum, applyId, null);
	}

	/** 补齐列表/详情关联数据 */
	private void enrichApplyDetail(WalletCardApplyEntity entity) {
		if (entity == null || entity.getId() == null) {
			return;
		}
		Long applyId = entity.getId();
		WalletCardApplyManEntity man = walletCardApplyManDao.findByApplyId(applyId);
		entity.setApplyManData(man);
		WalletCardApplySendEntity send = walletCardApplySendDao.findByApplyId(applyId);
		entity.setApplySendData(send);
		if (entity.getCardProductId() != null) {
			WalletCardProductEntity product = walletCardProductDao.findById(entity.getCardProductId());
			entity.setCardData(product);
		}
		WalletCardApplyKycEntity kyc = walletCardApplyKycDao.findByApplyId(applyId);
		entity.setKycData(kyc);
		WalletBankcardEntity userCard = walletBankcardDao.findByCardApplyId(applyId);
		entity.setUserCardData(userCard);
	}

	/** 三方开卡成功后写入本地卡记录 */
	private void insertAppliedBankcard(WalletUserEntity user, Long applyId, WalletCardProductEntity product,
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
		card.setCardStatus(WalletCardStatusEnums.WAIT_ACTIVE.getCode());
		card.setCardStatusName(WalletCardStatusEnums.WAIT_ACTIVE.getLabel());
		card.setBalance(BigDecimal.ZERO);
		card.setPinSet(0);
		card.setIsDefault(WalletConstants.CARD_DEFAULT_NO);
		card.setApplyOrderNo(third.getOrderNo());
		card.setSetTime(now);
		card.setGmtModified(now);
		if (walletBankcardDao.findDefaultByWalletUserId(user.getId()) == null) {
			card.setIsDefault(WalletConstants.CARD_DEFAULT_YES);
		}
		try {
			walletBankcardDao.insert(card);
		} catch (DuplicateKeyException e) {
			log.warn("admin open card bankcard duplicate userBankcardId={}", third.getUserBankcardId(), e);
		} catch (Exception e) {
			log.error("admin open card bankcard insert failed userBankcardId={}", third.getUserBankcardId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}
}
