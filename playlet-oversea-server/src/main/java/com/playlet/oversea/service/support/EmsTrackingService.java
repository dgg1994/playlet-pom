package com.playlet.oversea.service.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playlet.oversea.api.response.EmsTrackingInfoResp;
import com.playlet.oversea.api.response.EmsTrackingRegisterResp;
import com.playlet.oversea.config.ThirdPartyProperties;
import com.playlet.oversea.constants.EmsTrackingConstants;
import com.playlet.oversea.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 17track 物流注册与轨迹查询（对齐 worldpay HttpUtil.emsPost）。
 */
@Slf4j
@Service
public class EmsTrackingService {

	private static final Pattern TRACKING_PATTERN = Pattern.compile(EmsTrackingConstants.TRACKING_NUMBER_REGEX);

	@Autowired
	private ThirdPartyProperties thirdPartyProperties;
	@Autowired
	private ObjectMapper objectMapper;

	/** 校验物流单号格式 */
	public boolean isValidTrackingNumber(String trackingNumber) {
		return StringUtils.hasText(trackingNumber) && TRACKING_PATTERN.matcher(trackingNumber.trim()).matches();
	}

	/** 向 17track 注册物流单号 */
	public void registerTrackingNumber(String logisticsNum, String orderNo) {
		if (!StringUtils.hasText(thirdPartyProperties.getEmsApiKey())) {
			log.warn("ems api key missing, skip register logisticsNum={}", logisticsNum);
			return;
		}
		Map<String, Object> item = new HashMap<>(2);
		item.put("number", logisticsNum.trim());
		item.put("orderNo", orderNo);
		String body = toJson(Collections.singletonList(item));
		String resp = emsPost(EmsTrackingConstants.REGISTER_URL, body);
		if (!StringUtils.hasText(resp)) {
			throw new BaseException("物流单号注册失败");
		}
		EmsTrackingRegisterResp registerResp = parseJson(resp, EmsTrackingRegisterResp.class);
		if (registerResp != null && registerResp.getData() != null
				&& registerResp.getData().getRejected() != null
				&& !registerResp.getData().getRejected().isEmpty()) {
			EmsTrackingRegisterResp.RejectedItem rejected = registerResp.getData().getRejected().get(0);
			if (rejected.getError() != null && StringUtils.hasText(rejected.getError().getMessage())) {
				throw new BaseException(rejected.getError().getMessage());
			}
			throw new BaseException("物流单号注册被拒绝");
		}
		log.info("ems register success logisticsNum={}", logisticsNum);
	}

	/** 查询物流轨迹 */
	public EmsTrackingInfoResp queryTrackingInfo(String logisticsNum) {
		if (!StringUtils.hasText(thirdPartyProperties.getEmsApiKey())) {
			throw new BaseException("物流查询未配置");
		}
		Map<String, Object> item = new HashMap<>(1);
		item.put("number", logisticsNum.trim());
		String body = toJson(Collections.singletonList(item));
		String resp = emsPost(EmsTrackingConstants.TRACK_INFO_URL, body);
		if (!StringUtils.hasText(resp)) {
			throw new BaseException("物流查询失败");
		}
		return parseJson(resp, EmsTrackingInfoResp.class);
	}

	private String emsPost(String url, String paramStr) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty(EmsTrackingConstants.API_KEY_HEADER, thirdPartyProperties.getEmsApiKey());
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(30000);
			if (StringUtils.hasText(paramStr)) {
				try (OutputStream os = conn.getOutputStream()) {
					os.write(paramStr.getBytes(StandardCharsets.UTF_8));
				}
			}
			try (InputStream in = conn.getInputStream()) {
				return new String(in.readAllBytes(), StandardCharsets.UTF_8);
			}
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("ems post failed url={}", url, e);
			throw new BaseException("物流接口调用失败", e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private String toJson(Object body) {
		try {
			return objectMapper.writeValueAsString(body);
		} catch (Exception e) {
			throw new BaseException("物流请求序列化失败", e);
		}
	}

	private <T> T parseJson(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			log.error("ems response parse failed", e);
			throw new BaseException("物流响应解析失败", e);
		}
	}
}
