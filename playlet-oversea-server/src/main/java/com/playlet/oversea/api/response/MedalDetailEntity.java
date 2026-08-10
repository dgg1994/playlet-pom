package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("勋章详情")
public class MedalDetailEntity {

	@ApiModelProperty("勋章ID")
	private Integer medalId;

	@ApiModelProperty("业务码")
	private String medalCode;

	@ApiModelProperty("勋章名称")
	private String medalName;

	@ApiModelProperty("副文案/Slogan")
	private String slogan;

	@ApiModelProperty("条件展示文案")
	private String conditionText;

	@ApiModelProperty("当前展示图标（已解锁/未解锁对应图，已签名）")
	private String iconUrl;

	@ApiModelProperty("炫耀分享底图（已签名）")
	private String shareBgUrl;

	@ApiModelProperty("炫耀分享标题")
	private String shareTitle;

	@ApiModelProperty("炫耀分享描述")
	private String shareDesc;

	@ApiModelProperty("当前进度")
	private Integer progress;

	@ApiModelProperty("目标次数")
	private Integer targetCount;

	@ApiModelProperty("是否解锁：0未解锁 1已解锁")
	private Integer unlocked;

	@ApiModelProperty("解锁时间，未解锁为 null")
	private Date unlockTime;

	@ApiModelProperty("推进行为类型")
	private String actionType;

	@ApiModelProperty("排序权重")
	private Integer sortWeight;
}
