package com.playlet.oversea.utils;

import cn.hutool.http.HtmlUtil;

import java.util.regex.Pattern;

/**
 * HTML XSS 防护：纯文本转义、富文本危险标签/属性过滤。
 */
public final class HtmlSanitizeUtils {

	private static final int MAX_PLAIN_LEN = 2000;
	private static final int MAX_RICH_LEN = 200000;

	/** 协议/运营文案需要保留的结构标签，仅剔除高危标签整段 */
	private static final Pattern DANGEROUS_TAG_BLOCK = Pattern.compile(
			"(?is)<(script|iframe|object|embed|form|link|meta|base|svg|math|style|noscript)\\b[^>]*>.*?</\\1\\s*>");
	private static final Pattern DANGEROUS_TAG_EMPTY = Pattern.compile(
			"(?is)</?(script|iframe|object|embed|form|link|meta|base|svg|math|style|noscript)\\b[^>]*>");
	private static final Pattern DANGEROUS_SCHEME = Pattern.compile(
			"(?is)(\\s(?:href|src|xlink:href|action|formaction|poster|data|background)\\s*=\\s*)([\"']?)\\s*(?:javascript|vbscript|data)\\s*:[^\"'\\s>]*\\2");
	private static final Pattern EVENT_ATTR = Pattern.compile("(?is)\\s+on[a-z]+\\s*=\\s*([\"']).*?\\1");
	private static final Pattern EVENT_ATTR_UNQUOTED = Pattern.compile("(?is)\\s+on[a-z]+\\s*=\\s*[^\\s>]+");

	private HtmlSanitizeUtils() {
	}

	/**
	 * 评论、昵称等纯文本：去掉 HTML 标签后再转义特殊字符。
	 */
	public static String plain(String text) {
		if (text == null) {
			return null;
		}
		if (text.isEmpty()) {
			return text;
		}
		String cleaned = text.replace("\0", "");
		if (cleaned.length() > MAX_PLAIN_LEN) {
			cleaned = cleaned.substring(0, MAX_PLAIN_LEN);
		}
		return HtmlUtil.escape(HtmlUtil.cleanHtmlTag(cleaned));
	}

	/**
	 * 运营富文本（协议/系统消息等）：保留 h1/p/br/strong 等排版标签，只清脚本与危险属性。
	 * 不用 Hutool HtmlUtil.filter（会误删几乎全部结构标签）。
	 */
	public static String rich(String html) {
		if (html == null) {
			return null;
		}
		if (html.isEmpty()) {
			return html;
		}
		String cleaned = html.replace("\0", "");
		if (cleaned.length() > MAX_RICH_LEN) {
			cleaned = cleaned.substring(0, MAX_RICH_LEN);
		}
		cleaned = DANGEROUS_TAG_BLOCK.matcher(cleaned).replaceAll("");
		cleaned = DANGEROUS_TAG_EMPTY.matcher(cleaned).replaceAll("");
		cleaned = EVENT_ATTR.matcher(cleaned).replaceAll("");
		cleaned = EVENT_ATTR_UNQUOTED.matcher(cleaned).replaceAll("");
		cleaned = DANGEROUS_SCHEME.matcher(cleaned).replaceAll("$1$2#$2");
		return cleaned;
	}
}
