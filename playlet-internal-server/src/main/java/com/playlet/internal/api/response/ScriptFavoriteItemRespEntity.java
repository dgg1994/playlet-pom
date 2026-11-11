package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 我的收藏列表行。
 */
@Data
@ApiModel(value = "我的收藏行")
public class ScriptFavoriteItemRespEntity {

	@ApiModelProperty("剧本ID")
	private Integer id;
	private String title;
	private String coverUrl;
	private Integer episodes;
	private BigDecimal price;
	private Integer saleStatus;
	private String saleStatusText;
	private Date favoritedAt;
	@ApiModelProperty("题材标签")
	private String topic;
	@ApiModelProperty("作者")
	private String authorName;
	@ApiModelProperty("报价方式：固定报价/议价")
	private String priceModeText;
	@ApiModelProperty("收藏时间文案 yyyy-MM-dd")
	private String favoritedAtText;
}
