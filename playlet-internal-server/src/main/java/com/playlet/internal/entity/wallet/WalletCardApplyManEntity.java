package com.playlet.internal.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 开卡申请持卡人快照。
 */
@Data
@TableName("wallet_card_apply_man")
@ApiModel(value = "开卡持卡人快照", description = "申请单关联的持卡人副本")
public class WalletCardApplyManEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("apply_id")
	@ApiModelProperty(name = "applyId", value = "wallet_card_apply.id")
	private Long applyId;

	@TableField("wallet_user_id")
	@ApiModelProperty(name = "walletUserId", value = "wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("user_name")
	@ApiModelProperty(name = "userName", value = "英文名")
	private String userName;

	@TableField("user_surname")
	@ApiModelProperty(name = "userSurname", value = "英文姓")
	private String userSurname;

	@TableField("user_tel_dial_code")
	@ApiModelProperty(name = "userTelDialCode", value = "手机号区号")
	private String userTelDialCode;

	@TableField("user_tel_code")
	@ApiModelProperty(name = "userTelCode", value = "地区编码")
	private String userTelCode;

	@TableField("user_tel")
	@ApiModelProperty(name = "userTel", value = "手机号")
	private String userTel;

	@TableField("user_email")
	@ApiModelProperty(name = "userEmail", value = "邮箱")
	private String userEmail;

	@TableField("user_number")
	@ApiModelProperty(name = "userNumber", value = "证件号")
	private String userNumber;

	@TableField("user_sex")
	@ApiModelProperty(name = "userSex", value = "性别文案")
	private String userSex;

	@TableField("user_address")
	@ApiModelProperty(name = "userAddress", value = "住址")
	private String userAddress;

	@TableField("user_birthday")
	@ApiModelProperty(name = "userBirthday", value = "出生日期")
	private String userBirthday;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
