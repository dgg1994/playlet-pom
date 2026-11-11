package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创作者收藏列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "创作者收藏查询")
public class CreatorFavoriteQuery extends PageQueryHelperEntity {

	@ApiModelProperty("创作者名称关键字")
	private String keyword;
}
