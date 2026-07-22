package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("签到月历-单日")
public class SignInCalendarDayEntity {

	@ApiModelProperty("日期 yyyy-MM-dd")
	private String bizDate;

	@ApiModelProperty("日（1-31）")
	private Integer day;

	@ApiModelProperty("状态：signed已签 / makeup可补 / today今日未签 / empty不可操作，见 SignInCalendarDayStateEnums")
	private String state;
}
