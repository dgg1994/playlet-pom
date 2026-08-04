package com.playlet.internal.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 上传内容安全校验（扩展名白名单、危险 MIME、内容嗅探、路径穿越）。
 * 供 {@link QiniuUploadUtils} 与单测共用。
 */
public final class UploadSafetyUtils {

	/** 允许上传的扩展名（小写、不含点） */
	public static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
			"jpg", "jpeg", "png", "gif", "webp", "bmp", "ico",
			"mp4", "mov", "m4v", "webm", "mkv", "avi", "ts", "m3u8",
			"mp3", "aac", "wav", "m4a",
			"pdf", "zip"
	));

	/** 一律拒绝（含 XSS/脚本宿主风险） */
	public static final Set<String> BLOCKED_EXT = new HashSet<>(Arrays.asList(
			"html", "htm", "shtml", "xhtml", "svg", "svgz",
			"js", "mjs", "jsx",
			"xml", "xsl", "xslt", "css",
			"php", "jsp", "asp", "aspx", "cgi", "sh", "bat", "cmd", "exe", "dll"
	));

	public static final Set<String> BLOCKED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
			"text/html", "application/xhtml+xml", "image/svg+xml",
			"text/javascript", "application/javascript", "application/x-javascript",
			"text/xml", "application/xml", "text/css", "application/ecmascript"
	));

	private static final Pattern ACTIVE_TAG = Pattern.compile(
			"(?is)<\\s*(script|iframe|object|embed|svg|html|body|meta|link|base|form)\\b");
	private static final Pattern EVENT_HANDLER = Pattern.compile("(?is)\\bon[a-z]+\\s*=");
	private static final Pattern JS_SCHEME = Pattern.compile("(?is)(javascript|vbscript|data)\\s*:");

	private UploadSafetyUtils() {
	}

	public static void assertSafeUpload(String fileName, String contentType, byte[] headBytes) {
		assertSafePath(fileName);
		assertExtensionAllowed(extractExtension(fileName));
		if (contentType != null && !contentType.isEmpty()) {
			String ct = contentType.toLowerCase(Locale.ROOT).split(";", 2)[0].trim();
			if (BLOCKED_CONTENT_TYPES.contains(ct)) {
				throw new RuntimeException("不支持的文件类型: " + ct);
			}
		}
		if (looksLikeActiveContent(headBytes)) {
			throw new RuntimeException("不支持的文件内容（疑似 HTML/SVG/脚本）");
		}
		// 声明为图片但文件头明显是文本标记时拒绝（防伪装）
		String ext = extractExtension(fileName);
		if (isImageExt(ext) && looksLikeTextMarkup(headBytes)) {
			throw new RuntimeException("不支持的文件内容（扩展名与内容不匹配）");
		}
	}

	public static void assertSafePath(String path) {
		if (path == null || path.isEmpty()) {
			throw new RuntimeException("上传路径为空");
		}
		String normalized = path.replace('\\', '/');
		if (normalized.contains("..") || normalized.startsWith("/") || normalized.contains("://")) {
			throw new RuntimeException("非法上传路径");
		}
		assertExtensionAllowed(extractExtension(normalized));
	}

	public static void assertExtensionAllowed(String ext) {
		if (ext == null || ext.isEmpty()) {
			throw new RuntimeException("文件缺少扩展名");
		}
		if (BLOCKED_EXT.contains(ext) || !ALLOWED_EXT.contains(ext)) {
			throw new RuntimeException("不支持的文件扩展名: ." + ext);
		}
	}

	public static String extractExtension(String fileName) {
		if (fileName == null) {
			return "";
		}
		String name = fileName.replace('\\', '/');
		int slash = name.lastIndexOf('/');
		if (slash >= 0) {
			name = name.substring(slash + 1);
		}
		int q = name.indexOf('?');
		if (q >= 0) {
			name = name.substring(0, q);
		}
		// 双扩展名：任意被拦截后缀出现即拒（a.jpg.html / a.html.jpg）
		String lower = name.toLowerCase(Locale.ROOT);
		for (String blocked : BLOCKED_EXT) {
			if (lower.contains("." + blocked + ".") || lower.endsWith("." + blocked)) {
				if (!ALLOWED_EXT.contains(blocked)) {
					return blocked;
				}
			}
		}
		int dot = name.lastIndexOf('.');
		if (dot < 0 || dot == name.length() - 1) {
			return "";
		}
		return name.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	public static boolean looksLikeActiveContent(byte[] head) {
		if (head == null || head.length == 0) {
			return false;
		}
		String sample = decodeSample(head);
		if (sample.startsWith("<!doctype html")
				|| sample.startsWith("<html")
				|| sample.startsWith("<svg")
				|| sample.startsWith("<?xml")
				|| sample.startsWith("<!entity")
				|| sample.contains("<script")
				|| ACTIVE_TAG.matcher(sample).find()
				|| EVENT_HANDLER.matcher(sample).find()
				|| JS_SCHEME.matcher(sample).find()) {
			return true;
		}
		// URL 编码绕过：%3Cscript
		String compact = sample.replace(" ", "");
		return compact.contains("%3cscript") || compact.contains("%3chtml") || compact.contains("%3csvg");
	}

	private static boolean looksLikeTextMarkup(byte[] head) {
		if (head == null || head.length == 0) {
			return false;
		}
		String sample = decodeSample(head);
		return sample.startsWith("<") || sample.startsWith("%3c") || sample.startsWith("&lt;");
	}

	private static boolean isImageExt(String ext) {
		return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext)
				|| "gif".equals(ext) || "webp".equals(ext) || "bmp".equals(ext) || "ico".equals(ext);
	}

	private static String decodeSample(byte[] head) {
		int len = Math.min(head.length, 512);
		String sample = new String(head, 0, len, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT).trim();
		if (sample.startsWith("\ufeff")) {
			sample = sample.substring(1).trim();
		}
		// 去掉常见空白干扰
		return sample.replace("\0", "");
	}
}
