package com.playlet.internal.service.support;

import com.playlet.internal.constants.CoinConstants;
import com.playlet.internal.dao.coin.CoinGlobalConfigDao;
import com.playlet.internal.dao.coin.CoinRateDailyDao;
import com.playlet.internal.entity.coin.CoinGlobalConfigEntity;
import com.playlet.internal.entity.coin.CoinRateDailyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 解析当前「多少币≈1元」展示汇率。
 */
@Service
public class CoinRateSupport {

	@Autowired
	private CoinGlobalConfigDao coinGlobalConfigDao;
	@Autowired
	private CoinRateDailyDao coinRateDailyDao;

	/**
	 * 返回当前生效的 coinPerYuan；无配置时返回 null。
	 */
	public BigDecimal resolveCoinPerYuan() {
		CoinGlobalConfigEntity cfg = coinGlobalConfigDao.findOne();
		if (cfg == null) {
			return null;
		}
		if (Integer.valueOf(CoinConstants.RATE_MODE_DAILY).equals(cfg.getRateMode())) {
			CoinRateDailyEntity daily = coinRateDailyDao.findLatest();
			if (daily != null && daily.getCoinPerYuan() != null && daily.getCoinPerYuan().compareTo(BigDecimal.ZERO) > 0) {
				return daily.getCoinPerYuan();
			}
		}
		return cfg.getManualCoinPerYuan();
	}

	public CoinGlobalConfigEntity requireConfig() {
		return coinGlobalConfigDao.findOne();
	}

	/**
	 * 按广告公式计算日汇率：pCoin = (adTotal * ecpm / 1000 * rRate) / coinTotal；
	 * coinPerYuan ≈ 1 / pCoin。
	 */
	public CoinRateDailyEntity computeDailyRate(long adTotal, BigDecimal ecpm, BigDecimal rRate, long coinTotal) {
		CoinRateDailyEntity row = new CoinRateDailyEntity();
		row.setAdTotal(adTotal);
		row.setEcpm(ecpm == null ? BigDecimal.ZERO : ecpm);
		row.setRRate(rRate == null ? BigDecimal.ZERO : rRate);
		row.setCoinTotal(coinTotal);
		if (coinTotal <= 0 || row.getEcpm().compareTo(BigDecimal.ZERO) <= 0
				|| row.getRRate().compareTo(BigDecimal.ZERO) <= 0 || adTotal <= 0) {
			row.setPCoin(BigDecimal.ZERO);
			row.setCoinPerYuan(BigDecimal.ZERO);
			return row;
		}
		// 广告收入(元) = 次数 * eCPM / 1000
		BigDecimal adIncome = BigDecimal.valueOf(adTotal)
				.multiply(row.getEcpm())
				.divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP)
				.multiply(row.getRRate());
		BigDecimal pCoin = adIncome.divide(BigDecimal.valueOf(coinTotal), 10, RoundingMode.HALF_UP);
		row.setPCoin(pCoin);
		if (pCoin.compareTo(BigDecimal.ZERO) <= 0) {
			row.setCoinPerYuan(BigDecimal.ZERO);
		} else {
			BigDecimal coinPerYuan = BigDecimal.ONE.divide(pCoin, 0, RoundingMode.HALF_UP);
			row.setCoinPerYuan(coinPerYuan);
		}
		return row;
	}
}
