package com.playlet.oversea.utils;

import java.nio.charset.StandardCharsets;

/**
 * 协议/充值介绍富文本：包成可独立打开的 HTML（对齐 onetoken FileUploadUtil.saveHtmlToFile）。
 */
public final class SysConfigHtmlUtils {

	private SysConfigHtmlUtils() {
	}

	/**
	 * 将编辑器 HTML 片段包装为完整页面（黑底白字，便于 App WebView）。
	 */
	public static byte[] toFullHtmlBytes(String htmlFragment) {
		String body = htmlFragment == null ? "" : htmlFragment;
		String fullHtml = "<!DOCTYPE html>"
				+ "<html>"
				+ "<head>"
				+ "<meta charset=\"UTF-8\">"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
				+ "<title>Playlet</title>"
				+ "<style>"
				+ "body { color: white; background-color: black; }"
				+ "</style>"
				+ "</head>"
				+ "<body>"
				+ body
				+ "</body>"
				+ "</html>";
		return fullHtml.getBytes(StandardCharsets.UTF_8);
	}
}
