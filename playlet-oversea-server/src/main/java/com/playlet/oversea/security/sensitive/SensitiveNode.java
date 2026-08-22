package com.playlet.oversea.security.sensitive;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 类描述：
 *
 * @author GeminiSun
 * @date 2026/08/11 17:58
 */
@Data
public class SensitiveNode {

    /**
     * 子节点
     */
    private Map<Character, SensitiveNode> children =
            new HashMap<>();


    /**
     * 是否是敏感词结尾
     */
    private boolean end;


    /**
     * 完整敏感词
     */
    private String word;


    /**
     * 敏感等级
     *
     * 1 警告
     * 2 审核
     * 3 禁止
     */
    private Integer level;
}