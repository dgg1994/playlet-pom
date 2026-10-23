package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.ops.OpsDashboardHotDramaQuery;
import com.playlet.internal.query.ops.OpsDashboardQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端运营看板：网关 /china/admin/opsDashboard/**
 */
@RequestMapping("/opsDashboard")
@Api(value = "运营数据看板", tags = "运营数据看板")
public interface OpsDashboardService {

	@PostMapping("/summary")
	@ApiOperation(value = "运营数据总览", notes = "KPI（今日充值/新增/付费转化/在架）+ 充值与播放趋势 + 收入构成 "
			+ "+ 热播 TOP5 + 待办；body.rangeType=today|7d|30d|custom（默认7d）；custom 需 startDate、endDate")
	ResponseBase summary(OpsDashboardQuery query);

	@PostMapping("/hotDramaList")
	@ApiOperation(value = "热播短剧排行（查看全部）", notes = "按区间播放量倒序分页；含分类、播放量、今日充值；"
			+ "body 同 summary 的 rangeType/startDate/endDate，另加 pageNumber/pageSize")
	ResponseBase hotDramaList(OpsDashboardHotDramaQuery query);
}
