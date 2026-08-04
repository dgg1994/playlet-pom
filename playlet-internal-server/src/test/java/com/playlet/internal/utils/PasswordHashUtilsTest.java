package com.playlet.internal.utils;

import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashUtilsTest {

	@Test
	void encodeAndMatchBcrypt() {
		String hash = PasswordHashUtils.encode("Secret123!");
		assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
		assertTrue(PasswordHashUtils.matches("Secret123!", hash));
		assertFalse(PasswordHashUtils.matches("wrong", hash));
		assertFalse(PasswordHashUtils.needsRehash(hash));
	}

	@Test
	void matchesLegacyMd5AndNeedsRehash() {
		String md5 = DigestUtils.md5DigestAsHex("legacy-pass".getBytes());
		assertTrue(PasswordHashUtils.matches("legacy-pass", md5));
		assertTrue(PasswordHashUtils.needsRehash(md5));
		assertFalse(PasswordHashUtils.matches("other", md5));
	}

	@Test
	void encodeProducesDifferentSalts() {
		String a = PasswordHashUtils.encode("same");
		String b = PasswordHashUtils.encode("same");
		assertNotEquals(a, b);
		assertTrue(PasswordHashUtils.matches("same", a));
		assertTrue(PasswordHashUtils.matches("same", b));
	}
}
