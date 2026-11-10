package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 设置保存（隐私 + 推送，部分字段更新）。
 */
@Data
@ApiModel(value = "创作者设置保存")
public class CreatorSettingSaveQuery {

	@ApiModelProperty(name = "profileVisible", value = "主页可见 1公开 0仅自己")
	private Integer profileVisible;

	@ApiModelProperty(name = "contactVisible", value = "联系方式可见 1是 0否")
	private Integer contactVisible;

	@ApiModelProperty(name = "workVisible", value = "作品公开 1是 0否")
	private Integer workVisible;

	@ApiModelProperty(name = "pushSystem")
	private Integer pushSystem;

	@ApiModelProperty(name = "pushCoop")
	private Integer pushCoop;

	@ApiModelProperty(name = "pushActivity")
	private Integer pushActivity;

	@ApiModelProperty(name = "pushAudit")
	private Integer pushAudit;

	@ApiModelProperty(name = "pushIncome")
	private Integer pushIncome;
}
