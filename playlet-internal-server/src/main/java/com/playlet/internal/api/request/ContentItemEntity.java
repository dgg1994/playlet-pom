package com.playlet.internal.api.request;

import lombok.Data;

/**
 * 类描述：配置多语言
 *
 * @author GeminiSun
 * @date 2026/07/30 11:05
 */
@Data
public class ContentItemEntity {

    private Integer id;

    private String language;

    private String languageName;

    private String configName;

    private String configContent;

    private String configUrl;
}