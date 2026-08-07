package com.playlet.internal.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlSanitizeUtilsTest {

	@Test
	void plainStripsTagsAndEscapes() {
		String out = HtmlSanitizeUtils.plain("<script>alert(1)</script>hi");
		assertFalse(out.contains("<script"));
		assertTrue(out.contains("hi") || out.contains("alert"));
	}

	@Test
	void plainRemovesNullBytesAndNullSafe() {
		assertNull(HtmlSanitizeUtils.plain(null));
		assertEquals("", HtmlSanitizeUtils.plain(""));
		String out = HtmlSanitizeUtils.plain("a\0b");
		assertFalse(out.contains("\0"));
	}

	@Test
	void richRemovesScriptAndDangerousScheme() {
		String html = "<p>ok</p><script>alert(1)</script><a href=\"javascript:alert(1)\">x</a>";
		String out = HtmlSanitizeUtils.rich(html);
		assertFalse(out.toLowerCase().contains("<script"));
		assertFalse(out.toLowerCase().contains("javascript:"));
		assertTrue(out.contains("<p>ok</p>"));
	}

	@Test
	void richKeepsProtocolTagsAndStyle() {
		String html = "<h1 style=\"text-align: start;\">BerryV 用户协议</h1><p>更新日期</p><h2>一、服务说明</h2><br><strong>加粗</strong>";
		String out = HtmlSanitizeUtils.rich(html);
		assertTrue(out.contains("<h1"));
		assertTrue(out.contains("style=\"text-align: start;\""));
		assertTrue(out.contains("<p>"));
		assertTrue(out.contains("<h2>"));
		assertTrue(out.contains("<br>"));
		assertTrue(out.contains("<strong>"));
	}

	@Test
	void richStripsEventHandlers() {
		String out = HtmlSanitizeUtils.rich("<img src=x onerror=alert(1)>");
		assertFalse(out.toLowerCase().contains("onerror"));
	}
}
