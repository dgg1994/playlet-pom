package com.playlet.internal.service.creator.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playlet.internal.api.response.CreatorWalletFlowResp;
import com.playlet.internal.api.response.CreatorWalletOverviewResp;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.creator.CreatorCoinLedgerDao;
import com.playlet.internal.dao.creator.CreatorWithdrawApplyDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorCoinLedgerEntity;
import com.playlet.internal.entity.creator.CreatorWithdrawApplyEntity;
import com.playlet.internal.enums.CreatorCoinBizTypeEnums;
import com.playlet.internal.enums.CreatorWithdrawStatusEnums;
import com.playlet.internal.query.creator.CreatorWalletApplyQuery;
import com.playlet.internal.query.creator.CreatorWalletQuery;
import com.playlet.internal.service.creator.CreatorWalletService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 我的钱包实现。
 * <p>金额口径：creator_account.coin_balance 为账户余额，frozen_coin_balance 为提现冻结（余额的子集），
 * 可提现金额 = 余额 - 冻结。</p>
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorWalletServiceImpl extends BaseApiService implements CreatorWalletService {

	/** 单笔最低提现（金币） */
	private static final long MIN_WITHDRAW_AMOUNT = 100L;
	/** 冻结 DAO 为 int 入参，限制上限防溢出 */
	private static final long MAX_WITHDRAW_AMOUNT = 10000000L;

	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private CreatorCoinLedgerDao creatorCoinLedgerDao;
	@Autowired
	private CreatorWithdrawApplyDao creatorWithdrawApplyDao;

	@Override
	public ResponseBase overview(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		long balance = nvl(account.getCoinBalance());
		long frozen = nvl(account.getFrozenCoinBalance());
		CreatorWalletOverviewResp resp = new CreatorWalletOverviewResp();
		resp.setAccountBalance(balance);
		resp.setAvailableWithdraw(Math.max(0L, balance - frozen));
		// 一期无「结算中」资金字段，暂返回 0
		resp.setPendingSettle(0L);
		resp.setTotalIncome(nvl(account.getTotalIncomeCoin()));
		resp.setMinWithdrawAmount(MIN_WITHDRAW_AMOUNT);
		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase ledgerList(@RequestBody CreatorWalletQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorWalletQuery q = query == null ? new CreatorWalletQuery() : query;
		int pageNumber = q.getPageNumber() == null ? 1 : q.getPageNumber();
		int pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
		List<String> bizTypes = CreatorCoinBizTypeEnums.bizTypesOfCategory(q.getType());

		LambdaQueryWrapper<CreatorCoinLedgerEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(CreatorCoinLedgerEntity::getCreatorId, account.getId());
		if (bizTypes != null) {
			wrapper.in(CreatorCoinLedgerEntity::getBizType, bizTypes);
		}
		wrapper.orderByDesc(CreatorCoinLedgerEntity::getSetTime).orderByDesc(CreatorCoinLedgerEntity::getId);
		IPage<CreatorCoinLedgerEntity> result = creatorCoinLedgerDao.selectPage(new Page<>(pageNumber, pageSize), wrapper);

		Map<Integer, CreatorWithdrawApplyEntity> withdrawMap = batchQueryWithdraw(result.getRecords());
		List<CreatorWalletFlowResp> list = new ArrayList<>();
		for (CreatorCoinLedgerEntity row : result.getRecords()) {
			list.add(toFlow(row, withdrawMap));
		}
		return setResultSuccess(pageData(list, result.getTotal(), pageNumber, pageSize));
	}

	@Override
	public ResponseBase withdrawList(@RequestBody CreatorWalletQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorWalletQuery q = query == null ? new CreatorWalletQuery() : query;
		int pageNumber = q.getPageNumber() == null ? 1 : q.getPageNumber();
		int pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
		Integer status = CreatorWithdrawStatusEnums.codeOfLabel(q.getStatus());

		LambdaQueryWrapper<CreatorWithdrawApplyEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(CreatorWithdrawApplyEntity::getCreatorId, account.getId());
		if (status != null) {
			wrapper.eq(CreatorWithdrawApplyEntity::getStatus, status);
		}
		wrapper.orderByDesc(CreatorWithdrawApplyEntity::getSetTime).orderByDesc(CreatorWithdrawApplyEntity::getId);
		IPage<CreatorWithdrawApplyEntity> result = creatorWithdrawApplyDao.selectPage(new Page<>(pageNumber, pageSize), wrapper);

		List<CreatorWalletFlowResp> list = new ArrayList<>();
		for (CreatorWithdrawApplyEntity row : result.getRecords()) {
			list.add(toWithdrawFlow(row));
		}
		return setResultSuccess(pageData(list, result.getTotal(), pageNumber, pageSize));
	}

	@Override
	public ResponseBase applyWithdraw(@RequestBody CreatorWalletApplyQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query == null || query.getAmount() == null || query.getAmount() <= 0) {
			return setResultError("请输入提现金额");
		}
		long amount = query.getAmount();
		if (amount < MIN_WITHDRAW_AMOUNT) {
			return setResultError("单笔最低提现 " + MIN_WITHDRAW_AMOUNT + " 元");
		}
		if (amount > MAX_WITHDRAW_AMOUNT) {
			return setResultError("单笔提现金额过大，请分笔提现");
		}
		long balance = nvl(account.getCoinBalance());
		long frozen = nvl(account.getFrozenCoinBalance());
		long available = Math.max(0L, balance - frozen);
		if (amount > available) {
			return setResultError("可提现金额不足");
		}

		Date now = new Date();
		String bankName = isBlank(query.getBankName()) ? "结算银行卡" : query.getBankName().trim();
		String tail = isBlank(query.getBankCardTail()) ? "" : query.getBankCardTail().trim();

		// 1) 提现申请单
		CreatorWithdrawApplyEntity apply = new CreatorWithdrawApplyEntity();
		apply.setCreatorId(account.getId());
		apply.setApplyNo(buildApplyNo(now));
		apply.setAmount(amount);
		apply.setFee(0L);
		apply.setBankName(bankName);
		apply.setBankCardTail(tail);
		apply.setStatus(CreatorWithdrawStatusEnums.PROCESSING.getCode());
		apply.setRemark("");
		apply.setSetTime(now);
		apply.setGmtModified(now);
		creatorWithdrawApplyDao.insert(apply);

		// 2) 冻结（SQL 内二次校验，防并发超提；失败则抛异常回滚申请单）
		int rows = creatorAccountDao.freezeCoinBalance(account.getId(), (int) amount);
		if (rows == 0) {
			throw new IllegalStateException("可提现金额不足");
		}

		// 3) 冻结流水（余额未扣减，仅冻结增加；成功/失败由管理端处理）
		CreatorCoinLedgerEntity ledger = new CreatorCoinLedgerEntity();
		ledger.setCreatorId(account.getId());
		ledger.setChangeAmt(-amount);
		ledger.setBalanceBefore(balance);
		ledger.setBalanceAfter(balance);
		ledger.setFrozenBefore(frozen);
		ledger.setFrozenAfter(frozen + amount);
		ledger.setBizType(CreatorCoinBizTypeEnums.WITHDRAW.getBizType());
		ledger.setBizId(String.valueOf(apply.getId()));
		ledger.setRemark("提现至" + bankName + (tail.isEmpty() ? "" : "尾号" + tail));
		ledger.setSetTime(now);
		ledger.setGmtModified(now);
		creatorCoinLedgerDao.insert(ledger);

		log.info("creator withdraw apply creatorId={} applyId={} amount={}", account.getId(), apply.getId(), amount);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase ledgerCategories() {
		return setResultSuccess(CreatorCoinBizTypeEnums.categories());
	}

	/** 批量取提现单，用于流水列表回填真实提现状态 */
	private Map<Integer, CreatorWithdrawApplyEntity> batchQueryWithdraw(List<CreatorCoinLedgerEntity> rows) {
		List<Integer> ids = new ArrayList<>();
		for (CreatorCoinLedgerEntity row : rows) {
			if (CreatorCoinBizTypeEnums.WITHDRAW.getBizType().equalsIgnoreCase(row.getBizType())) {
				Integer id = parseId(row.getBizId());
				if (id != null) {
					ids.add(id);
				}
			}
		}
		Map<Integer, CreatorWithdrawApplyEntity> map = new HashMap<>();
		if (ids.isEmpty()) {
			return map;
		}
		List<CreatorWithdrawApplyEntity> applies = creatorWithdrawApplyDao.findByIds(ids);
		if (applies != null) {
			for (CreatorWithdrawApplyEntity item : applies) {
				map.put(item.getId(), item);
			}
		}
		return map;
	}

	private CreatorWalletFlowResp toFlow(CreatorCoinLedgerEntity row, Map<Integer, CreatorWithdrawApplyEntity> withdrawMap) {
		CreatorWalletFlowResp resp = new CreatorWalletFlowResp();
		resp.setId(row.getId());
		resp.setTime(formatTime(row.getSetTime()));
		resp.setTitle(CreatorCoinBizTypeEnums.titleOf(row.getBizType()));
		resp.setDesc(row.getRemark() == null ? "" : row.getRemark());
		long change = row.getChangeAmt() == null ? 0L : row.getChangeAmt();
		resp.setAmount(Math.abs(change));
		resp.setSign(change >= 0 ? "+" : "-");
		resp.setCategory(CreatorCoinBizTypeEnums.categoryOf(row.getBizType(), change));
		if (CreatorCoinBizTypeEnums.WITHDRAW.getBizType().equalsIgnoreCase(row.getBizType())) {
			CreatorWithdrawApplyEntity apply = withdrawMap.get(parseId(row.getBizId()));
			Integer status = apply == null ? CreatorWithdrawStatusEnums.PROCESSING.getCode() : apply.getStatus();
			resp.setStatusCode(status);
			resp.setStatusText(CreatorWithdrawStatusEnums.getLabel(status));
		} else {
			resp.setStatusText(change >= 0 ? "已到账" : "已完成");
		}
		return resp;
	}

	private CreatorWalletFlowResp toWithdrawFlow(CreatorWithdrawApplyEntity row) {
		CreatorWalletFlowResp resp = new CreatorWalletFlowResp();
		resp.setId(row.getId() == null ? null : row.getId().longValue());
		resp.setTime(formatTime(row.getSetTime()));
		String bankName = isBlank(row.getBankName()) ? "结算银行卡" : row.getBankName();
		resp.setTitle("提现至" + bankName);
		String fee = "手续费 ¥0.00";
		resp.setDesc(isBlank(row.getBankCardTail()) ? fee : "尾号 " + row.getBankCardTail() + " · " + fee);
		long amount = row.getAmount() == null ? 0L : row.getAmount();
		resp.setAmount(amount);
		resp.setSign("-");
		resp.setCategory("提现");
		resp.setStatusCode(row.getStatus());
		resp.setStatusText(CreatorWithdrawStatusEnums.getLabel(row.getStatus()));
		return resp;
	}

	private static Map<String, Object> pageData(List<CreatorWalletFlowResp> list, long total, int pageNumber, int pageSize) {
		Map<String, Object> data = new HashMap<>();
		data.put("list", list);
		data.put("total", total);
		data.put("pageNumber", pageNumber);
		data.put("pageSize", pageSize);
		return data;
	}

	private static String buildApplyNo(Date time) {
		return "TX" + new SimpleDateFormat("yyyyMMddHHmmss").format(time)
				+ String.format("%03d", (int) (System.currentTimeMillis() % 1000));
	}

	private static Integer parseId(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String formatTime(Date time) {
		return time == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private static long nvl(Long value) {
		return value == null ? 0L : value;
	}
}
