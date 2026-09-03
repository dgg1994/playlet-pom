package com.playlet.oversea.service.support;

import com.playlet.oversea.entity.welfare.WithdrawConfigEntity;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 提现金币按 withdraw_config 换算为 U 的金额计算。
 */
public final class WithdrawAmountCalculator {

	private static final int AMT_SCALE = 8;

	private WithdrawAmountCalculator() {
	}

	/**
	 * 按配置换算：毛额 = 积分 / points_per_unit，实到 = 毛额 - 手续费。
	 */
	public static Result calculate(int points, WithdrawConfigEntity cfg) {
		int rate = resolveRate(cfg);
		BigDecimal gross = BigDecimal.valueOf(points)
				.divide(BigDecimal.valueOf(rate), AMT_SCALE, RoundingMode.DOWN);
		BigDecimal fee = scale(cfg == null ? null : cfg.getServiceFee());
		BigDecimal actual = gross.subtract(fee).setScale(AMT_SCALE, RoundingMode.DOWN);
		return new Result(gross, fee, actual, rate);
	}

	private static int resolveRate(WithdrawConfigEntity cfg) {
		if (cfg == null || cfg.getPointsPerUnit() == null || cfg.getPointsPerUnit() <= 0) {
			return 1;
		}
		return cfg.getPointsPerUnit();
	}

	private static BigDecimal scale(BigDecimal value) {
		if (value == null) {
			return BigDecimal.ZERO.setScale(AMT_SCALE, RoundingMode.DOWN);
		}
		return value.setScale(AMT_SCALE, RoundingMode.DOWN);
	}

	@Getter
	public static final class Result {

		private final BigDecimal grossAmt;
		private final BigDecimal feeAmt;
		private final BigDecimal actualAmt;
		private final int pointsPerUnit;

		private Result(BigDecimal grossAmt, BigDecimal feeAmt, BigDecimal actualAmt, int pointsPerUnit) {
			this.grossAmt = grossAmt;
			this.feeAmt = feeAmt;
			this.actualAmt = actualAmt;
			this.pointsPerUnit = pointsPerUnit;
		}
	}
}
