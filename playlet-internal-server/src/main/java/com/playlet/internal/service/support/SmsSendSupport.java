package com.playlet.internal.service.support;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.SmsProperties;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.utils.AliyunSmsUtil;
import com.playlet.internal.utils.SmsUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 短信验证码发送路由。
 *
 * <p>按 {@code sms.provider} 选择通道：auto 时中国大陆号码（区号 86）走阿里云，其余走国际通道 santo。
 */
@Slf4j
@Component
public class SmsSendSupport {

	public static final String PROVIDER_ALIYUN = "aliyun";
	public static final String PROVIDER_SANTO = "santo";

	@Autowired
	private SmsProperties smsProperties;

	@Autowired
	private AliyunSmsUtil aliyunSmsUtil;

	/**
	 * 发送验证码短信。
	 *
	 * @param mobilePrefix 国家区号，如 86 / +86
	 * @param mobileNumber 手机号（不含区号）
	 * @param code         验证码
	 * @return true=通道已受理
	 */
	public boolean sendVerificationCode(String mobilePrefix, String mobileNumber, String code) {
		String provider = resolveProvider(mobilePrefix);
		if (PROVIDER_ALIYUN.equals(provider)) {
			if (aliyunSmsUtil.isConfigured()) {
				return aliyunSmsUtil.sendVerificationCode(toNationalNumber(mobilePrefix, mobileNumber), code);
			}
			// 显式配置 aliyun 却未填齐参数：直接失败，避免静默走错通道
			if (!isAutoProvider()) {
				log.error("sms.provider=aliyun 但阿里云短信配置不完整");
				return false;
			}
			log.warn("阿里云短信未配置，auto 模式回退国际通道");
		}
		ResponseBase result = SmsUtil.sendVerificationCode(toE164(mobilePrefix, mobileNumber), code);
		return result != null && Integer.valueOf(Constants.HTTP_RES_CODE_200).equals(result.getCode());
	}

	private boolean isAutoProvider() {
		String provider = smsProperties.getProvider();
		return StringUtils.isEmpty(provider) || "auto".equalsIgnoreCase(provider.trim());
	}

	/** 当前号码命中的通道：aliyun / santo */
	public String resolveProvider(String mobilePrefix) {
		String provider = smsProperties.getProvider();
		if (StringUtils.isEmpty(provider) || "auto".equalsIgnoreCase(provider.trim())) {
			return isChina(mobilePrefix) ? PROVIDER_ALIYUN : PROVIDER_SANTO;
		}
		String p = provider.trim().toLowerCase();
		if (!PROVIDER_ALIYUN.equals(p) && !PROVIDER_SANTO.equals(p)) {
			log.warn("sms.provider={} 不支持，按 auto 处理", provider);
			return isChina(mobilePrefix) ? PROVIDER_ALIYUN : PROVIDER_SANTO;
		}
		return p;
	}

	private boolean isChina(String mobilePrefix) {
		if (StringUtils.isEmpty(mobilePrefix)) {
			return false;
		}
		String prefix = mobilePrefix.trim();
		return "86".equals(prefix.startsWith("+") ? prefix.substring(1) : prefix);
	}

	/** 阿里云要求 11 位号码，不带国家码 */
	private String toNationalNumber(String mobilePrefix, String mobileNumber) {
		String number = mobileNumber.trim();
		if (isChina(mobilePrefix) && number.length() == 13 && number.startsWith("86")) {
			return number.substring(2);
		}
		return number;
	}

	/** 国际通道要求：区号+号码，不带 + */
	private String toE164(String mobilePrefix, String mobileNumber) {
		String prefix = mobilePrefix == null ? "" : mobilePrefix.trim();
		if (prefix.startsWith("+")) {
			prefix = prefix.substring(1);
		}
		return prefix + mobileNumber.trim();
	}
}
