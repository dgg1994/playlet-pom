package com.playlet.oversea.query.security;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("违规评论处置请求")
public class IllegalCommentHandleQuery {

	@ApiModelProperty(value = "违规记录ID", required = true)
	private Long id;

	@ApiModelProperty(value = "处置类型：1忽略/通过 2删除评论 3禁言用户 4冻结账户", required = true)
	private Integer handleType;

	@ApiModelProperty("处置备注")
	private String handleRemark;
}
