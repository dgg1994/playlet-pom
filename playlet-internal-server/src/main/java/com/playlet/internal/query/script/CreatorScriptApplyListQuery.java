package com.playlet.internal.query.script;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作家端收到的交易申请列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "作家申请列表查询", description = "仅查询当前作家的剧本申请")
public class CreatorScriptApplyListQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "applyType", value = "0全部 1购买 2议价")
	private Integer applyType;

	@ApiModelProperty(name = "applyStatus", value = "-1全部 0待处理 1已同意 2已拒绝 3已完成 4已取消 5已失效")
	private Integer applyStatus;

	@ApiModelProperty(name = "scriptId", value = "剧本ID")
	private Integer scriptId;
}
