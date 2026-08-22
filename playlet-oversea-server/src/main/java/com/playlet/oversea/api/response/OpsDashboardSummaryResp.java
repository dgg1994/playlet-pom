package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营看板汇总（对齐数据看板卡片；未实现指标默认 0）。
 */
@Data
@ApiModel(value = "运营看板汇总", description = "GMV/eCPM/ARPU/DAU/在线/留存/新增/人均播放")
public class OpsDashboardSummaryResp {

	@ApiModelProperty("区间类型 today|7d|30d|custom")
	private String rangeType;

	@ApiModelProperty("区间开始日 yyyy-MM-dd")
	private String startDate;

	@ApiModelProperty("区间结束日 yyyy-MM-dd")
	private String endDate;

	@ApiModelProperty("实时流水 GMV（万元）；未实现，默认 0")
	private BigDecimal gmv;

	@ApiModelProperty("广告 eCPM（元）；未实现，默认 0")
	private BigDecimal adEcpm;

	@ApiModelProperty("ARPU；未实现，默认 0")
	private BigDecimal arpu;

	@ApiModelProperty("DAU：单日=当日活跃；多日=区间平均日活（四舍五入）")
	private Long dau;

	@ApiModelProperty("当前在线设备数（实时，含未登录；与区间无关）")
	private Long onlineCount;

	@ApiModelProperty("在线判定窗口秒数")
	private Long onlineWindowSeconds;

	@ApiModelProperty("次日留存率 %（一位小数）")
	private BigDecimal retentionD1Rate;

	@ApiModelProperty("新增用户数")
	private Long newUserCnt;

	@ApiModelProperty("人均播放时长（分钟，一位小数）")
	private BigDecimal avgPlayMinutes;

	@ApiModelProperty("总播放秒（辅助字段；优先用户日播放表）")
	private Long totalPlaySeconds;

	@ApiModelProperty("区间 person-day（每日 DAU 之和，辅助字段）")
	private Long personDays;
}
