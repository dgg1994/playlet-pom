package com.playlet.internal.query.script;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 我的收藏 - 剧本收藏查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "剧本收藏查询")
public class ScriptFavoriteQuery extends PageQueryHelperEntity {

	@ApiModelProperty("剧本名称关键字")
	private String keyword;

	@ApiModelProperty("题材")
	private String topic;
}
