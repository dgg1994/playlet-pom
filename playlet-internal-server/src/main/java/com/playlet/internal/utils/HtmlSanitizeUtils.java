package com.playlet.internal.utils;

import cn.hutool.http.HtmlUtil;

/**
 * HTML XSS 防护：纯文本转义、富文本白名单过滤。
 */
public final class HtmlSanitizeUtils {

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
		return HtmlUtil.escape(HtmlUtil.cleanHtmlTag(text));
	}

	/**
	 * 运营富文本（系统消息内容等）：Hutool HTMLFilter 过滤危险标签/属性。
	 */
	public static String rich(String html) {
		if (html == null) {
			return null;
		}
		if (html.isEmpty()) {
			return html;
		}
		return HtmlUtil.filter(html);
	}
}
