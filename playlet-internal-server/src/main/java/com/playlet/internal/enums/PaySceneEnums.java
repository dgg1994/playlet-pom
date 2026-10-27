package com.playlet.internal.enums;

/**
 * 支付场景（端类型）：决定走三方哪个下单接口。
 */
public enum PaySceneEnums {

	/** 电脑端：微信 Native 扫码 / 支付宝 PC 网站支付 */
	PC("PC", "电脑端"),
	/** 手机浏览器：微信 H5 / 支付宝手机网站支付 */
	H5("H5", "手机网页"),
	/** App：微信 App 支付 / 支付宝 App 支付 */
	APP("APP", "APP"),
	/** 小程序、公众号：微信 JSAPI / 支付宝手机网站支付 */
	MINI("MINI", "小程序/公众号");

	private final String code;
	private final String name;

	PaySceneEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static PaySceneEnums ofCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String trimmed = code.trim();
		for (PaySceneEnums item : values()) {
			if (item.code.equalsIgnoreCase(trimmed)) {
				return item;
			}
		}
		return null;
	}

	public static boolean isValid(String code) {
		return ofCode(code) != null;
	}
}
