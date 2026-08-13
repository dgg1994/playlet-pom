package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class DramaWorkAuditListRespEntity {

	@ApiModelProperty("业务主键：剧=drama.id，集=drama_asset.id")
	private Integer bizId;

	@ApiModelProperty("所属剧ID")
	private Integer dramaId;

	@ApiModelProperty("集ID，类型为剧时为空")
	private Integer assetId;

	@ApiModelProperty("作品类型：1剧 2集")
	private Integer workType;

	@ApiModelProperty("展示名称")
	private String workName;

	@ApiModelProperty("封面URL（签过名，可访问）")
	private String coverUrl;

	@ApiModelProperty("集数（仅集）")
	private Integer setNum;

	@ApiModelProperty("视频名（仅集）")
	private String videoName;

	@ApiModelProperty("视频播放URL（签过名，仅集）")
	private String videoUrl;

	@ApiModelProperty("聚合审核状态")
	private Integer auditStatus;

	@ApiModelProperty("AI预审状态")
	private Integer aiStatus;

	@ApiModelProperty("A组状态")
	private Integer groupAStatus;

	@ApiModelProperty("B组状态")
	private Integer groupBStatus;

	@ApiModelProperty("上传/创建日期")
	private Date uploadDate;
}
