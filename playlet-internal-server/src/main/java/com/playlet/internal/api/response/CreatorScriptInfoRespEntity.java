package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作家端我的剧本详情：市场详情 + 审核/上架信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "作家剧本详情")
public class CreatorScriptInfoRespEntity extends ScriptMarketDetailRespEntity {

	@ApiModelProperty("审核状态 -1草稿 0待审 1审核中 2通过 3驳回")
	private Integer auditStatus;

	@ApiModelProperty("审核状态文案")
	private String auditStatusText;

	@ApiModelProperty("审核驳回原因")
	private String auditRejectReason;

	@ApiModelProperty("上架状态 0下架 1上架")
	private Integer shelfStatus;

	@ApiModelProperty("上架状态文案")
	private String shelfStatusText;

	@ApiModelProperty("下架原因")
	private String offlineReason;

	@ApiModelProperty("附件签名URL")
	private String attachmentUrl;
}
