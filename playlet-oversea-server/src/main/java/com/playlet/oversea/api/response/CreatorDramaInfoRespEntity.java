package com.playlet.oversea.api.response;

import com.playlet.oversea.entity.drama.TagEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 作家端作品详情。
 */
@Data
@ApiModel(value = "作家作品详情", description = "剧信息 + 标签 + 全部未删除集")
public class CreatorDramaInfoRespEntity {

	@ApiModelProperty("剧ID")
	private Integer id;

	@ApiModelProperty("短剧标题")
	private String dramaTitle;

	@ApiModelProperty("封面（已签名）")
	private String coverUrl;

	@ApiModelProperty("出品方")
	private String producerFirm;

	@ApiModelProperty("简介")
	private String descriptionInfo;

	@ApiModelProperty("总集数")
	private Integer totalEpisodes;

	@ApiModelProperty("已上传集数（未删除）")
	private Integer uploadSetNum;

	@ApiModelProperty("是否完结 1是0否")
	private Integer finishedState;

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

	@ApiModelProperty("标签")
	private List<TagEntity> tagList;

	@ApiModelProperty("已上传集（含审核中/驳回，按集数排序）")
	private List<CreatorDramaAssetRespEntity> assetList;

	@ApiModelProperty("创建时间")
	private Date setTime;

	@ApiModelProperty("更新时间")
	private Date gmtModified;
}
