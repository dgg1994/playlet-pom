package com.playlet.internal.enums;

/**
 * 类描述：推荐轮播枚举
 *
 * @author GeminiSun
 * @date 2026/07/25 11:02
 */
public enum RecommendedCarouselEnums {

    NOT_RECOMMENDED(0, "否"),
    RECOMMENDED(1, "是");

    private Integer index;

    private String name;


    private RecommendedCarouselEnums(Integer index, String name) {
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
