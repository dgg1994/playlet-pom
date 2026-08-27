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
 * 钱包 KYC 申请流水。
 */
@Data
@TableName("wallet_kyc_apply")
@ApiModel(value = "钱包KYC申请", description = "KYC 提交流水")
public class WalletKycApplyEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("wallet_user_id")
	@ApiModelProperty(name = "walletUserId", value = "wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("first_name")
	@ApiModelProperty(name = "firstName", value = "英文名")
	private String firstName;

	@TableField("last_name")
	@ApiModelProperty(name = "lastName", value = "英文姓")
	private String lastName;

	@TableField("id_no_cipher")
	@ApiModelProperty(name = "idNoCipher", value = "证件号密文")
	private String idNoCipher;

	@TableField("id_no_hash")
	@ApiModelProperty(name = "idNoHash", value = "证件号 SHA-256")
	private String idNoHash;

	@TableField("email")
	@ApiModelProperty(name = "email", value = "KYC 邮箱")
	private String email;

	@TableField("nation_code")
	@ApiModelProperty(name = "nationCode", value = "国籍 ISO Alpha-3")
	private String nationCode;

	@TableField("cert_type")
	@ApiModelProperty(name = "certType", value = "1身份证 2护照 3驾照")
	private Integer certType;

	@TableField("id_url")
	@ApiModelProperty(name = "idUrl", value = "证件正面 url")
	private String idUrl;

	@TableField("id_back_url")
	@ApiModelProperty(name = "idBackUrl", value = "证件反面 url")
	private String idBackUrl;

	@TableField("birthday")
	@ApiModelProperty(name = "birthday", value = "生日 yyyy-MM-dd")
	private String birthday;

	@TableField("country_code")
	@ApiModelProperty(name = "countryCode", value = "居住国 ISO Alpha-3")
	private String countryCode;

	@TableField("area_code")
	@ApiModelProperty(name = "areaCode", value = "手机区号")
	private String areaCode;

	@TableField("phone")
	@ApiModelProperty(name = "phone", value = "手机号")
	private String phone;

	@TableField("file_type")
	@ApiModelProperty(name = "fileType", value = "补充资料类型")
	private Integer fileType;

	@TableField("file_url")
	@ApiModelProperty(name = "fileUrl", value = "补充资料 url")
	private String fileUrl;

	@TableField("face_url")
	@ApiModelProperty(name = "faceUrl", value = "人脸报告 url")
	private String faceUrl;

	@TableField("reference_id")
	@ApiModelProperty(name = "referenceId", value = "第三方 sessionId")
	private String referenceId;

	@TableField("reference_type")
	@ApiModelProperty(name = "referenceType", value = "第三方报告类型")
	private String referenceType;

	@TableField("selfie_url")
	@ApiModelProperty(name = "selfieUrl", value = "人脸自拍 url")
	private String selfieUrl;

	@TableField("apply_status")
	@ApiModelProperty(name = "applyStatus", value = "三方状态快照")
	private String applyStatus;

	@TableField("kyc_state")
	@ApiModelProperty(name = "kycState", value = "1待认证 2认证中 3成功 4失败")
	private Integer kycState;

	@TableField("failed_reason")
	@ApiModelProperty(name = "failedReason", value = "失败原因")
	private String failedReason;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "提交时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
