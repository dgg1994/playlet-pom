package com.playlet.oversea.enums;

/**
 * 用户性别：0未知 1男 2女。
 */
public enum UserGenderEnums {

	UNKNOWN(0),
	MALE(1),
	FEMALE(2);

	private final int index;

	UserGenderEnums(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public static boolean isValid(Integer gender) {
		if (gender == null) {
			return true;
		}
		for (UserGenderEnums e : values()) {
			if (e.index == gender) {
				return true;
			}
		}
		return false;
	}
}
