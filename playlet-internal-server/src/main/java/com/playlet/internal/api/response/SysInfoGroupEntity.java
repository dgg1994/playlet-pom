package com.playlet.internal.api.response;

import com.playlet.internal.api.request.ContentItemEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 配置列表按 configType 分组返回
 */
@Data
@ApiModel("配置分组（多语言）")
public class SysInfoGroupEntity {

	@ApiModelProperty("配置类型")
	private Integer configType;

	@ApiModelProperty("配置类型名称")
	private String configTypeName;

	@ApiModelProperty("配置标签")
	private Integer configLable;

	@ApiModelProperty("配置状态（1正常 2停用）")
	private Integer status;

	@ApiModelProperty("多语言配置内容")
	private List<ContentItemEntity> configContent;
}
