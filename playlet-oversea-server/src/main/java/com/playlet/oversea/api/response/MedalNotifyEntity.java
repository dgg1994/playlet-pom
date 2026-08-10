package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("勋章解锁弹窗项")
public class MedalNotifyEntity {

	@ApiModelProperty("勋章ID")
	private Integer medalId;

	@ApiModelProperty("业务码")
	private String medalCode;

	@ApiModelProperty("勋章名称")
	private String medalName;

	@ApiModelProperty("副文案")
	private String slogan;

	@ApiModelProperty("已解锁图标（签名URL）")
	private String iconUrl;

	@ApiModelProperty("解锁时间")
	private Date unlockTime;
}
