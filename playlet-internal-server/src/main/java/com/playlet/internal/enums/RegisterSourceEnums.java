package com.playlet.internal.enums;

/**
 * 类描述：注册来源
 *
 * @author GeminiSun
 * @date 2026/05/07 17:16
 */
public enum RegisterSourceEnums {


    ONE_CLICK_LOGIN(1, "一键注册用户"),
    SIGN_UP(2, "正常注册用户");

    private Integer index;

    private String name;


    private RegisterSourceEnums(Integer index, String name) {
        this.index = index;
        this.name = name;
    }


    public Integer getIndex() {
        return index;
    }


    public void setIndex(Integer index) {
        this.index = index;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }
}
