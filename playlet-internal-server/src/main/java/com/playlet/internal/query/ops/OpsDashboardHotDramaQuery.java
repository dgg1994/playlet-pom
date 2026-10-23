package com.playlet.internal.query.ops;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运营看板热播短剧排行分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "热播短剧排行查询", description = "查看全部内容分页")
public class OpsDashboardHotDramaQuery extends PageQueryHelperEntity {

	@ApiModelProperty(value = "区间类型：today / 7d / 30d / custom", required = true)
	private String rangeType;

	@ApiModelProperty("自定义开始日 yyyy-MM-dd（custom 必填）")
	private String startDate;

	@ApiModelProperty("自定义结束日 yyyy-MM-dd（custom 必填）")
	private String endDate;
}
