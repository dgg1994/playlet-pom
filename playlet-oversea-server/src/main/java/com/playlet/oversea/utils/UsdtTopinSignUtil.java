package com.playlet.oversea.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * USDT 监听网关签名（对齐 worldpay MonitorUtil.makeSign / Verification）。
 */
public final class UsdtTopinSignUtil {

	private UsdtTopinSignUtil() {
	}

	/** 生成出站签名 */
	@SuppressWarnings("unchecked")
	public static String makeSign(Object body, String secret) {
		if (body == null || StringUtils.isEmpty(secret)) {
			return "";
		}
		Map<String, Object> params = JSON.parseObject(JSON.toJSONString(body), Map.class);
		return makeSign(params, secret);
	}

	public static String makeSign(Map<String, Object> data, String secret) {
		if (data == null) {
			return "";
		}
		Map<String, Object> filteredData = new HashMap<>(data);
		filteredData.remove("sign");
		Map<String, Object> sortedData = recursiveSort(filteredData);
		String jsonStr = JSON.toJSONString(sortedData,
				SerializerFeature.DisableCircularReferenceDetect,
				SerializerFeature.WriteMapNullValue,
				SerializerFeature.BrowserCompatible);
		return hmacSha256(jsonStr, secret);
	}

	/** 校验回调签名 */
	public static boolean verifySign(Object body, String sign, String secret) {
		if (body == null || StringUtils.isEmpty(sign) || StringUtils.isEmpty(secret)) {
			return false;
		}
		String expected = makeSign(body, secret);
		return sign.equals(expected);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> recursiveSort(Map<String, Object> map) {
		Map<String, Object> sortedMap = new TreeMap<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map) {
				sortedMap.put(entry.getKey(), recursiveSort((Map<String, Object>) value));
			} else if (value instanceof List) {
				sortedMap.put(entry.getKey(), value);
			} else {
				sortedMap.put(entry.getKey(), value);
			}
		}
		return sortedMap;
	}

	private static String hmacSha256(String data, String secret) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			mac.init(secretKeySpec);
			byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(rawHmac);
		} catch (Exception e) {
			throw new IllegalStateException("HMAC-SHA256 encryption failed", e);
		}
	}

	private static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
