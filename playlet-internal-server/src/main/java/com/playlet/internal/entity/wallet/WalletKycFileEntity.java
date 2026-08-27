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
 * 钱包 KYC 证件文件。
 */
@Data
@TableName("wallet_kyc_file")
@ApiModel(value = "钱包KYC文件", description = "证件/自拍上传留痕")
public class WalletKycFileEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("wallet_user_id")
	@ApiModelProperty(name = "walletUserId", value = "wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("cert_type")
	@ApiModelProperty(name = "certType", value = "1身份证 2护照 3驾照")
	private Integer certType;

	@TableField("document_type")
	@ApiModelProperty(name = "documentType", value = "1正面 2反面 3手持/自拍")
	private Integer documentType;

	@TableField("document_file_id")
	@ApiModelProperty(name = "documentFileId", value = "三方文件 id")
	private String documentFileId;

	@TableField("document_file_url")
	@ApiModelProperty(name = "documentFileUrl", value = "文件 url/key")
	private String documentFileUrl;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "上传时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
