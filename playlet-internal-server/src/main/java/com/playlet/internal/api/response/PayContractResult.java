package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * 三方代扣签约结果（连续包月）。
 */
@Data
@ApiModel("签约结果")
public class PayContractResult {

	@ApiModelProperty("渠道 WECHAT/ALIPAY")
	private String channel;

	@ApiModelProperty("是否成功拉起签约")
	private boolean success;

	@ApiModelProperty("商户侧签约号")
	private String externalAgreementNo;

	@ApiModelProperty("三方协议号；支付宝签约后立即返回部分场景才有，微信在回调中返回")
	private String agreementNo;

	@ApiModelProperty("签约页面 form（支付宝 PC/H5：直接渲染自动提交表单）")
	private String signForm;

	@ApiModelProperty("App 拉起签约串（支付宝 APP 场景返回 sdk body）")
	private String signUrl;

	@ApiModelProperty("端上拉起签约所需参数（微信各场景）")
	private Map<String, Object> signParams;

	@ApiModelProperty("三方错误码")
	private String errCode;

	@ApiModelProperty("三方错误描述")
	private String errMsg;

	public static PayContractResult fail(String channel, String externalAgreementNo, String errMsg) {
		PayContractResult result = new PayContractResult();
		result.setChannel(channel);
		result.setExternalAgreementNo(externalAgreementNo);
		result.setSuccess(false);
		result.setErrMsg(errMsg);
		return result;
	}
}
