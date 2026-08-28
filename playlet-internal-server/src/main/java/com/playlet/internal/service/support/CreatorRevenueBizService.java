package com.playlet.internal.service.support;

import com.playlet.internal.api.response.CreatorRevenueSummaryRespEntity;
import com.playlet.internal.api.response.CreatorRevenueTrendAggRow;
import com.playlet.internal.api.response.CreatorRevenueTrendItemRespEntity;
import com.playlet.internal.api.response.CreatorSettlementAccountRespEntity;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.creator.CreatorCoinLedgerDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorCoinLedgerEntity;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.utils.CreatorBizUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作家收益概览：今日/累计/待结算 + U 卡结算账户。
 */
@Service
public class CreatorRevenueBizService {

	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private CreatorCoinLedgerDao creatorCoinLedgerDao;
	@Autowired
	private WithdrawWalletSupport withdrawWalletSupport;

	public CreatorRevenueSummaryRespEntity buildSummary(Integer creatorId) {
		CreatorRevenueSummaryRespEntity resp = new CreatorRevenueSummaryRespEntity();
		CreatorAccountEntity account = creatorAccountDao.selectById(creatorId);
		if (account == null) {
			resp.setTodayIncomeCoin(0L);
			resp.setTotalIncomeCoin(0L);
			resp.setPendingSettleCoin(0L);
			resp.setSettlementAccount(buildSettlementAccount(creatorId));
			resp.setIncomeTrend(buildIncomeTrend(creatorId));
			return resp;
		}
		LocalDate today = CreatorBizUtils.today();
		String todayStr = CreatorBizUtils.formatDate(today);
		Long todayIncomeCoin = creatorCoinLedgerDao.sumPositiveIncomeByDate(creatorId, todayStr);
		// 收益概览直接返回金币，不做元换算
		resp.setTodayIncomeCoin(todayIncomeCoin == null ? 0L : todayIncomeCoin);
		resp.setTotalIncomeCoin(account.getTotalIncomeCoin() == null ? 0L : account.getTotalIncomeCoin());
		// 待结算日结未上线前固定 0；后续接 pending_settle_coin
		resp.setPendingSettleCoin(0L);
		resp.setSettlementAccount(buildSettlementAccount(creatorId));
		resp.setIncomeTrend(buildIncomeTrend(creatorId));
		return resp;
	}

	/** 作家资金流水分页：直接返回 creator_coin_ledger 记录。 */
	public PageInfo<CreatorCoinLedgerEntity> fundRecords(Integer creatorId, PageQueryHelperEntity page) {
		PageQueryHelperEntity queryPage = page == null ? new PageQueryHelperEntity() : page;
		PageHelper.startPage(queryPage.getPageNumber(), queryPage.getPageSize());
		List<CreatorCoinLedgerEntity> rows = creatorCoinLedgerDao.findByCreatorId(creatorId);
		if (rows == null) {
			rows = new ArrayList<>();
		}
		return new PageInfo<>(rows);
	}

	/** 近 7 日（含今天）收益趋势：按上海自然日聚合，缺日补 0 */
	private List<CreatorRevenueTrendItemRespEntity> buildIncomeTrend(Integer creatorId) {
		LocalDate today = CreatorBizUtils.today();
		int trendDays = CreatorConstants.REVENUE_TREND_DAYS;
		LocalDate fromDate = today.minusDays(trendDays - 1L);
		String fromStr = CreatorBizUtils.formatDate(fromDate);
		String toStr = CreatorBizUtils.formatDate(today);
		Map<String, Long> coinByDate = new HashMap<>();
		if (creatorId != null) {
			List<CreatorRevenueTrendAggRow> rows = creatorCoinLedgerDao.sumPositiveIncomeGroupByDate(
					creatorId, fromStr, toStr);
			if (rows != null) {
				for (CreatorRevenueTrendAggRow row : rows) {
					if (row == null || row.getBizDate() == null) {
						continue;
					}
					coinByDate.put(row.getBizDate(), row.getIncomeCoin() == null ? 0L : row.getIncomeCoin());
				}
			}
		}
		List<CreatorRevenueTrendItemRespEntity> trend = new ArrayList<>(trendDays);
		for (int i = 0; i < trendDays; i++) {
			LocalDate day = fromDate.plusDays(i);
			String dateStr = CreatorBizUtils.formatDate(day);
			CreatorRevenueTrendItemRespEntity item = new CreatorRevenueTrendItemRespEntity();
			item.setDate(dateStr);
			item.setIncomeCoin(coinByDate.getOrDefault(dateStr, 0L));
			trend.add(item);
		}
		return trend;
	}

	private CreatorSettlementAccountRespEntity buildSettlementAccount(Integer creatorId) {
		CreatorSettlementAccountRespEntity account = new CreatorSettlementAccountRespEntity();
		account.setWalletWithdrawReady(0);
		if (creatorId == null) {
			return account;
		}
		WithdrawWalletSnapshot snap = new WithdrawWalletSnapshot();
		withdrawWalletSupport.enrich(WithdrawUserTypeEnums.CREATOR.getCode(), creatorId, snap);
		account.setWalletWithdrawReady(snap.getWalletWithdrawReady() == null ? 0 : snap.getWalletWithdrawReady());
		account.setDefaultCardMasked(snap.getPayoutTargetMasked());
		return account;
	}
}
