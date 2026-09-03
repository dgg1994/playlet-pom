package com.playlet.oversea.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 年龄校验（对齐 worldpay AgeCheckUtil）。
 */
public final class AgeCheckUtil {

	private static final DateTimeFormatter BIRTHDAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final int ADULT_AGE = 18;

	private AgeCheckUtil() {
	}

	public static boolean isAdult(String birthdayStr) {
		if (StringUtils.isEmpty(birthdayStr)) {
			return false;
		}
		try {
			LocalDate birthday = LocalDate.parse(birthdayStr.trim(), BIRTHDAY_FORMAT);
			Period period = Period.between(birthday, LocalDate.now());
			return period.getYears() >= ADULT_AGE;
		} catch (DateTimeParseException e) {
			return false;
		}
	}
}
