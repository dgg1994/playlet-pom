package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 备案审核列表行。
 */
@Data
@ApiModel(value = "备案审核列表项", description = "备案审核列表展示的单行数据")
public class DramaRecordAuditListRespEntity {

	@ApiModelProperty("剧ID")
	private Integer dramaId;

	@ApiModelProperty("剧名")
	private String dramaTitle;

	@ApiModelProperty("封面URL")
	private String coverUrl;

	@ApiModelProperty("出品方")
	private String producerFirm;

	@ApiModelProperty("备案分类 1一类 2二类 3三类")
	private Integer filingCategory;

	@ApiModelProperty("备案分类名称")
	private String filingCategoryLabel;

	@ApiModelProperty("总集数")
	private Integer totalEpisodes;

	@ApiModelProperty("备案状态")
	private Integer recordStatus;

	@ApiModelProperty("备案状态名称")
	private String recordStatusLabel;

	@ApiModelProperty("当前审核节点（初审/复审，仅待审时展示）")
	private String currentStepLabel;

	@ApiModelProperty("上传时间")
	private Date uploadDate;

	@ApiModelProperty("备案号")
	private String recordNo;
}
