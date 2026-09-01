package com.playlet.internal.service.third;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playlet.internal.api.request.BankcardActiveRequest;
import com.playlet.internal.api.request.BankcardApplyRequest;
import com.playlet.internal.api.request.BankcardCanActiveRequest;
import com.playlet.internal.api.request.BankcardRechargeRequest;
import com.playlet.internal.api.request.BankcardSetPinRequest;
import com.playlet.internal.api.request.BankcardUpdateEmailRequest;
import com.playlet.internal.api.request.BankcardUpdateStatusRequest;
import com.playlet.internal.api.request.BankcardUserIdRequest;
import com.playlet.internal.api.request.KycApplyRequest;
import com.playlet.internal.api.request.KycCountryListRequest;
import com.playlet.internal.api.request.RegisterRequest;
import com.playlet.internal.api.response.KycCountryResp;
import com.playlet.internal.api.response.KycStatusResp;
import com.playlet.internal.api.response.WalletKycFileUploadResp;
import com.playlet.internal.api.response.ThirdBankcardActiveResp;
import com.playlet.internal.api.response.ThirdBankcardApplyResp;
import com.playlet.internal.api.response.ThirdBankcardBalanceResp;
import com.playlet.internal.api.response.ThirdBankcardCanActiveResp;
import com.playlet.internal.api.response.ThirdBankcardInfoResp;
import com.playlet.internal.api.response.ThirdBankcardPinResp;
import com.playlet.internal.api.response.ThirdBankcardProductResp;
import com.playlet.internal.api.response.ThirdUserBankcardResp;
import com.playlet.internal.api.response.ThirdUserRegisterResp;
import com.playlet.internal.config.ThirdPartyProperties;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.WalletApiPaths;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.KycFieldNormalizeUtil;
import com.playlet.internal.utils.RsaSignUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.REGISTER_PATH;
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
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.COUNTRY_PATH;
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
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.KYC_STATUS_PATH;
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
	 * KYC 证件文件上传。文档：POST /api/file/upload，multipart 字段 idCard；签名仅含 appId/nonce/timestamp。
	 *
	 * @param uid    worldPay 用户 uid
	 * @param idCard 证件图片
	 * @return 文件 url
	 */
	public WalletKycFileUploadResp uploadKycFile(Long uid, MultipartFile idCard) {
		requireUid(uid);
		validateKycUploadFile(idCard);
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.FILE_UPLOAD_PATH;
		log.info("third party kyc file upload start uid={} fileName={} size={}",
				uid, idCard.getOriginalFilename(), idCard.getSize());
		try {
			byte[] fileBytes = idCard.getBytes();
			String originalFilename = idCard.getOriginalFilename();
			JsonNode data = exchangeMultipart(url, fileBytes, originalFilename,
					String.valueOf(uid), "KYC文件上传");
			WalletKycFileUploadResp resp = treeToValue(data, WalletKycFileUploadResp.class, "KYC文件上传");
			if (resp == null || StringUtils.isEmpty(resp.getFileUrl())) {
				throw new BaseException("KYC文件上传响应缺少 fileUrl");
			}
			log.info("third party kyc file upload success uid={}", uid);
			return resp;
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("third party kyc file upload failed uid={}", uid, e);
			throw new BaseException("KYC文件上传失败", e);
		}
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
		KycFieldNormalizeUtil.normalizeKycApply(body);
		validateKycApply(body);
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.KYC_APPLY_PATH;
		log.info("third party kyc apply start uid={} nationCode={} countryCode={}",
				uid, body.getNationCode(), body.getCountryCode());
		exchange(HttpMethod.POST, url, body, String.valueOf(uid), "提交KYC信息");
		log.info("third party kyc apply success uid={}", uid);
	}

	/**
	 * 商户可用卡产品列表。文档：GET /api/bankcard/merchant/card/list
	 */
	public List<ThirdBankcardProductResp> listCardProducts() {
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_PRODUCT_LIST_PATH;
		log.info("third party card product list start");
		JsonNode data = exchange(HttpMethod.GET, url, null, null, "卡产品列表");
		return parseList(data, ThirdBankcardProductResp.class);
	}

	/**
	 * 用户卡列表。文档：GET /api/bankcard/user/card/list，uid 放 Header。
	 *
	 */
	public List<ThirdUserBankcardResp> listUserCards(Long uid) {
		requireUid(uid);
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_USER_LIST_PATH;
		log.info("third party user card list start uid={}", uid);
		JsonNode data = exchange(HttpMethod.GET, url, null, String.valueOf(uid), "用户卡列表");
		List<ThirdUserBankcardResp> list = parseList(data, ThirdUserBankcardResp.class);
		log.info("third party user card list success uid={} size={}", uid, list.size());
		return list;
	}

	/**
	 * 申请银行卡。文档：POST /api/bankcard/apply，uid 放 Header。
	 */
	public ThirdBankcardApplyResp applyBankcard(Long uid, BankcardApplyRequest body) {
		requireUid(uid);
		if (body == null || body.getProductId() == null) {
			throw new BaseException("productId不能为空");
		}
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_APPLY_PATH;
		log.info("third party card apply start uid={} productId={}", uid, body.getProductId());
		JsonNode data = exchange(HttpMethod.POST, url, body, String.valueOf(uid), "申请银行卡");
		ThirdBankcardApplyResp resp = treeToValue(data, ThirdBankcardApplyResp.class, "申请银行卡");
		if (resp.getUserBankcardId() == null) {
			throw new BaseException("申请银行卡响应缺少 userBankcardId");
		}
		log.info("third party card apply success uid={} userBankcardId={} orderNo={}",
				uid, resp.getUserBankcardId(), resp.getOrderNo());
		return resp;
	}

	/**
	 * 银行卡是否可激活。文档：POST /api/bankcard/get/canActive
	 */
	public ThirdBankcardCanActiveResp canActiveBankcard(Long uid, BankcardCanActiveRequest body) {
		requireUid(uid);
		if (body == null || StringUtils.isEmpty(body.getCardNo()) || StringUtils.isEmpty(body.getVerifyCode())) {
			throw new BaseException("cardNo/verifyCode不能为空");
		}
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_CAN_ACTIVE_PATH;
		log.info("third party card canActive start uid={}", uid);
		JsonNode data = exchange(HttpMethod.POST, url, body, String.valueOf(uid), "银行卡是否可激活");
		return treeToValue(data, ThirdBankcardCanActiveResp.class, "银行卡是否可激活");
	}

	/**
	 * 银行卡激活。文档：POST /api/bankcard/active
	 */
	public ThirdBankcardActiveResp activeBankcard(Long uid, BankcardActiveRequest body) {
		requireUid(uid);
		validateActive(body);
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_ACTIVE_PATH;
		log.info("third party card active start uid={} productId={}", uid, body.getProductId());
		JsonNode data = exchange(HttpMethod.POST, url, body, String.valueOf(uid), "银行卡激活");
		ThirdBankcardActiveResp resp = treeToValue(data, ThirdBankcardActiveResp.class, "银行卡激活");
		log.info("third party card active success uid={} userBankcardId={}", uid, resp.getUserBankcardId());
		return resp;
	}

	/**
	 * 设置 Pin。文档：POST /api/bankcard/setPin（不打印 pin）
	 */
	public void setBankcardPin(Long uid, BankcardSetPinRequest body) {
		requireUid(uid);
		if (body == null || body.getUserBankcardId() == null || StringUtils.isEmpty(body.getPin())) {
			throw new BaseException("userBankcardId/pin不能为空");
		}
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_SET_PIN_PATH;
		log.info("third party card setPin start uid={} userBankcardId={}", uid, body.getUserBankcardId());
		// BankcardSetPinRequest.pin 为 WRITE_ONLY，不能直接作为出站 body 序列化
		Map<String, Object> thirdBody = new LinkedHashMap<>();
		thirdBody.put("userBankcardId", body.getUserBankcardId());
		thirdBody.put("pin", body.getPin());
		exchange(HttpMethod.POST, url, thirdBody, String.valueOf(uid), "设置Pin");
		log.info("third party card setPin success uid={} userBankcardId={}", uid, body.getUserBankcardId());
	}

	/**
	 * 查询银行卡余额。文档：POST /api/bankcard/getBalance
	 */
	public ThirdBankcardBalanceResp getBankcardBalance(Long uid, Long userBankcardId) {
		requireUid(uid);
		BankcardUserIdRequest body = requireUserBankcardId(userBankcardId);
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_GET_BALANCE_PATH;
		log.info("third party card balance start uid={} userBankcardId={}", uid, userBankcardId);
		JsonNode data = exchange(HttpMethod.POST, url, body, String.valueOf(uid), "查询银行卡余额");
		return treeToValue(data, ThirdBankcardBalanceResp.class, "查询银行卡余额");
	}

	/**
	 * 银行卡充值。文档：POST /api/bankcard/recharge
	 */
	public void rechargeBankcard(Long uid, BankcardRechargeRequest body) {
		requireUid(uid);
		if (body == null || body.getUserBankcardId() == null || body.getAmount() == null
				|| StringUtils.isEmpty(body.getRequestOrderId())) {
			throw new BaseException("userBankcardId/amount/requestOrderId不能为空");
		}
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_RECHARGE_PATH;
		log.info("third party card recharge start uid={} userBankcardId={} amount={} requestOrderId={}",
				uid, body.getUserBankcardId(), body.getAmount(), body.getRequestOrderId());
		exchange(HttpMethod.POST, url, body, String.valueOf(uid), "银行卡充值");
		log.info("third party card recharge success uid={} requestOrderId={}", uid, body.getRequestOrderId());
	}

	/**
	 * 更新银行卡状态（冻结/解冻）。文档：POST /api/bankcard/update/status
	 */
	public void updateBankcardStatus(Long uid, BankcardUpdateStatusRequest body) {
		requireUid(uid);
		if (body == null || body.getUserBankcardId() == null || body.getEnable() == null) {
			throw new BaseException("userBankcardId/enable不能为空");
		}
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_UPDATE_STATUS_PATH;
		log.info("third party card updateStatus start uid={} userBankcardId={} enable={}",
				uid, body.getUserBankcardId(), body.getEnable());
		exchange(HttpMethod.POST, url, body, String.valueOf(uid), "更新银行卡状态");
		log.info("third party card updateStatus success uid={} userBankcardId={}", uid, body.getUserBankcardId());
	}

	/**
	 * 注销银行卡。文档：POST /api/bankcard/close
	 */
	public void closeBankcard(Long uid, Long userBankcardId) {
		requireUid(uid);
		BankcardUserIdRequest body = requireUserBankcardId(userBankcardId);
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_CLOSE_PATH;
		log.info("third party card close start uid={} userBankcardId={}", uid, userBankcardId);
		exchange(HttpMethod.POST, url, body, String.valueOf(uid), "注销银行卡");
		log.info("third party card close success uid={} userBankcardId={}", uid, userBankcardId);
	}

	/**
	 * 查询银行卡信息。文档：POST /api/bankcard/info（不打印 cvv）
	 */
	public ThirdBankcardInfoResp getBankcardInfo(Long uid, Long userBankcardId) {
		requireUid(uid);
		BankcardUserIdRequest body = requireUserBankcardId(userBankcardId);
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_INFO_PATH;
		log.info("third party card info start uid={} userBankcardId={}", uid, userBankcardId);
		JsonNode data = exchange(HttpMethod.POST, url, body, String.valueOf(uid), "查询银行卡信息");
		ThirdBankcardInfoResp resp = treeToValue(data, ThirdBankcardInfoResp.class, "查询银行卡信息");
		log.info("third party card info success uid={} userBankcardId={} infoType={} status={}",
				uid, userBankcardId, resp.getInfoType(), resp.getStatus());
		return resp;
	}

	/**
	 * 更新银行卡邮箱。文档：POST /api/bankcard/update/email
	 */
	public void updateBankcardEmail(Long uid, BankcardUpdateEmailRequest body) {
		requireUid(uid);
		if (body == null || body.getUserBankcardId() == null || StringUtils.isEmpty(body.getEmail())) {
			throw new BaseException("userBankcardId/email不能为空");
		}
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_UPDATE_EMAIL_PATH;
		log.info("third party card updateEmail start uid={} userBankcardId={}", uid, body.getUserBankcardId());
		exchange(HttpMethod.POST, url, body, String.valueOf(uid), "更新银行卡邮箱");
		log.info("third party card updateEmail success uid={} userBankcardId={}", uid, body.getUserBankcardId());
	}

	/**
	 * 查询 Pin。文档：POST /api/bankcard/queryPin（不打印 pin）
	 */
	public ThirdBankcardPinResp queryBankcardPin(Long uid, Long userBankcardId) {
		requireUid(uid);
		BankcardUserIdRequest body = requireUserBankcardId(userBankcardId);
		String url = thirdPartyProperties.getBaseUrl() + WalletApiPaths.CARD_QUERY_PIN_PATH;
		log.info("third party card queryPin start uid={} userBankcardId={}", uid, userBankcardId);
		JsonNode data = exchange(HttpMethod.POST, url, body, String.valueOf(uid), "查询Pin");
		ThirdBankcardPinResp resp = treeToValue(data, ThirdBankcardPinResp.class, "查询Pin");
		log.info("third party card queryPin success uid={} userBankcardId={}", uid, userBankcardId);
		return resp;
	}

	/** 校验上传文件：非空 + 后缀白名单（对齐 worldpayPolymeric） */
	private static void validateKycUploadFile(MultipartFile idCard) {
		if (idCard == null || idCard.isEmpty()) {
			throw new BaseException("请上传证件文件");
		}
		String fileName = idCard.getOriginalFilename();
		if (StringUtils.isEmpty(fileName)) {
			throw new BaseException("文件名不能为空");
		}
		String lowerName = fileName.trim().toLowerCase();
		boolean valid = Arrays.stream(WalletConstants.KYC_UPLOAD_ALLOWED_SUFFIXES)
				.anyMatch(lowerName::endsWith);
		if (!valid) {
			throw new BaseException("仅支持 png/jpg/jpeg/pdf 格式");
		}
	}

	/** 校验 KYC 必填字段（与文档 required 对齐；身份证/驾照需反面照） */
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
		// 1身份证 / 3驾照：文档要求正面+背面
		boolean needBack = body.getCertType() == WalletConstants.KYC_CERT_ID_CARD
				|| body.getCertType() == WalletConstants.KYC_CERT_DRIVER_LICENSE;
		if (needBack && StringUtils.isEmpty(body.getIdBackUrl())) {
			throw new BaseException("KYC证件反面照不能为空");
		}
	}

	private void validateActive(BankcardActiveRequest body) {
		if (body == null
				|| body.getProductId() == null
				|| StringUtils.isEmpty(body.getCardNo())
				|| StringUtils.isEmpty(body.getMobilePrefix())
				|| StringUtils.isEmpty(body.getMobile())
				|| StringUtils.isEmpty(body.getCountryCode())
				|| StringUtils.isEmpty(body.getAddress())
				|| StringUtils.isEmpty(body.getCity())
				|| StringUtils.isEmpty(body.getState())
				|| StringUtils.isEmpty(body.getPostCode())) {
			throw new BaseException("银行卡激活必填字段不完整");
		}
	}

	private static void requireUid(Long uid) {
		if (uid == null) {
			throw new BaseException("uid不能为空");
		}
	}

	private static BankcardUserIdRequest requireUserBankcardId(Long userBankcardId) {
		if (userBankcardId == null) {
			throw new BaseException("userBankcardId不能为空");
		}
		BankcardUserIdRequest body = new BankcardUserIdRequest();
		body.setUserBankcardId(userBankcardId);
		return body;
	}

	private <T> List<T> parseList(JsonNode data, Class<T> elementType) {
		if (data == null || data.isNull()) {
			return Collections.emptyList();
		}
		try {
			return objectMapper.convertValue(data,
					objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
		} catch (Exception e) {
			log.error("third party parse list failed type={}", elementType.getSimpleName(), e);
			throw new BaseException("三方响应解析失败", e);
		}
	}

	/**
	 * multipart 上传：签名不含 body 字段。
	 */
	private JsonNode exchangeMultipart(String url, byte[] fileBytes, String originalFilename,
			String uidHeader, String bizName) throws Exception {
		if (StringUtils.isEmpty(thirdPartyProperties.getBaseUrl())
				|| StringUtils.isEmpty(thirdPartyProperties.getAppId())
				|| StringUtils.isEmpty(thirdPartyProperties.getPrivateKey())) {
			throw new BaseException("third-party 配置未完整");
		}
		String appId = thirdPartyProperties.getAppId();
		String nonce = UUID.randomUUID().toString().replace("-", "");
		String timestamp = String.valueOf(System.currentTimeMillis());
		String signContent = RsaSignUtil.buildSignContent(appId, nonce, timestamp, null);
		log.info("third party {} signContent={}", bizName, signContent);
		String sign = RsaSignUtil.generateSign(appId, nonce, timestamp, null,
				thirdPartyProperties.getPrivateKey());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.set(HEADER_APP_ID, appId);
		headers.set(HEADER_NONCE, nonce);
		headers.set(HEADER_TIMESTAMP, timestamp);
		headers.set(HEADER_SIGN, sign);
		if (!StringUtils.isEmpty(uidHeader)) {
			headers.set(HEADER_UID, uidHeader);
		}

		ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
			@Override
			public String getFilename() {
				return originalFilename;
			}
		};
		MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
		multipartBody.add(WalletConstants.KYC_UPLOAD_FIELD_ID_CARD, fileResource);
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(multipartBody, headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			log.error("third party {} http failed status={} url={}", bizName, response.getStatusCode(), url);
			throw new BaseException(bizName + "失败");
		}
		return parseBizData(response.getBody(), bizName);
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
			// pin/cvv 等敏感字段不落签名原文日志
			if (containsSensitiveSignField(signContent)) {
				log.info("third party {} signContent=[redacted]", bizName);
			} else {
				log.info("third party {} signContent={}", bizName, signContent);
			}
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

	private static boolean containsSensitiveSignField(String signContent) {
		if (StringUtils.isEmpty(signContent)) {
			return false;
		}
		String lower = signContent.toLowerCase();
		return lower.contains("pin=") || lower.contains("cvv=") || lower.contains("verifycode=");
	}
}
