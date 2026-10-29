package com.playlet.internal.service.support.pay;

import com.playlet.internal.api.response.PayContractResult;
import com.playlet.internal.api.response.PayDeductResult;
import com.playlet.internal.config.PayProperties;
import com.playlet.internal.enums.PaySceneEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付「委托代扣 PAY AFTER PAY(papay)」。
 * <p>
 * wechatpay-java 0.2.x 未提供 papay 模块，此处直接按 V3 规范自行签名调用：
 * 预签约 / 查约 / 解约 / 代扣。接口路径以微信支付官方文档为准。
 */
@Slf4j
@Component
public class WechatPapaySupport {

	private static final String GATEWAY = "https://api.mch.weixin.qq.com";
	private static final String SIGN_TYPE = "SHA256withRSA";

	@Autowired
	private PayProperties payProperties;

	/** 渠道是否可用：总开关 + 微信开关 + plan_id 必填 */
	public boolean enabled() {
		PayProperties.Wechat wx = payProperties.getWechat();
		return Boolean.TRUE.equals(payProperties.getEnabled())
				&& wx != null && Boolean.TRUE.equals(wx.getEnabled())
				&& !StringUtils.isEmpty(wx.getPapayPlanId());
	}

	/**
	 * 预签约（App 场景）。返回端上调起签约的参数透传给客户端。
	 *
	 * @param scene           MINI/APP/JSAPI/H5
	 * @param externalNo      商户侧签约号（我们用首单号）
	 * @param displayAccount  用户展示的扣费描述
	 * @param openId          JSAPI/MINI 场景必填
	 */
	public PayContractResult contractSign(PaySceneEnums scene, String externalNo,
			String displayAccount, String openId) {
		PayProperties.Wechat wx = payProperties.getWechat();
		PayContractResult result = new PayContractResult();
		result.setChannel("WECHAT");
		result.setExternalAgreementNo(externalNo);

		String path = "/v3/papay/contracts/" + wx.getAppId() + "/prepay";
		Map<String, Object> body = new HashMap<>(8);
		body.put("appid", wx.getAppId());
		body.put("plan_id", wx.getPapayPlanId());
		body.put("contract_code", externalNo);
		body.put("contract_display_account", displayAccount);
		body.put("notify_url", StringUtils.isEmpty(wx.getContractNotifyUrl())
				? wx.getNotifyUrl() : wx.getContractNotifyUrl());
		if (!StringUtils.isEmpty(openId)) {
			body.put("openid", openId);
		}
		try {
			String resp = post(path, body);
			result.setSuccess(true);
			Map<String, Object> params = new HashMap<>(8);
			params.put("appid", wx.getAppId());
			params.put("planId", wx.getPapayPlanId());
			params.put("contractCode", externalNo);
			params.put("scene", scene == null ? PaySceneEnums.APP.getCode() : scene.getCode());
			params.put("prepayData", resp);
			result.setSignParams(params);
			log.info("wechat papay contract prepay ok externalNo={}", externalNo);
			return result;
		} catch (Exception e) {
			log.error("wechat papay contract prepay failed externalNo={}", externalNo, e);
			result.setSuccess(false);
			result.setErrMsg("微信签约失败：" + e.getMessage());
			return result;
		}
	}

	/** 查询签约状态；返回原始 JSON 中的 status 字段，未查到返回 null */
	public String contractQuery(String appId, String contractCode, String contractId) {
		StringBuilder path = new StringBuilder("/v3/papay/contracts/");
		if (!StringUtils.isEmpty(contractId)) {
			path.append("plan-id/").append(contractId);
		} else {
			path.append("plan-id/").append(payProperties.getWechat().getPapayPlanId())
					.append("/out-contract-code/").append(contractCode);
		}
		path.append("?appid=").append(appId == null ? payProperties.getWechat().getAppId() : appId);
		try {
			return get(path.toString());
		} catch (Exception e) {
			log.error("wechat papay contract query failed contractCode={}", contractCode, e);
			return null;
		}
	}

	/** 解约 */
	public boolean contractUnsign(String appId, String contractCode, String contractId, String reason) {
		StringBuilder path = new StringBuilder("/v3/papay/contracts/");
		if (!StringUtils.isEmpty(contractId)) {
			path.append("plan-id/").append(contractId);
		} else {
			path.append("plan-id/").append(payProperties.getWechat().getPapayPlanId())
					.append("/out-contract-code/").append(contractCode);
		}
		path.append("/terminate?appid=").append(appId == null ? payProperties.getWechat().getAppId() : appId);
		Map<String, Object> body = new HashMap<>(4);
		body.put("contract_termination_remark", reason == null ? "用户主动解约" : reason);
		try {
			post(path.toString(), body);
			log.info("wechat papay contract unsign ok contractCode={}", contractCode);
			return true;
		} catch (Exception e) {
			log.error("wechat papay contract unsign failed contractCode={}", contractCode, e);
			return false;
		}
	}

