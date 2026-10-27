package com.playlet.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信通道配置（国内阿里云 / 国际 santo）。
 *
 * <p>yml 前缀 sms。provider 取值：
 * <ul>
 *     <li>auto：区号 86 走阿里云，其余走国际通道（默认）</li>
 *     <li>aliyun：全部走阿里云（仅支持中国大陆号码）</li>
 *     <li>santo：全部走国际通道</li>
 * </ul>
 * 国际通道的 url/account/password 仍由 {@code SmsUtil} 的 @Value 读取，此处不重复定义。
 */
@Data
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

	/** 通道选择：auto / aliyun / santo */
	private String provider = "auto";

	private Aliyun aliyun = new Aliyun();

	@Data
	public static class Aliyun {

		/** RAM 子账号 AccessKeyId */
		private String accessKeyId;

		/** RAM 子账号 AccessKeySecret */
		private String accessKeySecret;

		/** 地域，短信固定 cn-hangzhou */
		private String regionId = "cn-hangzhou";

		/** 接入地址，默认 dysmsapi.aliyuncs.com */
		private String endpoint = "dysmsapi.aliyuncs.com";

		/** 已报备的短信签名，如：剧好看 */
		private String signName;

		/** 已报备的验证码模板 CODE，如：SMS_123456789 */
		private String templateCode;

		/** 模板中验证码变量名，默认 code（模板内容形如：您的验证码为${code}，5分钟内有效） */
		private String templateParam = "code";
	}
}
