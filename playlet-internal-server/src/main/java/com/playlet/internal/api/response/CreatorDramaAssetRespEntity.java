package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 作家端剧集行（对齐「剧名 - 剧集」表格）。
 */
@Data
@ApiModel(value = "作家剧集", description = "序列/时长/状态/曝光/完播/上传日期")
public class CreatorDramaAssetRespEntity {

	@ApiModelProperty("集ID")
	private Integer id;

	@ApiModelProperty("剧ID")
	private Integer dramaId;

	@ApiModelProperty("剧集序列（第几集）")
	private Integer setNum;

	@ApiModelProperty("剧集时长（秒）；暂无片源元数据时为空")
	private Integer durationSeconds;

	@ApiModelProperty("剧集时长展示，如 7'55\"")
	private String durationText;

	@ApiModelProperty("状态码 0待审 1审核中 2通过 3驳回 4申诉中")
	private Integer auditStatus;

	@ApiModelProperty("状态文案，如 审核中")
	private String auditStatusName;

	@ApiModelProperty("上架状态 0未上架 1已上架")
	private Integer shelfStatus;

	@ApiModelProperty("曝光量")
	private Long exposureCount;

	@ApiModelProperty("完播量")
	private Long completeCount;

	@ApiModelProperty("完播率 0-100（曝光为 0 时为 0）")
	private Double completeRate;

	@ApiModelProperty("上传日期")
	private Date setTime;

	@ApiModelProperty("视频名")
	private String videoName;

	@ApiModelProperty("视频播放URL（已签名）")
	private String videoUrl;

	@ApiModelProperty("驳回原因")
	private String auditRejectReason;
}
