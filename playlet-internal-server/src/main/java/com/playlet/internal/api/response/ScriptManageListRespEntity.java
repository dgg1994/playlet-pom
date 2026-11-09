package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端剧本列表行。
 */
@Data
@ApiModel(value = "管理端剧本列表行")
public class ScriptManageListRespEntity {

	private Integer id;
	private String title;
	private String coverUrl;
	private Integer authorId;
	private String authorName;
	private Integer auditStatus;
	private String auditStatusText;
	private Integer shelfStatus;
	private String shelfStatusText;
	private Integer saleStatus;
	private String saleStatusText;
	private BigDecimal price;
	private Integer episodes;
	private Date setTime;
}
