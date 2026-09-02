package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.BankcardUpdateStatusRequest;
import com.playlet.internal.api.response.WalletBankcardAdminResp;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletBankcardDao;
import com.playlet.internal.dao.wallet.WalletCardProductDao;
import com.playlet.internal.dao.wallet.WalletCardTransactionDao;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import com.playlet.internal.entity.wallet.WalletCardTransactionEntity;
import com.playlet.internal.enums.WalletCardStatusEnums;
import com.playlet.internal.enums.WalletLogStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.wallet.WalletBankcardAdminQuery;
import com.playlet.internal.query.wallet.WalletCardTransactionAdminQuery;
import com.playlet.internal.service.AppUserCardManageService;
import com.playlet.internal.service.third.ThirdService;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端用户持卡与卡交易流水。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class AppUserCardManageServiceImpl implements AppUserCardManageService {

	@Autowired
	private WalletBankcardDao walletBankcardDao;
	@Autowired
	private WalletCardProductDao walletCardProductDao;
	@Autowired
	private WalletCardTransactionDao walletCardTransactionDao;
	@Autowired
	private ThirdService thirdService;

	@Override
	@SysLogAnnotation(module = "用户持卡", type = "POST", remark = "持卡列表")
	public ResponseBase pcFindUserCardList(@RequestBody(required = false) WalletBankcardAdminQuery query) {
		return findUserCardListInternal(query);
	}

	@Override
	@SysLogAnnotation(module = "用户持卡", type = "POST", remark = "用户持卡弹窗")
	public ResponseBase findUserCardList(@RequestBody(required = false) WalletBankcardAdminQuery query) {
		return findUserCardListInternal(query);
	}

	private ResponseBase findUserCardListInternal(WalletBankcardAdminQuery query) {
		if (query == null) {
			query = new WalletBankcardAdminQuery();
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<WalletBankcardAdminResp> list = walletBankcardDao.findAdminList(query);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (WalletBankcardAdminResp row : list) {
			if (row.getStatus() != null) {
				row.setStatusName(WalletCardStatusEnums.getLabelByCode(row.getStatus()));
			}
			if (row.getUid() != null) {
				row.setUid(String.valueOf(row.getUid()));
			}
			if (row.getCardId() != null) {
				WalletCardProductEntity product = walletCardProductDao.findById(row.getCardId());
				row.setCardData(product);
			}
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "用户持卡", type = "POST", remark = "卡交易流水")
	public ResponseBase pcFindTransaction(@RequestBody(required = false) WalletCardTransactionAdminQuery query) {
		if (query == null) {
			query = new WalletCardTransactionAdminQuery();
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<WalletCardTransactionEntity> list = walletCardTransactionDao.findPcList(query);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (WalletCardTransactionEntity row : list) {
			if (row.getOrderState() != null) {
				WalletLogStatusEnums state = WalletLogStatusEnums.fromIntCode(row.getOrderState());
				if (state != null) {
					row.setOrderStateName(state.getLabel());
				}
			}
			BigDecimal amt = row.getLocalCurrencyAmt() == null ? BigDecimal.ZERO : row.getLocalCurrencyAmt();
			BigDecimal fee = row.getHandlingFees() == null ? BigDecimal.ZERO : row.getHandlingFees();
			row.setTotalManey(amt.add(fee));
			row.setTransTypeLabel(row.getTransType() != null ? row.getTransType() : row.getBizType());
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "用户持卡", type = "GET", remark = "解冻卡")
	public ResponseBase unfreeze(Long id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		WalletBankcardEntity card = walletBankcardDao.selectById(id);
		if (card == null) {
			return setResultError(I18nUtil.getMessage("wallet.card_not_found"));
		}
		if (card.getCardStatus() == null
				|| card.getCardStatus() != WalletCardStatusEnums.FREEZE.getCode()) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 优先调三方解冻；失败则仅更新本地状态
		BankcardUpdateStatusRequest req = new BankcardUpdateStatusRequest();
		req.setUserBankcardId(card.getUserBankcardId());
		req.setEnable(true);
		try {
			thirdService.updateBankcardStatus(card.getWalletUid(), req);
		} catch (Exception e) {
			log.warn("admin unfreeze third skipped cardId={} userBankcardId={}", id, card.getUserBankcardId(), e);
		}
		try {
			walletBankcardDao.updateCardStatus(card.getId(),
					WalletCardStatusEnums.ACTIVE.getCode(), WalletCardStatusEnums.ACTIVE.getLabel());
		} catch (Exception e) {
			log.error("admin unfreeze update failed cardId={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("admin unfreeze success cardId={} userBankcardId={}", id, card.getUserBankcardId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}
}
