package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端重置登录密码。
 */
@Data
@ApiModel(value = "重置用户密码", description = "不传 newPassword 则服务端生成临时密码并返回一次")
public class AppUserResetPwdQuery {

	@ApiModelProperty(value = "用户ID", required = true)
	private Integer uid;

	@ApiModelProperty("新密码；空则自动生成")
	private String newPassword;
}
