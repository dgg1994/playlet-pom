package com.playlet.internal.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 我的征稿收藏列表行。
 */
@Data
@ApiModel(value = "我的征稿收藏行")
public class CallFavoriteItemRespEntity {

	@ApiModelProperty("征稿ID")
	private Integer id;

	@ApiModelProperty("标题")
	private String title;

	@ApiModelProperty("题材")
	private String topic;

	@ApiModelProperty("总奖金")
	private Long amount;

	@ApiModelProperty("独家/非独家")
	private Boolean exclusive;

	@ApiModelProperty("需求稿件数")
	private Integer need;

	@ApiModelProperty("当前参与人数")
	private Integer participants;

	@ApiModelProperty("征稿方昵称")
	private String publisher;

	@ApiModelProperty("截止日期")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date deadline;

	@ApiModelProperty("剩余天数（已结束为 0）")
	private Integer daysLeft;

	@ApiModelProperty("状态文案：征稿中/即将截止/已结束")
	private String status;

	@ApiModelProperty("收藏时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	private Date favoritedAt;

	@ApiModelProperty("收藏时间文案 yyyy-MM-dd")
	private String favoritedAtText;
}
