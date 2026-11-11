package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 征稿广场列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "征稿广场列表查询", description = "状态 / 题材 / 截止日期 / 排序 / 分页")
public class CallSquareListQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "keyword", value = "关键词：标题/摘要/对标作品/标签")
	private String keyword;

	@ApiModelProperty(name = "topic", value = "题材标签，全部表示不筛")
	private String topic;

	@ApiModelProperty(name = "status", value = "征稿状态 0全部 1征稿中 2即将截止 3已结束")
	private Integer status;

	@ApiModelProperty(name = "deadlineStart", value = "截止日期起（yyyy-MM-dd）")
	private String deadlineStart;

	@ApiModelProperty(name = "deadlineEnd", value = "截止日期止（yyyy-MM-dd）")
	private String deadlineEnd;

	@ApiModelProperty(name = "sortType", value = "1综合 2最新发布 3奖金最高 4参与最多 5即将截止")
	private Integer sortType;
}
