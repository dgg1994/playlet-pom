package com.playlet.internal.enums;

/**
 * 审核步骤状态；A/B 处理入参 action 亦复用 PASS(1)/REJECT(2)。
 */
public enum DramaAssetAuditStepStatusEnums {
	PENDING(0, "待审核"),
	PASS(1, "通过"),
	REJECT(2, "驳回");

	private final int code;
	private final String label;

	DramaAssetAuditStepStatusEnums(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static DramaAssetAuditStepStatusEnums fromCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (DramaAssetAuditStepStatusEnums item : values()) {
			if (item.code == code) {
				return item;
			}
		}
		return null;
	}

	/** 是否为处理动作（通过/驳回），不含待审。 */
	public static boolean isHandleAction(Integer code) {
		DramaAssetAuditStepStatusEnums item = fromCode(code);
		return item == PASS || item == REJECT;
	}

	public static boolean isPending(Integer code) {
		return fromCode(code) == PENDING;
	}

	public static boolean isPass(Integer code) {
		return fromCode(code) == PASS;
	}

	public static boolean isReject(Integer code) {
		return fromCode(code) == REJECT;
	}
}
