package com.playlet.internal.api.response;

import com.playlet.internal.entity.drama.TagEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

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

	@ApiModelProperty("所属剧名（集列表）")
	private String dramaTitle;

	@ApiModelProperty("封面URL（签过名，可访问）")
	private String coverUrl;

	@ApiModelProperty("作品简介（剧评审对象）")
	private String descriptionInfo;

	@ApiModelProperty("出品方")
	private String producerFirm;

	@ApiModelProperty("标签列表（剧评审对象）")
	private List<TagEntity> tagList;

	@ApiModelProperty("集数（仅集）")
	private Integer setNum;

	@ApiModelProperty("视频名（仅集）")
	private String videoName;

	@ApiModelProperty("视频播放URL（签过名，仅集）")
	private String videoUrl;

	@ApiModelProperty("聚合审核状态")
	private Integer auditStatus;

	@ApiModelProperty("上架状态 0未上架 1已上架")
	private Integer shelfStatus;

	@ApiModelProperty("驳回原因")
	private String auditRejectReason;

	@ApiModelProperty("AI预审状态")
	private Integer aiStatus = 0;

	@ApiModelProperty("AI预审备注")
	private String aiHandleRemark;

	@ApiModelProperty("A组状态")
	private Integer groupAStatus = 0;

	@ApiModelProperty("A组审核备注")
	private String groupAHandleRemark;

	@ApiModelProperty("B组状态")
	private Integer groupBStatus = 0;

	@ApiModelProperty("B组审核备注")
	private String groupBHandleRemark;

	@ApiModelProperty("上传/创建日期")
	private Date uploadDate;
}
