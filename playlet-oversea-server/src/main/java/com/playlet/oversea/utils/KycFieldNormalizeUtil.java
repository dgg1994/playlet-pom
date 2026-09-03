package com.playlet.oversea.utils;

import com.playlet.oversea.api.request.KycApplyRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * KYC 手机号 / 国家码规范化（对齐 worldPay 三方校验）。
 */
public final class KycFieldNormalizeUtil {

	private static final Map<String, String> ALPHA2_TO_ALPHA3;

	static {
		Map<String, String> map = new HashMap<>();
		map.put("HK", "HKG");
		map.put("CN", "CHN");
		map.put("SG", "SGP");
		map.put("US", "USA");
		map.put("GB", "GBR");
		map.put("TW", "TWN");
		map.put("MO", "MAC");
		map.put("JP", "JPN");
		map.put("KR", "KOR");
		map.put("MY", "MYS");
		map.put("TH", "THA");
		map.put("PH", "PHL");
		map.put("VN", "VNM");
		map.put("ID", "IDN");
		map.put("AU", "AUS");
		map.put("CA", "CAN");
		ALPHA2_TO_ALPHA3 = Collections.unmodifiableMap(map);
	}

	private KycFieldNormalizeUtil() {
	}

	/**
	 * 手机区号：去掉 + / 空格，仅保留数字（如 +852 → 852）。
	 */
	public static String normalizeAreaCode(String areaCode) {
		if (StringUtils.isEmpty(areaCode)) {
			return areaCode;
		}
		String digits = areaCode.trim().replaceAll("[^0-9]", "");
		if (digits.startsWith("00") && digits.length() > 2) {
			return digits.substring(2);
		}
		return digits;
	}

	/**
	 * 国家码：转为 ISO Alpha-3（如 HK → HKG）；已是三位则转大写。
	 */
	public static String normalizeNationCode(String nationCode) {
		if (StringUtils.isEmpty(nationCode)) {
			return nationCode;
		}
		String code = nationCode.trim().toUpperCase();
		if (code.length() == 2) {
			String alpha3 = ALPHA2_TO_ALPHA3.get(code);
			return alpha3 == null ? code : alpha3;
		}
		return code;
	}

	/**
	 * 手机号：仅保留数字；若误带区号前缀则剥离。
	 */
	public static String normalizePhone(String phone, String areaCode) {
		if (StringUtils.isEmpty(phone)) {
			return phone;
		}
		String digits = phone.trim().replaceAll("[^0-9]", "");
		String dial = normalizeAreaCode(areaCode);
		if (!StringUtils.isEmpty(dial) && digits.startsWith(dial) && digits.length() > dial.length()) {
			return digits.substring(dial.length());
		}
		return digits;
	}

	/** 规范化 KYC 提交体中的区号 / 国家码 / 手机号 */
	public static void normalizeKycApply(KycApplyRequest req) {
		if (req == null) {
			return;
		}
		String areaCode = normalizeAreaCode(req.getAreaCode());
		req.setAreaCode(areaCode);
		req.setNationCode(normalizeNationCode(req.getNationCode()));
		req.setCountryCode(normalizeNationCode(req.getCountryCode()));
		req.setPhone(normalizePhone(req.getPhone(), areaCode));
	}

	/** 持卡人区号 / 国家码 / 手机号入库前规范化 */
	public static void normalizeHolderTel(String userTelDialCode, String userTelCode, String userTel,
			HolderTelConsumer consumer) {
		if (consumer == null) {
			return;
		}
		String areaCode = normalizeAreaCode(userTelDialCode);
		consumer.accept(areaCode, normalizeNationCode(userTelCode), normalizePhone(userTel, areaCode));
	}

	@FunctionalInterface
	public interface HolderTelConsumer {
		void accept(String userTelDialCode, String userTelCode, String userTel);
	}
}
