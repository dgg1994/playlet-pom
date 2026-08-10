package com.playlet.oversea.api.request;

import lombok.Data;

import java.util.List;

/** 新增配置入参 */
@Data
public class SysInfoAddEntity {

    private Integer configType;

    private List<ContentItemEntity> configContent;

}
