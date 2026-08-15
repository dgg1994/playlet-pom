package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作家端评论列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "作家评论列表查询", description = "剧名搜索 + 热度/时间排序")
public class CreatorCommentListQuery extends PageQueryHelperEntity {

	@ApiModelProperty("剧名模糊搜索")
	private String dramaTitle;

	@ApiModelProperty("排序：1按热度 2按时间，默认2")
	private Integer sortType;
}
