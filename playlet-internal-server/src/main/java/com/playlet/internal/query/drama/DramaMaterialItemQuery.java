package com.playlet.internal.query.drama;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 短剧创建向导提交的备案材料条目（对应 drama_record_material）。
 */
@Data
@ApiModel(value = "备案材料条目", description = "材料类型 + 七牛文件key")
public class DramaMaterialItemQuery {

	@NotNull(message = "材料类型不能为空")
	@ApiModelProperty(value = "材料类型（DramaRecordMaterialTypeEnums code）", required = true)
	private Integer materialType;

	@ApiModelProperty(value = "文件对象key（七牛）", required = true)
	private String fileKey;

	@ApiModelProperty(value = "原始文件名")
	private String fileName;

	@ApiModelProperty(value = "是否必填 1是 0否")
	private Integer required;
}
