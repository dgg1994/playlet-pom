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
	}

	@Test
	void richStripsEventHandlers() {
		String out = HtmlSanitizeUtils.rich("<img src=x onerror=alert(1)>");
		assertFalse(out.toLowerCase().contains("onerror"));
	}
}
