package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 运营数据总览汇总（对齐管理端看板原型）。
 */
@Data
@ApiModel(value = "运营数据总览", description = "KPI + 趋势 + 收入构成 + 热播榜 + 待办")
public class OpsDashboardSummaryResp {

	@ApiModelProperty("区间类型 today|7d|30d|custom")
	private String rangeType;

	@ApiModelProperty("区间开始日 yyyy-MM-dd（趋势/构成用）")
	private String startDate;

	@ApiModelProperty("区间结束日 yyyy-MM-dd")
	private String endDate;

	@ApiModelProperty("数据更新时间")
	private Date updateTime;

	// ---------- KPI（今日口径，较昨日环比） ----------

	@ApiModelProperty("今日充值金额(元)")
	private BigDecimal todayRechargeAmount;

	@ApiModelProperty("充值较昨日涨跌幅%（无昨日数据为 null）")
	private BigDecimal todayRechargeMomRate;

	@ApiModelProperty("今日新增用户")
	private Long todayNewUserCnt;

	@ApiModelProperty("新增用户较昨日涨跌幅%")
	private BigDecimal todayNewUserMomRate;

	@ApiModelProperty("付费用户数（今日去重）")
	private Long payingUserCnt;

	@ApiModelProperty("付费转化率%：付费用户/今日DAU")
	private BigDecimal payConversionRate;

	@ApiModelProperty("在架短剧数")
	private Long onShelfDramaCnt;

	@ApiModelProperty("待审核短剧数")
	private Long pendingAuditDramaCnt;

	// ---------- 趋势 / 构成 / 榜 / 待办 ----------

	@ApiModelProperty("充值与播放量趋势")
	private List<OpsDashboardTrendPoint> trendList;

	@ApiModelProperty("收入构成")
	private List<OpsDashboardRevenuePart> revenueParts;

	@ApiModelProperty("热播短剧 TOP5")
	private List<OpsDashboardHotDramaItem> hotDramaTop5;

	@ApiModelProperty("待办事项")
	private List<OpsDashboardTodoItem> todoList;
}
