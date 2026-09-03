package com.playlet.oversea.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 钱包三方用户映射。
 */
@Data
@TableName("wallet_user")
@ApiModel(value = "钱包用户", description = "本地主体 ↔ 钱包三方 uid")
public class WalletUserEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("user_type")
	@ApiModelProperty(name = "userType", value = "1=C端 2=作家")
	private Integer userType;

	@TableField("local_uid")
	@ApiModelProperty(name = "localUid", value = "本地用户/作家主键")
	private Integer localUid;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("email")
	@ApiModelProperty(name = "email", value = "注册邮箱快照")
	private String email;

	@TableField("mobile_prefix")
	@ApiModelProperty(name = "mobilePrefix", value = "手机区号")
	private String mobilePrefix;

	@TableField("mobile_number")
	@ApiModelProperty(name = "mobileNumber", value = "手机号")
	private String mobileNumber;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1正常 0禁用")
	private Integer status;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
