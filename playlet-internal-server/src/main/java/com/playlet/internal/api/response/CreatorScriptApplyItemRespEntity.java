package com.playlet.internal.api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易申请 DAO 行（含手机号密文，出参前脱敏）。
 */
@Data
@ApiModel(value = "交易申请DAO行")
public class CreatorScriptApplyItemRespEntity {

	private Long applyId;
	private String applyNo;
	private Integer scriptId;
	private String scriptName;
	private String buyerName;
	private String contactName;
	private Integer applyType;
	private BigDecimal offerPrice;
	private String message;
	private Integer applyStatus;
	private Date setTime;

	@JsonIgnore
	private String contactMobileCipher;
}
