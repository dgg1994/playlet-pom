package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 作家端作品列表行（剧集系列管理卡片）。
 */
@Data
@ApiModel(value = "作家作品列表行", description = "封面/标题/总集数/更新时间")
public class CreatorDramaListRespEntity {

	@ApiModelProperty("剧ID")
	private Integer id;

	@ApiModelProperty("短剧标题")
	private String dramaTitle;

	@ApiModelProperty("封面海报（已签名）")
	private String coverUrl;

	@ApiModelProperty("总集数（共 N 集）")
	private Integer totalEpisodes;

	@ApiModelProperty("已上传集数（未删除）")
	private Integer uploadSetNum;

	@ApiModelProperty("更新时间（更新于）")
	private Date gmtModified;

	@ApiModelProperty("创建时间")
	private Date setTime;

	@ApiModelProperty("审核状态 0待审 1审核中 2通过 3驳回 4申诉中")
	private Integer auditStatus;

	@ApiModelProperty("驳回原因（审核驳回时有值）")
	private String auditRejectReason;

	@ApiModelProperty("上架状态 0未上架 1已上架")
	private Integer shelfStatus;
}
