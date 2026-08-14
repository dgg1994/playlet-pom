package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 作家端剧集行（详情用）。
 */
@Data
@ApiModel(value = "作家剧集", description = "含审核/上架/驳回原因，不含运营备注与互动数")
public class CreatorDramaAssetRespEntity {

	@ApiModelProperty("集ID")
	private Integer id;

	@ApiModelProperty("剧ID")
	private Integer dramaId;

	@ApiModelProperty("第几集")
	private Integer setNum;

	@ApiModelProperty("视频名")
	private String videoName;

	@ApiModelProperty("视频播放URL（已签名）")
	private String videoUrl;

	@ApiModelProperty("视频类型 1横屏 2竖屏")
	private Integer videoType;

	@ApiModelProperty("审核状态 0待审 1审核中 2通过 3驳回 4申诉中")
	private Integer auditStatus;

	@ApiModelProperty("上架状态 0未上架 1已上架")
	private Integer shelfStatus;

	@ApiModelProperty("驳回原因")
	private String auditRejectReason;

	@ApiModelProperty("申诉状态 0无 1申诉中 2申诉通过 3申诉驳回")
	private Integer appealStatus;

	@ApiModelProperty("申诉理由")
	private String appealReason;

	@ApiModelProperty("申诉时间")
	private Date appealTime;

	@ApiModelProperty("上传时间")
	private Date setTime;
}
