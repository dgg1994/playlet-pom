package com.playlet.internal.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.playlet.internal.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信（国内通道）。
 *
 * <p>使用已报备的签名 + 验证码模板发送，手机号须为 11 位国内号码（不带 +86）。
 */
@Slf4j
@Component
public class AliyunSmsUtil {

	private static final String OK = "OK";

	@Autowired
	private SmsProperties smsProperties;

	private volatile IAcsClient client;

	/** 阿里云通道是否已配置齐全（auto 模式下未配置可回退国际通道） */
	public boolean isConfigured() {
		SmsProperties.Aliyun ali = smsProperties.getAliyun();
		return ali != null && !StringUtils.isEmpty(ali.getAccessKeyId())
				&& !StringUtils.isEmpty(ali.getAccessKeySecret())
				&& !StringUtils.isEmpty(ali.getSignName())
				&& !StringUtils.isEmpty(ali.getTemplateCode());
	}

	/**
	 * 发送验证码短信。
	 *
	 * @param mobileNumber 11 位国内手机号
	 * @param code         验证码
	 * @return true=网关已受理
	 */
	public boolean sendVerificationCode(String mobileNumber, String code) {
		SmsProperties.Aliyun ali = smsProperties.getAliyun();
		if (!isConfigured()) {
			log.error("aliyun sms 配置不完整（accessKeyId/accessKeySecret/signName/templateCode），无法发送 mobile={}",
					maskMobile(mobileNumber));
			return false;
		}
		try {
			SendSmsRequest request = new SendSmsRequest();
			request.setPhoneNumbers(mobileNumber);
			request.setSignName(ali.getSignName());
			request.setTemplateCode(ali.getTemplateCode());
			String param = StringUtils.isEmpty(ali.getTemplateParam()) ? "code" : ali.getTemplateParam();
			request.setTemplateParam("{\"" + param + "\":\"" + code + "\"}");
			if (!StringUtils.isEmpty(ali.getEndpoint())) {
				request.setSysEndpoint(ali.getEndpoint());
			}
			SendSmsResponse response = client().getAcsResponse(request);
			if (response != null && OK.equalsIgnoreCase(response.getCode())) {
				log.info("aliyun sms ok mobile={} bizId={}", maskMobile(mobileNumber), response.getBizId());
				return true;
			}
			log.error("aliyun sms fail mobile={} code={} message={} requestId={}", maskMobile(mobileNumber),
					response == null ? null : response.getCode(),
					response == null ? null : response.getMessage(),
					response == null ? null : response.getRequestId());
			return false;
		} catch (Exception e) {
			log.error("aliyun sms error mobile={}", maskMobile(mobileNumber), e);
			return false;
		}
	}

	private IAcsClient client() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					SmsProperties.Aliyun ali = smsProperties.getAliyun();
					DefaultProfile profile = DefaultProfile.getProfile(
							StringUtils.isEmpty(ali.getRegionId()) ? "cn-hangzhou" : ali.getRegionId(),
							ali.getAccessKeyId(), ali.getAccessKeySecret());
					client = new DefaultAcsClient(profile);
				}
			}
		}
		return client;
	}

	/** 日志脱敏：保留前 3 后 4 */
	private String maskMobile(String mobile) {
		if (mobile == null || mobile.length() < 8) {
			return mobile;
		}
		return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
	}
}
