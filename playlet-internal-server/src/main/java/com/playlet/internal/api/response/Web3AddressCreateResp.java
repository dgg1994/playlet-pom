package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 三方 /v1/createaccount 返回的 Web3 地址集合。
 */
@Data
@ApiModel(value = "Web3地址创建结果", description = "三方 createaccount data")
public class Web3AddressCreateResp {

	private String tronAddress;

	private String bnbAddress;

	private String ethAddress;

	private String btcAddress;
}
