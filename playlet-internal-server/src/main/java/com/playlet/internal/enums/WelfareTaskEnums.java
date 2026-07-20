package com.playlet.internal.enums;

/**
 * 类描述：福利任务状态
 *
 * @author GeminiSun
 * @date 2026/07/18 16:01
 */
public enum WelfareTaskEnums {
    STATUS_ENABLED(1, "开启"),
    STATUS_DISABLED(0, "关闭");


    private final Integer index;

    private final String name;

    WelfareTaskEnums(Integer index, String name) {
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
