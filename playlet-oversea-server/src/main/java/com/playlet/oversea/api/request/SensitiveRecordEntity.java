package com.playlet.oversea.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 类描述：敏感信息记录参数
 *
 * @author GeminiSun
 * @date 2026/08/12 10:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveRecordEntity {

    /**
     * 评论ID
     */
    private Integer commentId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 短剧ID
     */
    private Integer dramaId;

    /**
     * 剧集ID
     */
    private Integer episodeId;

    /**
     * 原内容
     */
    private String content;

    /**
     * 来源类型
     *
     * 1评论
     * 2举报
     * 3弹幕
     */
    private Integer sourceType;
}