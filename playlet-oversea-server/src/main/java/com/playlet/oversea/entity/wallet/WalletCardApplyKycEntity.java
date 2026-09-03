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
 * 开卡申请 KYC 证件快照。
 */
@Data
@TableName("wallet_card_apply_kyc")
@ApiModel(value = "开卡KYC快照", description = "申请单关联的证件信息")
public class WalletCardApplyKycEntity {

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

	@TableField("paperwork_type")
	@ApiModelProperty(name = "paperworkType", value = "PASSPORT / NATIONAL_ID")
	private String paperworkType;

	@TableField("paperwork_num")
	@ApiModelProperty(name = "paperworkNum", value = "证件号码")
	private String paperworkNum;

	@TableField("expiration_time")
	@ApiModelProperty(name = "expirationTime", value = "证件到期时间")
	private String expirationTime;

	@TableField("front_photo_id")
	@ApiModelProperty(name = "frontPhotoId", value = "正面照 id")
	private String frontPhotoId;

	@TableField("front_photo_url")
	@ApiModelProperty(name = "frontPhotoUrl", value = "正面照 url")
	private String frontPhotoUrl;

	@TableField("back_photo_id")
	@ApiModelProperty(name = "backPhotoId", value = "反面照 id")
	private String backPhotoId;

	@TableField("back_photo_url")
	@ApiModelProperty(name = "backPhotoUrl", value = "反面照 url")
	private String backPhotoUrl;

	@TableField("handheld_photo_id")
	@ApiModelProperty(name = "handheldPhotoId", value = "手持证件照 id")
	private String handheldPhotoId;

	@TableField("handheld_photo_url")
	@ApiModelProperty(name = "handheldPhotoUrl", value = "手持证件照 url")
	private String handheldPhotoUrl;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
