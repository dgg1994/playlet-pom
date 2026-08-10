package com.playlet.oversea.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

/**
 * 密码哈希：新密码使用 BCrypt；校验兼容历史 MD5。
 */
public final class PasswordHashUtils {

	private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

	private PasswordHashUtils() {
	}

	/** 对新密码编码（BCrypt） */
	public static String encode(String rawPassword) {
		if (rawPassword == null) {
			return null;
		}
		return BCRYPT.encode(rawPassword);
	}

	/** 校验明文与库中哈希（支持 BCrypt / 历史 MD5） */
	public static boolean matches(String rawPassword, String storedHash) {
		if (rawPassword == null || storedHash == null || storedHash.isEmpty()) {
			return false;
		}
		if (isBcrypt(storedHash)) {
			return BCRYPT.matches(rawPassword, storedHash);
		}
		String md5 = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
		return storedHash.equalsIgnoreCase(md5);
	}

	/** 是否为旧哈希，登录成功后应重写为 BCrypt */
	public static boolean needsRehash(String storedHash) {
		return storedHash != null && !storedHash.isEmpty() && !isBcrypt(storedHash);
	}

	private static boolean isBcrypt(String hash) {
		return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
	}
}
