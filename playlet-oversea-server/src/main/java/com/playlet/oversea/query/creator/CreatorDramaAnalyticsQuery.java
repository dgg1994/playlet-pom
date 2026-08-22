package com.playlet.oversea.query.creator;

import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作家端作品数据分析查询（归属由登录态注入）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "作家作品数据分析", description = "剧名检索、按热度/时间排序")
public class CreatorDramaAnalyticsQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "dramaTitle", value = "剧名模糊检索")
	private String dramaTitle;

	@ApiModelProperty(name = "sortType", value = "排序：1按热度 2按时间，默认1")
	private Integer sortType;
}
