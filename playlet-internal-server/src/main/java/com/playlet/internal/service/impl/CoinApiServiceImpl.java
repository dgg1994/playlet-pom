package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.request.CoinLedgerQueryEntity;
import com.playlet.internal.api.response.CoinBalanceRespEntity;
import com.playlet.internal.api.response.CoinLedgerItemEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.coin.CoinGlobalConfigDao;
import com.playlet.internal.dao.welfare.UserCoinLedgerDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.coin.CoinGlobalConfigEntity;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.CoinApiService;
import com.playlet.internal.service.support.CoinRateSupport;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * C 端金币：余额、流水、汇率。
 */
@Slf4j
@RestController
@CrossOrigin
public class CoinApiServiceImpl implements CoinApiService {

	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;
	@Autowired
	private CoinGlobalConfigDao coinGlobalConfigDao;
	@Autowired
	private CoinRateSupport coinRateSupport;

	@Override
	public ResponseBase balance(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		long balance = account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		long frozen = account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
		CoinBalanceRespEntity resp = new CoinBalanceRespEntity();
		resp.setCoinBalance(balance);
		resp.setFrozenCoinBalance(frozen);
		resp.setAvailableCoin(Math.max(balance - frozen, 0L));
		resp.setCoinPerYuan(coinRateSupport.resolveCoinPerYuan());
		CoinGlobalConfigEntity cfg = coinGlobalConfigDao.findOne();
		resp.setRateMode(cfg == null ? null : cfg.getRateMode());
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase ledger(HttpServletRequest request, @RequestBody(required = false) CoinLedgerQueryEntity query) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		PageQueryHelperEntity page = query == null ? new PageQueryHelperEntity() : query;
		String bizType = query == null || StringUtils.isEmpty(query.getBizType())
				? null : query.getBizType().trim();
		PageHelper.startPage(page.getPageNumber(), page.getPageSize());
		List<UserCoinLedgerEntity> rows = userCoinLedgerDao.findByUidAndBizType(uid, bizType);
		List<CoinLedgerItemEntity> items = new ArrayList<>();
		if (rows != null) {
			for (UserCoinLedgerEntity row : rows) {
				items.add(toLedgerItem(row));
			}
		}
		PageInfo<UserCoinLedgerEntity> rawPage = new PageInfo<>(rows == null ? new ArrayList<>() : rows);
		PageInfo<CoinLedgerItemEntity> info = new PageInfo<>();
		info.setList(items);
		info.setPageNum(rawPage.getPageNum());
		info.setPageSize(rawPage.getPageSize());
		info.setTotal(rawPage.getTotal());
		info.setPages(rawPage.getPages());
		return setResultSuccess(info, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase rate() {
		Map<String, Object> data = new HashMap<>(4);
		CoinGlobalConfigEntity cfg = coinGlobalConfigDao.findOne();
		data.put("coinPerYuan", coinRateSupport.resolveCoinPerYuan());
		data.put("rateMode", cfg == null ? null : cfg.getRateMode());
		data.put("manualCoinPerYuan", cfg == null ? null : cfg.getManualCoinPerYuan());
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	private static CoinLedgerItemEntity toLedgerItem(UserCoinLedgerEntity row) {
		CoinLedgerItemEntity item = new CoinLedgerItemEntity();
		item.setId(row.getId());
		item.setChangeAmt(row.getChangeAmt());
		item.setBalanceAfter(row.getBalanceAfter());
		item.setBizType(row.getBizType());
		item.setBizTypeLabel(CoinBizTypeEnums.getLableByName(row.getBizType()));
		item.setBizId(row.getBizId());
		item.setTaskCode(row.getTaskCode());
		item.setAdBoostFlag(row.getAdBoostFlag());
		item.setRemark(row.getRemark());
		item.setSetTime(row.getSetTime());
		return item;
	}
}
