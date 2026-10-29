package com.playlet.internal.enums;

/**
 * 三方代扣签约协议状态（user_pay_agreement.status）。
 */
public enum PayAgreementStatusEnums {

	/** 待签约：已下单拉起签约，未收到签约结果 */
	WAIT(0, "待签约"),
	/** 已签约 */
	SIGNED(1, "已签约"),
	/** 已解约 */
	UNSIGNED(2, "已解约"),
	/** 失效：三方解约/风控关停 */
	INVALID(3, "失效");

	private final Integer index;
	private final String name;

	PayAgreementStatusEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static PayAgreementStatusEnums ofIndex(Integer index) {
		if (index == null) {
			return null;
		}
		for (PayAgreementStatusEnums item : values()) {
			if (item.index.equals(index)) {
				return item;
			}
		}
		return null;
	}

	/** 可用：仅已签约可用于代扣 */
	public static boolean isSigned(Integer index) {
		return SIGNED.equals(ofIndex(index));
	}
}
