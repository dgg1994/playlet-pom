package com.playlet.internal.query.script;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作家端我的剧本列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "作家剧本列表查询", description = "归属由登录态注入")
public class CreatorScriptListQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "title", value = "剧本名模糊检索")
	private String title;

	@ApiModelProperty(name = "auditStatus", value = "审核状态 -1草稿 0待审 1审核中 2通过 3驳回")
	private Integer auditStatus;

	@ApiModelProperty(name = "shelfStatus", value = "上架状态 0下架 1上架")
	private Integer shelfStatus;

	@ApiModelProperty(name = "saleStatus", value = "售卖状态 0待售 1议价中 2已售 3签约中")
	private Integer saleStatus;
}
