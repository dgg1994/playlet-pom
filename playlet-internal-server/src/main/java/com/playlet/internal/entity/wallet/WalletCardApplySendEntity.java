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
 * 开卡申请邮寄地址。
 */
@Data
@TableName("wallet_card_apply_send")
@ApiModel(value = "开卡邮寄地址", description = "实体卡申请邮寄地址")
public class WalletCardApplySendEntity {

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

	@TableField("address_id")
	@ApiModelProperty(name = "addressId", value = "三方邮寄地址 id")
	private Integer addressId;

	@TableField("nation")
	@ApiModelProperty(name = "nation", value = "国家")
	private String nation;

	@TableField("province")
	@ApiModelProperty(name = "province", value = "省/州")
	private String province;

	@TableField("city")
	@ApiModelProperty(name = "city", value = "市")
	private String city;

	@TableField("address_info")
	@ApiModelProperty(name = "addressInfo", value = "详细地址")
	private String addressInfo;

	@TableField("collect_man")
	@ApiModelProperty(name = "collectMan", value = "收件人")
	private String collectMan;

	@TableField("collect_tel")
	@ApiModelProperty(name = "collectTel", value = "收件人电话")
	private String collectTel;

	@TableField("post_code")
	@ApiModelProperty(name = "postCode", value = "邮编")
	private String postCode;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
