package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 运营看板待办项。
 */
@Data
@ApiModel(value = "运营待办项", description = "待审核/待结算/申诉等")
public class OpsDashboardTodoItem {

	@ApiModelProperty("类型 AUDIT / SETTLE / APPEAL / ACTIVITY")
	private String type;

	@ApiModelProperty("标题")
	private String title;

	@ApiModelProperty("描述文案")
	private String desc;

	@ApiModelProperty("数量（可空）")
	private Integer count;

	@ApiModelProperty("跳转路径（前端路由，可空）")
	private String link;
}
