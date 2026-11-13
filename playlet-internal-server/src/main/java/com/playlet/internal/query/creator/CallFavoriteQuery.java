package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 我的收藏 - 征稿收藏查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "征稿收藏查询")
public class CallFavoriteQuery extends PageQueryHelperEntity {

	@ApiModelProperty("征稿标题关键字")
	private String keyword;

	@ApiModelProperty("题材")
	private String topic;
}
