package com.playlet.oversea.security.sensitive;

import lombok.Data;

/**
 * 类描述：敏感词命中结果
 *
 * @author GeminiSun
 * @date 2026/08/12 09:26
 */
@Data
public class SensitiveMatch {
    /**
     * 敏感词
     */
    private String word;


    /**
     * 风险等级
     */
    private Integer level;


    public SensitiveMatch(
            String word,
            Integer level){

        this.word = word;
        this.level = level;

    }
}