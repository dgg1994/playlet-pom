package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 作家端我的剧本列表行。
 */
@Data
@ApiModel(value = "作家剧本列表行")
public class CreatorScriptListRespEntity {

	private Integer id;
	private String title;
	private String coverUrl;
	private Integer episodes;
	private BigDecimal price;
	private Integer saleStatus;
	private String saleStatusText;
	private Integer auditStatus;
	private String auditStatusText;
	private String auditRejectReason;
	private Integer shelfStatus;
	private String shelfStatusText;
	private Date setTime;
	private Date gmtModified;

	@ApiModelProperty("收藏数")
	private Long favoriteCount;
}
