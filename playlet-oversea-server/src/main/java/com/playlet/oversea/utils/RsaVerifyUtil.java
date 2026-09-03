package com.playlet.oversea.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 验签工具（worldPay WebHook / 入站回调）。
 */
public final class RsaVerifyUtil {

	private RsaVerifyUtil() {
	}

	/**
	 * 校验签名，规则与出站 {@link RsaSignUtil#buildSignContent} 一致。
	 */
	public static boolean verifySign(String appId, String nonce, String timestamp, Object body,
			String sign, String publicKeyStr) throws Exception {
		if (StringUtils.isEmpty(sign)) {
			return false;
		}
		String signContent = RsaSignUtil.buildSignContent(appId, nonce, timestamp, body);
		return verifyData(signContent, sign, publicKeyStr);
	}

	private static boolean verifyData(String data, String sign, String publicKeyStr) throws Exception {
		PublicKey publicKey = loadPublicKey(publicKeyStr);
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initVerify(publicKey);
		signature.update(data.getBytes(StandardCharsets.UTF_8));
		return signature.verify(Base64.getDecoder().decode(sign.trim()));
	}

	private static PublicKey loadPublicKey(String publicKeyStr) throws Exception {
		if (StringUtils.isEmpty(publicKeyStr)) {
			throw new IllegalArgumentException("publicKey 不能为空");
		}
		String normalized = publicKeyStr.trim()
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replace("-----BEGIN RSA PUBLIC KEY-----", "")
				.replace("-----END RSA PUBLIC KEY-----", "")
				.replace("\\n", "")
				.replace("\\r", "")
				.replaceAll("\\s+", "");
		byte[] keyBytes = Base64.getDecoder().decode(normalized);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
	}
}
