package com.playlet.internal.service.third;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playlet.internal.api.request.KycApplyRequest;
import com.playlet.internal.api.request.KycCountryListRequest;
import com.playlet.internal.api.request.RegisterRequest;
import com.playlet.internal.api.response.KycCountryResp;
import com.playlet.internal.api.response.KycStatusResp;
import com.playlet.internal.api.response.ThirdUserRegisterResp;
import com.playlet.internal.config.ThirdPartyProperties;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.OnePayApiPaths;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.RsaSignUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * worldPay 等三方 HTTP 调用。
 */
@Slf4j
@Service
public class ThirdService {

	private static final String HEADER_APP_ID = "appId";
	private static final String HEADER_NONCE = "nonce";
	private static final String HEADER_TIMESTAMP = "timestamp";
	private static final String HEADER_SIGN = "sign";
	private static final String HEADER_UID = "uid";

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ThirdPartyProperties thirdPartyProperties;
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 调用 worldPay 用户注册：鉴权参数放 Header，Body 仅 email/tel。
	 *
	 * @param email 用户邮箱（必填）
	 * @param tel   手机号（可选）
	 * @return worldPay uid
	 */
	public Long registerUser(String email, String tel) {
		if (StringUtils.isEmpty(email)) {
			throw new BaseException("email不能为空");
		}
		RegisterRequest body = new RegisterRequest();
		body.setEmail(email.trim());
		if (!StringUtils.isEmpty(tel)) {
			body.setTel(tel.trim());
		}
		String url = thirdPartyProperties.getBaseUrl() + OnePayApiPaths.REGISTER_PATH;
		log.info("third party register start email={}", maskEmail(email));
		JsonNode data = exchange(HttpMethod.POST, url, body, null, "三方用户注册");
		ThirdUserRegisterResp resp = treeToValue(data, ThirdUserRegisterResp.class, "三方用户注册");
		if (resp == null || resp.getUid() == null) {
			throw new BaseException("三方用户注册响应 uid 为空");
		}
		log.info("third party register success email={} uid={}", maskEmail(email), resp.getUid());
		return resp.getUid();
	}

	/**
	 * KYC 国家列表。文档：POST /api/user/kyc/country/list
	 *
	 * @param name 国家名称，空则查全部
	 * @return 国家列表
	 */
	public List<KycCountryResp> listKycCountries(String name) {
		KycCountryListRequest body = new KycCountryListRequest();
		if (!StringUtils.isEmpty(name)) {
			body.setName(name.trim());
		}
		String url = thirdPartyProperties.getBaseUrl() + OnePayApiPaths.COUNTRY_PATH;
		log.info("third party kyc country list start name={}", StringUtils.isEmpty(name) ? "ALL" : name.trim());
		JsonNode data = exchange(HttpMethod.POST, url, body, null, "KYC国家列表");
		if (data == null || data.isNull()) {
			return Collections.emptyList();
		}
		List<KycCountryResp> list = objectMapper.convertValue(data, new TypeReference<List<KycCountryResp>>() {
		});
		return list == null ? Collections.emptyList() : list;
	}

	/**
	 * 查询 KYC 状态。文档：GET /api/user/kyc/status，uid 放 Header。
	 *
	 * @param uid worldPay 用户 uid
	 * @return KYC 状态
	 */
	public KycStatusResp getKycStatus(Long uid) {
		if (uid == null) {
			throw new BaseException("uid不能为空");
		}
		String url = thirdPartyProperties.getBaseUrl() + OnePayApiPaths.KYC_STATUS_PATH;
		log.info("third party kyc status start uid={}", uid);
		// GET 无 body，签名仅含 appId/nonce/timestamp
		JsonNode data = exchange(HttpMethod.GET, url, null, String.valueOf(uid), "查询KYC状态");
		KycStatusResp resp = treeToValue(data, KycStatusResp.class, "查询KYC状态");
		if (resp == null || StringUtils.isEmpty(resp.getStatus())) {
			throw new BaseException("查询KYC状态响应缺少 status");
		}
		log.info("third party kyc status success uid={} status={}", uid, resp.getStatus());
		return resp;
	}

	/**
	 * 提交 KYC 信息。文档：POST /api/user/kyc/apply，uid 放 Header。
	 *
	 * @param uid  worldPay 用户 uid
	 * @param body KYC 业务字段
	 */
	public void applyKyc(Long uid, KycApplyRequest body) {
		if (uid == null) {
			throw new BaseException("uid不能为空");
		}
		if (body == null) {
			throw new BaseException("KYC提交参数不能为空");
		}
		validateKycApply(body);
		String url = thirdPartyProperties.getBaseUrl() + OnePayApiPaths.KYC_APPLY_PATH;
		log.info("third party kyc apply start uid={} nationCode={} countryCode={}",
				uid, body.getNationCode(), body.getCountryCode());
		exchange(HttpMethod.POST, url, body, String.valueOf(uid), "提交KYC信息");
		log.info("third party kyc apply success uid={}", uid);
	}

