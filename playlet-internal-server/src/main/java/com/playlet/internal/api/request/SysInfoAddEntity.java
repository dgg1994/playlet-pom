package com.playlet.internal.api.request;

import lombok.Data;

import java.util.List;

/**
 * 类描述：新增配置入参
 *
 * @author GeminiSun
 * @date 2026/07/30 11:01
 */
@Data
public class SysInfoAddEntity {

    private Integer configType;

    private List<ContentItemEntity> configContent;

}
