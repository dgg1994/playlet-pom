package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 签到阶梯循环模式（对应 sign_in_global_config.cycle_mode）
 */
public enum SignInCycleModeEnums {

	CAP("CAP", "封顶"),
	LOOP("LOOP", "循环");

	private final String code;
	private final String lable;

	SignInCycleModeEnums(String code, String lable) {
		this.code = code;
		this.lable = lable;
	}

	public String getCode() {
		return code;
	}

	public String getLable() {
		return lable;
	}

	public static SignInCycleModeEnums fromCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String normalized = code.trim();
		for (SignInCycleModeEnums mode : values()) {
			if (mode.code.equalsIgnoreCase(normalized)) {
				return mode;
			}
		}
		return null;
	}

	public static boolean isValid(String code) {
		return fromCode(code) != null;
	}

	public static SignInCycleModeEnums defaultMode() {
		return CAP;
	}

	public static List<DicEntity> getList() {
		List<DicEntity> list = new ArrayList<>();
		for (SignInCycleModeEnums mode : values()) {
			DicEntity dic = new DicEntity();
			dic.setName(mode.code);
			dic.setLable(mode.lable);
			list.add(dic);
		}
		return list;
	}
}
