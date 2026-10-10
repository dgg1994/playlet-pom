package com.playlet.internal.api.response;

import lombok.Data;

/**
 * 类描述：偏好标签
 *
 * @author GeminiSun
 * @date 2026/10/10 14:37
 */
@Data
public class WatchPreferTagRespEntity {

    private String groupId;

    private String tagName;

    /** 当前用户是否已选（mine 接口回显用；列表接口可为 null） */
    private Boolean selected;
}