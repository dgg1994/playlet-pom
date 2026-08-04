package com.playlet.internal.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UploadSafetyUtilsTest {

	@Test
	void allowsNormalImage() {
		byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
		assertDoesNotThrow(() ->
				UploadSafetyUtils.assertSafeUpload("cover.png", "image/png", pngHeader));
	}

	@Test
	void rejectsBlockedExtAndMime() {
		assertThrows(RuntimeException.class, () ->
				UploadSafetyUtils.assertSafeUpload("a.html", "text/html", "<html>".getBytes()));
		assertThrows(RuntimeException.class, () ->
				UploadSafetyUtils.assertSafeUpload("a.jpg", "image/svg+xml", new byte[]{1, 2, 3}));
	}

	@Test
	void rejectsDoubleExtensionAndTraversal() {
		assertEquals("html", UploadSafetyUtils.extractExtension("a.jpg.html"));
		assertThrows(RuntimeException.class, () ->
				UploadSafetyUtils.assertSafePath("../etc/passwd.jpg"));
		assertThrows(RuntimeException.class, () ->
				UploadSafetyUtils.assertSafePath("/abs/evil.jpg"));
	}

	@Test
	void rejectsHtmlDisguisedAsImage() {
		byte[] html = "<!doctype html><script>alert(1)</script>".getBytes();
		RuntimeException ex = assertThrows(RuntimeException.class, () ->
				UploadSafetyUtils.assertSafeUpload("x.jpg", "image/jpeg", html));
		assertTrue(ex.getMessage().contains("内容") || ex.getMessage().contains("类型")
				|| ex.getMessage().contains("不支持"));
	}

	@Test
	void rejectsSvgAndEncodedScript() {
		assertTrue(UploadSafetyUtils.looksLikeActiveContent("<svg onload=alert(1)>".getBytes()));
		assertTrue(UploadSafetyUtils.looksLikeActiveContent("%3Cscript%3Ealert(1)%3C/script%3E".getBytes()));
	}
}
