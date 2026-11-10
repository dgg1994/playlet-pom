package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创作者代表作列表项（产品管理）。
 */
@Data
@ApiModel(value = "创作者代表作")
public class CreatorRepresentativeRespEntity {

	@ApiModelProperty(value = "主键")
	private Integer id;

	@ApiModelProperty(value = "短剧名称")
	private String dramaName;

	@ApiModelProperty(value = "备案分类")
	private String category;

	@ApiModelProperty(value = "封面图片key")
	private String cover;

	@ApiModelProperty(value = "封面图片签名URL")
	private String coverUrl;

	@ApiModelProperty(value = "作品链接")
	private String workLink;

	@ApiModelProperty(value = "简介")
	private String intro;

	@ApiModelProperty(value = "0未上架 1已上架")
	private Integer displayStatus;

	@ApiModelProperty(value = "展示状态文案：已上架/已下架")
	private String displayStatusText;

	@ApiModelProperty(value = "0待审 1审核中 2通过 3驳回")
	private Integer auditStatus;

	@ApiModelProperty(value = "审核状态文案：审核中/审核通过/审核驳回")
	private String auditStatusText;

	@ApiModelProperty(value = "审核备注（驳回原因）")
	private String auditRemark;
}
