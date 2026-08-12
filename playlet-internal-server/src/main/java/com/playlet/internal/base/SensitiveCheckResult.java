package com.playlet.internal.base;

import com.playlet.internal.security.sensitive.SensitiveMatch;
import lombok.Data;

import java.util.List;

/**
 * 类描述：敏感词命中返回结果集
 *
 * @author GeminiSun
 * @date 2026/08/12 09:21
 */
@Data
public class SensitiveCheckResult {
    /**
     * 是否通过
     */
    private Boolean pass;


    /**
     * 命中的敏感词
     */
    private List<SensitiveMatch> matches;


    /**
     * 最高风险等级
     */
    private Integer level;


}