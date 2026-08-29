package com.playlet.internal.service.third;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.playlet.internal.api.request.UsdtTopinGetAddressRequest;
import com.playlet.internal.api.response.Web3AddressCreateResp;
import com.playlet.internal.config.UsdtTopinProperties;
import com.playlet.internal.constants.UsdtTopinApiPaths;
import com.playlet.internal.constants.UsdtTopinConstants;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.UsdtTopinSignUtil;
import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * USDT 链上监听网关 HTTP 客户端（对齐 worldpay MonitorUtil.walletPost）。
 */
@Slf4j
@Component
public class UsdtTopinClient {

	@Autowired
	private UsdtTopinProperties usdtTopinProperties;

	/** 向三方申请多链充值地址 */
	public Web3AddressCreateResp createAccount(String walletUid, String email) {
		if (StringUtils.isEmpty(walletUid) || StringUtils.isEmpty(email)) {
			throw new BaseException("walletUid/email不能为空");
		}
		UsdtTopinGetAddressRequest body = new UsdtTopinGetAddressRequest();
		body.setUid(walletUid.trim());
		body.setEmail(email.trim());
		JSONObject data = walletPost(UsdtTopinApiPaths.GET_ADDRESS, body);
		Web3AddressCreateResp resp = parseWeb3Address(data);
		if (StringUtils.isEmpty(resp.getTronAddress()) && StringUtils.isEmpty(resp.getBnbAddress())) {
			throw new BaseException("USDT充值地址为空");
		}
		return resp;
	}

	/** 校验回调签名 */
	public boolean verifySign(Object payload, String sign) {
		return UsdtTopinSignUtil.verifySign(payload, sign, usdtTopinProperties.getSignKey());
	}

	/** 对齐 MonitorUtil.walletPost */
	private JSONObject walletPost(String method, Object body) {
		String gatewayUrl = usdtTopinProperties.getGatewayUrl();
		String apiKey = usdtTopinProperties.getApiKey();
		String signKey = usdtTopinProperties.getSignKey();
		if (StringUtils.isEmpty(gatewayUrl)) {
			throw new BaseException("usdt-topin.gateway-url未配置");
		}
		if (StringUtils.isEmpty(apiKey)) {
			throw new BaseException("usdt-topin.api-key未配置");
		}
		if (StringUtils.isEmpty(signKey)) {
			throw new BaseException("usdt-topin.sign-key未配置");
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> params = JSON.parseObject(JSON.toJSONString(body), Map.class);
		String sign = UsdtTopinSignUtil.makeSign(params, signKey);
		Map<String, Object> requestParams = new HashMap<>(params);
		requestParams.put("sign", sign);
		String requestJson = JSON.toJSONString(requestParams);
		String url = trimTrailingSlash(gatewayUrl) + method;
		try {
			log.info("usdt topin request url={} uid={}", url, params.get("uid"));
			String responseBody = HttpRequest.post(url)
					.timeout(usdtTopinProperties.getReadTimeoutMs())
					.setConnectionTimeout(usdtTopinProperties.getConnectTimeoutMs())
					.header("Content-Type", UsdtTopinConstants.CONTENT_TYPE_JSON)
					.header(UsdtTopinConstants.HEADER_API_KEY, apiKey)
					.body(requestJson)
					.charset(StandardCharsets.UTF_8)
					.execute()
					.body();
			if (!StringUtils.isEmpty(responseBody) && responseBody.trim().startsWith("<")) {
				log.error("usdt topin blocked by gateway html url={}", url);
				throw new BaseException("USDT网关被安全策略拦截，请联系运维加白名单");
			}
			JSONObject root = JSON.parseObject(responseBody);
			if (root == null) {
				throw new BaseException("USDT网关响应为空");
			}
			Integer code = root.getInteger("code");
			if (code == null || code != UsdtTopinConstants.SUCCESS_CODE) {
				String msg = root.getString("msg");
				log.warn("usdt topin rejected url={} code={} msg={}", url, code, msg);
				throw new BaseException(StringUtils.isEmpty(msg) ? "USDT网关拒绝" : msg);
			}
			return root.getJSONObject("data");
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("usdt topin http failed url={}", url, e);
			throw new BaseException("USDT网关调用失败", e);
		}
	}

	private Web3AddressCreateResp parseWeb3Address(JSONObject data) {
		if (data == null) {
			return new Web3AddressCreateResp();
		}
		// 与 worldpay 一致：data 整体反序列化为 Web3AddressCreateResp
		Web3AddressCreateResp resp = data.toJavaObject(Web3AddressCreateResp.class);
		if (resp == null) {
			resp = new Web3AddressCreateResp();
		}
		if (StringUtils.isEmpty(resp.getTronAddress())) {
			resp.setTronAddress(firstNonEmpty(
					data.getString("tronAddress"),
					nestedAddress(data, "tron"),
					data.getString("address")));
		}
		if (StringUtils.isEmpty(resp.getBnbAddress())) {
			resp.setBnbAddress(firstNonEmpty(data.getString("bnbAddress"), nestedAddress(data, "bnb")));
		}
		if (StringUtils.isEmpty(resp.getEthAddress())) {
			resp.setEthAddress(firstNonEmpty(
					data.getString("ethAddress"),
					data.getString("evmAddress"),
					nestedAddress(data, "eth")));
		}
		if (StringUtils.isEmpty(resp.getBtcAddress())) {
			resp.setBtcAddress(firstNonEmpty(data.getString("btcAddress"), nestedAddress(data, "btc")));
		}
		return resp;
	}

	private static String nestedAddress(JSONObject data, String chain) {
		JSONObject address = data.getJSONObject("address");
		if (address == null) {
			return null;
		}
		return address.getString(chain);
	}

	private static String firstNonEmpty(String... values) {
		if (values == null) {
			return null;
		}
		for (String value : values) {
			if (!StringUtils.isEmpty(value)) {
				return value.trim();
			}
		}
		return null;
	}

	private static String trimTrailingSlash(String url) {
		if (url == null) {
			return "";
		}
		if (url.endsWith("/")) {
			return url.substring(0, url.length() - 1);
		}
		return url;
	}
}
