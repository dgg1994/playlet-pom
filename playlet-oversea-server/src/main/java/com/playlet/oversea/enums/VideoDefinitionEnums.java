package com.playlet.oversea.enums;

import java.util.regex.Pattern;

/**
 * 视频清晰度（多码率命名约定：{prefix}_{code}.m3u8 / {prefix}_{code}.mp4）
 */
public enum VideoDefinitionEnums {
	D360("360", "流畅"),
	D480("480", "标清"),
	D720("720", "高清"),
	D1080("1080", "超清");

	/** 默认清晰度 */
	public static final VideoDefinitionEnums DEFAULT = D720;

	/** 带码率后缀：prefix_720.m3u8 */
	public static final Pattern MULTI_RATE_M3U8_PATTERN;

	/** 普通 m3u8：prefix.m3u8 */
	public static final Pattern PLAIN_M3U8_PATTERN =
			Pattern.compile("^(.*)\\.m3u8$", Pattern.CASE_INSENSITIVE);

	/** 带码率后缀：prefix_720.mp4 */
	public static final Pattern MULTI_RATE_MP4_PATTERN;

	/** 普通 mp4：prefix.mp4 */
	public static final Pattern PLAIN_MP4_PATTERN =
			Pattern.compile("^(.*)\\.mp4$", Pattern.CASE_INSENSITIVE);

	/** 无扩展名时的码率后缀：xxx_720 */
	public static final Pattern DEFINITION_SUFFIX_PATTERN;

	/** 默认清晰度回退顺序（高优先） */
	public static final VideoDefinitionEnums[] FALLBACK_ORDER = {D1080, D720, D480, D360};

	private final String code;
	private final String label;

	static {
		String codes = codesRegex();
		MULTI_RATE_M3U8_PATTERN = Pattern.compile(
				"^(.*)_(" + codes + ")\\.m3u8$", Pattern.CASE_INSENSITIVE);
		MULTI_RATE_MP4_PATTERN = Pattern.compile(
				"^(.*)_(" + codes + ")\\.mp4$", Pattern.CASE_INSENSITIVE);
		DEFINITION_SUFFIX_PATTERN = Pattern.compile(
				"(?i).*_(" + codes + ")$");
	}

	VideoDefinitionEnums(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	/** 拼多码率 m3u8 key：prefix + _720.m3u8 */
	public String toM3u8Key(String prefix) {
		return prefix + "_" + code + ".m3u8";
	}

	/** 拼多码率 mp4 下载 key：prefix + _720.mp4 */
	public String toMp4Key(String prefix) {
		return prefix + "_" + code + ".mp4";
	}

	/**
	 * 从库内 video_url（m3u8/mp4，可带清晰度后缀）解析出命名前缀。
	 * oceans_720.m3u8 / oceans.m3u8 / oceans_720.mp4 / oceans.mp4 -> oceans
	 */
	public static String resolvePrefix(String key) {
		if (key == null || key.isEmpty()) {
			return null;
		}
		java.util.regex.Matcher m = MULTI_RATE_M3U8_PATTERN.matcher(key);
		if (m.matches()) {
			return m.group(1);
		}
		m = MULTI_RATE_MP4_PATTERN.matcher(key);
		if (m.matches()) {
			return m.group(1);
		}
		m = PLAIN_M3U8_PATTERN.matcher(key);
		if (m.matches()) {
			return stripDefinitionSuffix(m.group(1));
		}
		m = PLAIN_MP4_PATTERN.matcher(key);
		if (m.matches()) {
			return stripDefinitionSuffix(m.group(1));
		}
		int slash = key.lastIndexOf('/');
		String name = slash >= 0 ? key.substring(slash + 1) : key;
		int dot = name.lastIndexOf('.');
		if (dot > 0) {
			name = name.substring(0, dot);
		}
		return stripDefinitionSuffix(name);
	}

	private static String stripDefinitionSuffix(String nameWithoutExt) {
		if (!hasDefinitionSuffix(nameWithoutExt)) {
			return nameWithoutExt;
		}
		return nameWithoutExt.replaceAll("(?i)_(" + codesRegex() + ")$", "");
	}

	public static VideoDefinitionEnums ofCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (VideoDefinitionEnums item : values()) {
			if (item.code.equalsIgnoreCase(code)) {
				return item;
			}
		}
		return null;
	}

	public static String labelOf(String code) {
		VideoDefinitionEnums item = ofCode(code);
		return item == null ? null : item.label;
	}

	public static boolean hasDefinitionSuffix(String nameWithoutExt) {
		return nameWithoutExt != null && DEFINITION_SUFFIX_PATTERN.matcher(nameWithoutExt).matches();
	}

	/** 将已有码率后缀替换为指定清晰度，如 xxx_360 -> xxx_720 */
	public static String replaceDefinitionSuffix(String nameWithoutExt, VideoDefinitionEnums target) {
		return nameWithoutExt.replaceAll("(?i)_(" + codesRegex() + ")$", "_" + target.getCode());
	}

	private static String codesRegex() {
		StringBuilder sb = new StringBuilder();
		VideoDefinitionEnums[] items = values();
		for (int i = 0; i < items.length; i++) {
			if (i > 0) {
				sb.append('|');
			}
			sb.append(items[i].code);
		}
		return sb.toString();
	}
}
