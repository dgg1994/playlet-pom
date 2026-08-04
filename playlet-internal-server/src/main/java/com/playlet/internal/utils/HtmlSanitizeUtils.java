package com.playlet.internal.utils;

import cn.hutool.http.HtmlUtil;

import java.util.regex.Pattern;

/**
 * HTML XSS 防护：纯文本转义、富文本白名单过滤。
 */
public final class HtmlSanitizeUtils {

	private static final int MAX_PLAIN_LEN = 2000;
	private static final int MAX_RICH_LEN = 50000;

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
	 * 运营富文本（系统消息内容等）：Hutool HTMLFilter 过滤危险标签/属性，并清掉危险协议与残留事件属性。
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
		String filtered = HtmlUtil.filter(cleaned);
		filtered = EVENT_ATTR.matcher(filtered).replaceAll("");
		filtered = EVENT_ATTR_UNQUOTED.matcher(filtered).replaceAll("");
		filtered = DANGEROUS_SCHEME.matcher(filtered).replaceAll("$1$2#$2");
		return filtered;
	}
}
