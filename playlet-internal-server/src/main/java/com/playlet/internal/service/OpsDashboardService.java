package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
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
	@ApiOperation(value = "看板指标汇总", notes = "对齐数据看板：GMV/eCPM/ARPU（暂为0）、DAU、在线、次日留存、新增用户、人均播放；"
			+ "body.rangeType=today|7d|30d|custom；custom 需 startDate、endDate")
	ResponseBase summary(OpsDashboardQuery query);
}
