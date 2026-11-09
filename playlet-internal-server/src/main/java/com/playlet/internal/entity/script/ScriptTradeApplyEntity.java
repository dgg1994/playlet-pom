package com.playlet.internal.entity.script;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 剧本市场-购买/议价申请。
 */
@Data
@TableName("script_trade_apply")
@ApiModel(value = "剧本交易申请", description = "购买/议价申请")
public class ScriptTradeApplyEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("apply_no")
	@ApiModelProperty(name = "applyNo", value = "申请编号")
	private String applyNo;

	@TableField("script_id")
	@ApiModelProperty(name = "scriptId", value = "script.id")
	private Integer scriptId;

	@TableField("buyer_uid")
	@ApiModelProperty(name = "buyerUid", value = "购买方 app_account.id")
	private Integer buyerUid;

	@TableField("author_id")
	@ApiModelProperty(name = "authorId", value = "编剧 creator_account.id")
	private Integer authorId;

	@TableField("apply_type")
	@ApiModelProperty(name = "applyType", value = "1购买 2议价")
	private Integer applyType;

	@TableField("license_type")
	@ApiModelProperty(name = "licenseType", value = "申请时授权类型")
	private Integer licenseType;

	@TableField("original_price_yuan")
	@ApiModelProperty(name = "originalPriceYuan", value = "剧本原价（元）")
	private BigDecimal originalPriceYuan;

	@TableField("offer_price_yuan")
	@ApiModelProperty(name = "offerPriceYuan", value = "购买/议价金额（元）")
	private BigDecimal offerPriceYuan;

	@TableField("contact_name")
	@ApiModelProperty(name = "contactName", value = "联系人")
	private String contactName;

	@TableField("contact_mobile_cipher")
	@ApiModelProperty(name = "contactMobileCipher", value = "联系电话密文")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@JSONField(serialize = false)
	private String contactMobileCipher;

	@TableField("contact_mobile_hash")
	@ApiModelProperty(name = "contactMobileHash", value = "联系电话哈希")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@JSONField(serialize = false)
	private String contactMobileHash;

	@TableField("message")
	@ApiModelProperty(name = "message", value = "留言/议价说明")
	private String message;

	@TableField("apply_status")
	@ApiModelProperty(name = "applyStatus", value = "0待处理 1已同意 2已拒绝 3已完成 4已取消 5已失效")
	private Integer applyStatus;

	@TableField("handle_remark")
	@ApiModelProperty(name = "handleRemark", value = "处理备注")
	private String handleRemark;

	@TableField("handle_time")
	@ApiModelProperty(name = "handleTime", value = "处理时间")
	private Date handleTime;

	@TableField("expire_time")
	@ApiModelProperty(name = "expireTime", value = "申请失效时间")
	private Date expireTime;

	@TableField("script_name_snapshot")
	@ApiModelProperty(name = "scriptNameSnapshot", value = "剧本名称快照")
	private String scriptNameSnapshot;

	@TableField("author_name_snapshot")
	@ApiModelProperty(name = "authorNameSnapshot", value = "编剧名称快照")
	private String authorNameSnapshot;

	@TableField("cover_key_snapshot")
	@ApiModelProperty(name = "coverKeySnapshot", value = "封面 key 快照")
	private String coverKeySnapshot;

	@TableField("license_scope_snapshot")
	@ApiModelProperty(name = "licenseScopeSnapshot", value = "授权范围快照")
	private String licenseScopeSnapshot;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