	/** 校验 KYC 必填字段（与文档 required 对齐） */
	private void validateKycApply(KycApplyRequest body) {
		if (StringUtils.isEmpty(body.getFirstName())
				|| StringUtils.isEmpty(body.getLastName())
				|| StringUtils.isEmpty(body.getIdNo())
				|| StringUtils.isEmpty(body.getEmail())
				|| StringUtils.isEmpty(body.getNationCode())
				|| body.getCertType() == null
				|| StringUtils.isEmpty(body.getIdUrl())
				|| StringUtils.isEmpty(body.getBirthday())
				|| StringUtils.isEmpty(body.getCountryCode())
				|| StringUtils.isEmpty(body.getAreaCode())
				|| StringUtils.isEmpty(body.getPhone())) {
			throw new BaseException("KYC必填字段不完整");
		}
	}

	/**
	 * 统一签名 + 发起请求，解析业务响应 data。
	 *
	 * @param method    HTTP 方法
	 * @param url       完整 URL
	 * @param body      请求体，GET 可为 null
	 * @param uidHeader 用户 uid（部分接口需要），可为 null
	 * @param bizName   业务名，用于日志与异常文案
	 */
	private JsonNode exchange(HttpMethod method, String url, Object body, String uidHeader, String bizName) {
		if (StringUtils.isEmpty(thirdPartyProperties.getBaseUrl())
				|| StringUtils.isEmpty(thirdPartyProperties.getAppId())
				|| StringUtils.isEmpty(thirdPartyProperties.getPrivateKey())) {
			throw new BaseException("third-party 配置未完整");
		}
		String appId = thirdPartyProperties.getAppId();
		String nonce = UUID.randomUUID().toString().replace("-", "");
		String timestamp = String.valueOf(System.currentTimeMillis());
		try {
			// 先打签名原文，便于核对是否含 timestamp&email 拼接
			String signContent = RsaSignUtil.buildSignContent(appId, nonce, timestamp, body);
			log.info("third party {} signContent={}", bizName, signContent);
			String sign = RsaSignUtil.generateSign(appId, nonce, timestamp, body,
					thirdPartyProperties.getPrivateKey());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(HEADER_APP_ID, appId);
			headers.set(HEADER_NONCE, nonce);
			headers.set(HEADER_TIMESTAMP, timestamp);
			headers.set(HEADER_SIGN, sign);
			if (!StringUtils.isEmpty(uidHeader)) {
				headers.set(HEADER_UID, uidHeader);
			}
			HttpEntity<?> entity = new HttpEntity<>(body, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
			if (response.getStatusCode() != HttpStatus.OK) {
				log.error("third party {} http failed status={} url={}", bizName, response.getStatusCode(), url);
				throw new BaseException(bizName + "失败");
			}
			return parseBizData(response.getBody(), bizName);
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("third party {} failed url={}", bizName, url, e);
			throw new BaseException(bizName + "失败", e);
		}
	}

	private JsonNode parseBizData(String responseBody, String bizName) throws Exception {
		if (StringUtils.isEmpty(responseBody)) {
			throw new BaseException(bizName + "响应为空");
		}
		JsonNode root = objectMapper.readTree(responseBody);
		JsonNode codeNode = root.get("code");
		if (codeNode == null || codeNode.isNull()
				|| codeNode.asInt() != Constants.HTTP_RES_CODE_200) {
			String msg = root.has("msg") ? root.get("msg").asText() : "unknown";
			log.warn("third party {} biz failed code={} msg={}", bizName,
					codeNode == null ? null : codeNode.asInt(), msg);
			throw new BaseException(bizName + "失败: " + msg);
		}
		return root.get("data");
	}

	private <T> T treeToValue(JsonNode data, Class<T> type, String bizName) {
		if (data == null || data.isNull()) {
			throw new BaseException(bizName + "响应缺少 data");
		}
		try {
			return objectMapper.treeToValue(data, type);
		} catch (Exception e) {
			log.error("third party {} parse data failed", bizName, e);
			throw new BaseException(bizName + "响应解析失败", e);
		}
	}

	private static String maskEmail(String email) {
		if (StringUtils.isEmpty(email) || !email.contains("@")) {
			return "***";
		}
		int at = email.indexOf('@');
		String name = email.substring(0, at);
		String domain = email.substring(at);
		if (name.length() <= 1) {
			return "*" + domain;
		}
		return name.charAt(0) + "***" + domain;
	}
}
