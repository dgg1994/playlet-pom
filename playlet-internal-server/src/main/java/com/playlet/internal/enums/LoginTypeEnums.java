package com.playlet.internal.enums;

/**
 * 一键登录渠道（与 App 传参 type 一致）
 */
public enum LoginTypeEnums {

    APPLE(1, "apple"),
    GOOGLE(2, "google");

    private final Integer index;
    private final String name;

    LoginTypeEnums(Integer index, String name) {
        this.index = index;
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}
