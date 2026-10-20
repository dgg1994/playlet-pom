package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端用户管理顶部统计。
 */
@Data
@ApiModel(value = "用户管理统计", description = "总注册/今日新增/付费/有效会员")
public class AppUserStatsRespEntity {

	@ApiModelProperty("总注册用户")
	private Long totalUsers;

	@ApiModelProperty("今日新增")
	private Long todayNewUsers;

	@ApiModelProperty("付费用户（有成功现金会员订单）")
	private Long paidUsers;

	@ApiModelProperty("有效会员")
	private Long activeMembers;
}
