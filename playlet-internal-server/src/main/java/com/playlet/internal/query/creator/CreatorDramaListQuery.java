package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作家端作品列表查询（归属由登录态注入，禁止客户端传 belongUser）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "作家作品列表", description = "仅查询当前登录作家的剧")
public class CreatorDramaListQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "dramaTitle", value = "剧名模糊检索")
	private String dramaTitle;

	@ApiModelProperty(name = "auditStatus", value = "审核状态：0/1待审核（含待审+审核中） 2通过 3驳回 4申诉中")
	private Integer auditStatus;

	@ApiModelProperty(name = "shelfStatus", value = "上架状态 0未上架 1已上架")
	private Integer shelfStatus;
}
