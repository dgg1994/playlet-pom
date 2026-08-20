package com.playlet.internal.utils;

import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.constants.RankBoardGroupConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * 作家端通用：上海自然日、日期格式化、金币→元换算。
 */
public final class CreatorBizUtils {

	public static final ZoneId ZONE = ZoneId.of(RankBoardGroupConstants.TIMEZONE);

	private CreatorBizUtils() {
	}

	public static LocalDate today() {
		return LocalDate.now(ZONE);
	}

	public static String formatDate(LocalDate date) {
		if (date == null) {
			return null;
		}
		return date.format(RankBoardGroupConstants.DATE_FMT);
	}

	public static String formatDateTime(Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant().atZone(ZONE).toLocalDateTime().format(RankBoardGroupConstants.DATE_TIME_FMT);
	}

	/** 100 金币 = 1.00 元 */
	public static BigDecimal coinToYuan(Long coin) {
		long value = coin == null ? 0L : coin;
		return BigDecimal.valueOf(value)
				.divide(BigDecimal.valueOf(CreatorConstants.COIN_PER_YUAN), 2, RoundingMode.HALF_UP);
	}

	public static BigDecimal zeroYuan() {
		return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
	}
}
