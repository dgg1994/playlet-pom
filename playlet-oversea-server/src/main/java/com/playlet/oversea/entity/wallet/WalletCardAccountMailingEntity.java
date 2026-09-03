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
 * 用户收件地址（对齐 onetoken app_card_account_mailing）。
 */
@Data
@TableName("wallet_card_account_mailing")
@ApiModel(value = "用户收件地址", description = "实体卡邮寄地址本地快照")
public class WalletCardAccountMailingEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "本地主键")
	private Long id;

	@TableField("wallet_user_id")
	@ApiModelProperty(name = "walletUserId", value = "wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("address_id")
	@ApiModelProperty(name = "addressId", value = "三方邮寄地址 id，即 deliveryAddressId")
	private Integer addressId;

	@TableField("country_region_id")
	@ApiModelProperty(name = "countryRegionId", value = "邮寄地区 id，查询邮寄地区列表获得")
	private Integer countryRegionId;

	@TableField("country")
	@ApiModelProperty(name = "country", value = "国家")
	private String country;

	@TableField("city")
	@ApiModelProperty(name = "city", value = "城市")
	private String city;

	@TableField("receiver_name")
	@ApiModelProperty(name = "receiverName", value = "收件人")
	private String receiverName;

	@TableField("receiver_mobile")
	@ApiModelProperty(name = "receiverMobile", value = "收件人电话")
	private String receiverMobile;

	@TableField("receiver_address")
	@ApiModelProperty(name = "receiverAddress", value = "邮寄地址")
	private String receiverAddress;

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
