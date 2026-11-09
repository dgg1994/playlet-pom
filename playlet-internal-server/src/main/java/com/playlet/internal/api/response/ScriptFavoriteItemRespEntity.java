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
}
