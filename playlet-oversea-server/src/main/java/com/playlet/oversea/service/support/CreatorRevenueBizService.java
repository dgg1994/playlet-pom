package com.playlet.oversea.service.support;

import com.playlet.oversea.api.response.CreatorRevenueSummaryRespEntity;
import com.playlet.oversea.api.response.CreatorRevenueTrendAggRow;
import com.playlet.oversea.api.response.CreatorRevenueTrendItemRespEntity;
import com.playlet.oversea.api.response.CreatorSettlementAccountRespEntity;
import com.playlet.oversea.constants.CreatorConstants;
import com.playlet.oversea.dao.creator.CreatorAccountDao;
import com.playlet.oversea.dao.creator.CreatorCoinLedgerDao;
import com.playlet.oversea.dao.creator.CreatorProfileDao;
import com.playlet.oversea.entity.creator.CreatorAccountEntity;
import com.playlet.oversea.entity.creator.CreatorCoinLedgerEntity;
import com.playlet.oversea.entity.creator.CreatorProfileEntity;
import com.playlet.oversea.enums.OnePayBindStatusEnums;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import com.playlet.oversea.utils.CreatorBizUtils;
import com.playlet.oversea.utils.StringUtils;
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
 * 作家收益概览：今日/累计/待结算 + OnePay 结算账户。
 */
@Service
public class CreatorRevenueBizService {

	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private CreatorCoinLedgerDao creatorCoinLedgerDao;
	@Autowired
	private CreatorProfileDao creatorProfileDao;

	public CreatorRevenueSummaryRespEntity buildSummary(Integer creatorId) {
		CreatorRevenueSummaryRespEntity resp = new CreatorRevenueSummaryRespEntity();
		CreatorAccountEntity account = creatorAccountDao.selectById(creatorId);
		if (account == null) {
			resp.setTodayIncomeCoin(0L);
			resp.setTotalIncomeCoin(0L);
			resp.setPendingSettleCoin(0L);
			resp.setSettlementAccount(buildSettlementAccount(null));
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
		resp.setSettlementAccount(buildSettlementAccount(creatorProfileDao.findByCreatorId(creatorId)));
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

	private CreatorSettlementAccountRespEntity buildSettlementAccount(CreatorProfileEntity profile) {
		CreatorSettlementAccountRespEntity account = new CreatorSettlementAccountRespEntity();
		if (profile == null) {
			account.setBindStatus(OnePayBindStatusEnums.UNBOUND.getCode());
			return account;
		}
		Integer bindStatus = profile.getOnepayBindStatus();
		account.setBindStatus(bindStatus == null ? OnePayBindStatusEnums.UNBOUND.getCode() : bindStatus);
		boolean bound = account.getBindStatus() != null
				&& account.getBindStatus() == OnePayBindStatusEnums.BOUND.getCode()
				&& StringUtils.isNotEmpty(profile.getOnepayAccount());
		if (bound) {
			// 收益页直接返回完整 OnePay 账号，不做脱敏
			account.setOnepayAccountMasked(profile.getOnepayAccount().trim());
			account.setBindTime(CreatorBizUtils.formatDateTime(profile.getOnepayBindTime()));
		}
		return account;
	}
}
