package com.playlet.internal.query.drama;

import javax.validation.constraints.NotNull;

import com.playlet.internal.query.pub.PageQueryHelperEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryDramaCommentQuery extends PageQueryHelperEntity {

	@NotNull(message = "剧ID不能为空")
	@ApiModelProperty(name = "dramaId", value = "剧ID", required = true, dataType = "Integer")
	private Integer dramaId;

	@ApiModelProperty(name = "deleteState", value = "删除状态", required = false, dataType = "Integer")
	private Integer deleteState;

	@ApiModelProperty(name = "parentId", value = "父级id", required = false, dataType = "Integer")
	private Integer parentId;
}
