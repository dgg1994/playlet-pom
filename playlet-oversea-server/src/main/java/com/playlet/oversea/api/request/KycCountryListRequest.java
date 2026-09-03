package com.playlet.oversea.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * worldPay KYC 国家列表请求体。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "KYC国家列表", description = "POST /api/user/kyc/country/list body")
public class KycCountryListRequest {

	@ApiModelProperty(value = "国家名称，不填返回全部")
	private String name;
}
