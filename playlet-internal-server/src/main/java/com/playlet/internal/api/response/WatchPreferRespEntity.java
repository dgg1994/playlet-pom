package com.playlet.internal.api.response;

import lombok.Data;

import java.util.List;

/**
 * 类描述：偏好列表
 *
 * @author GeminiSun
 * @date 2026/10/10 11:55
 */
@Data
public class WatchPreferRespEntity {

    private String categoryCode;

    private String categoryName;

    private List<WatchPreferTagRespEntity> watchPreferTagRespEntities;
}