	/**
	 * 签约后发起代扣（扣款）。
	 *
	 * @param orderNo    商户代扣订单号
	 * @param contractId 三方协议号 contract_id
	 */
	public PayDeductResult contractPay(String orderNo, String contractId,
			BigDecimal amountYuan, String description) {
		PayProperties.Wechat wx = payProperties.getWechat();
		Map<String, Object> body = new HashMap<>(8);
		body.put("appid", wx.getAppId());
		body.put("mchid", wx.getMchId());
		body.put("out_trade_no", orderNo);
		body.put("contract_id", contractId);
		body.put("description", description);
		body.put("notify_url", StringUtils.isEmpty(wx.getDeductNotifyUrl())
				? wx.getNotifyUrl() : wx.getDeductNotifyUrl());
		Map<String, Object> amount = new HashMap<>(4);
		amount.put("total", amountYuan.multiply(BigDecimal.valueOf(100))
				.setScale(0, RoundingMode.HALF_UP).intValue());
		amount.put("currency", "CNY");
		body.put("amount", amount);
		try {
			String resp = post("/v3/papay/pay/transactions/contract", body);
			PayDeductResult result = new PayDeductResult();
			result.setChannel("WECHAT");
			result.setOrderNo(orderNo);
			result.setSuccess(true);
			result.setThirdTradeNo(extractString(resp, "transaction_id"));
			result.setTradeState(extractString(resp, "trade_state"));
			log.info("wechat papay deduct ok orderNo={} state={}", orderNo, result.getTradeState());
			return result;
		} catch (Exception e) {
			log.error("wechat papay deduct failed orderNo={}", orderNo, e);
			return PayDeductResult.fail("WECHAT", orderNo, "DEDUCT_FAIL", e.getMessage());
		}
	}

	// ---------------------------------------------------------------- HTTP

	private String post(String pathWithQuery, Map<String, Object> body) throws Exception {
		// body 转 JSON：值均为 String/Number/Boolean/Map，直接提取字符串
		String json = com.alibaba.fastjson.JSONObject.toJSONString(body);
		return request("POST", pathWithQuery, json);
	}

	private String get(String pathWithQuery) throws Exception {
		return request("GET", pathWithQuery, "");
	}

	private String request(String method, String pathWithQuery, String body) throws Exception {
		PayProperties.Wechat wx = payProperties.getWechat();
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		String nonce = UUID.randomUUID().toString().replace("-", "");
		String message = method + "\n" + pathWithQuery + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
		String signature = sign(message);
		String authorization = "WECHATPAY2-SHA256-RSA2048 mchid=\"" + wx.getMchId() + "\","
				+ "nonce_str=\"" + nonce + "\","
				+ "signature=\"" + signature + "\","
				+ "timestamp=\"" + timestamp + "\","
				+ "serial_no=\"" + wx.getMchSerialNo() + "\"";

		URL url = new URL(GATEWAY + pathWithQuery);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(10000);
		conn.setRequestProperty("Authorization", authorization);
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("User-Agent", "playlet-internal");
		if (!"GET".equals(method)) {
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			try (OutputStream os = conn.getOutputStream()) {
				os.write(body.getBytes(StandardCharsets.UTF_8));
			}
		}
		int code = conn.getResponseCode();
		InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
		String respBody = read(stream);
		if (code >= 400) {
			throw new BaseException("微信 V3 调用失败 httpCode=" + code + " body=" + respBody);
		}
		return respBody;
	}

	private String sign(String message) throws Exception {
		Signature signer = Signature.getInstance(SIGN_TYPE);
		signer.initSign(privateKey());
		signer.update(message.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(signer.sign());
	}

	private volatile PrivateKey privateKey;

	private PrivateKey privateKey() throws Exception {
		if (privateKey == null) {
			synchronized (this) {
				if (privateKey == null) {
					PayProperties.Wechat wx = payProperties.getWechat();
					String pem = StringUtils.isEmpty(wx.getPrivateKeyPath())
							? wx.getPrivateKey()
							: new String(Files.readAllBytes(Paths.get(wx.getPrivateKeyPath())), StandardCharsets.UTF_8);
					if (StringUtils.isEmpty(pem)) {
						throw new BaseException("微信支付私钥未配置");
					}
					String base64 = pem.replaceAll("-----BEGIN (.*?)-----", "")
							.replaceAll("-----END (.*?)-----", "")
							.replaceAll("\\s", "");
					PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64));
					privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
				}
			}
		}
		return privateKey;
	}

	private static String read(InputStream in) throws Exception {
		if (in == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		}
		return sb.toString();
	}

	private static String extractString(String json, String key) {
		if (StringUtils.isEmpty(json) || StringUtils.isEmpty(key)) {
			return null;
		}
		int idx = json.indexOf("\"" + key + "\"");
		if (idx < 0) {
			return null;
		}
		int colon = json.indexOf(':', idx);
		int start = json.indexOf('"', colon + 1);
		if (start < 0) {
			return null;
		}
		int end = json.indexOf('"', start + 1);
		return end < 0 ? null : json.substring(start + 1, end);
	}
}
