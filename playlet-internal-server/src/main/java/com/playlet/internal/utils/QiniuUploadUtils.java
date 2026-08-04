package com.playlet.internal.utils;

import com.playlet.internal.config.QiniuConfig;
import com.playlet.internal.constants.RedisKeyConstants;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 七牛云上传工具类。
 * <p>
 * 上传成功返回对象 key（非签名 URL）；读时通过 {@link #toAccessUrl(String)} / MediaUrlService 签名。
 */
@Slf4j
@Component
public class QiniuUploadUtils {

	private static QiniuUploadUtils instance;

	/** 允许上传的扩展名（小写、不含点） */
	private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
			"jpg", "jpeg", "png", "gif", "webp", "bmp", "ico",
			"mp4", "mov", "m4v", "webm", "mkv", "avi", "ts", "m3u8",
			"mp3", "aac", "wav", "m4a",
			"pdf", "zip"
	));

	/** 一律拒绝（含 XSS/脚本宿主风险） */
	private static final Set<String> BLOCKED_EXT = new HashSet<>(Arrays.asList(
			"html", "htm", "shtml", "xhtml", "svg", "svgz",
			"js", "mjs", "jsx",
			"xml", "xsl", "xslt", "css",
			"php", "jsp", "asp", "aspx", "cgi", "sh", "bat", "cmd", "exe", "dll"
	));

	private static final Set<String> BLOCKED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
			"text/html", "application/xhtml+xml", "image/svg+xml",
			"text/javascript", "application/javascript", "application/x-javascript",
			"text/xml", "application/xml"
	));

	@Autowired
	private QiniuConfig qiniuConfig;

	@Autowired
	private Auth qiniuAuth;

	@Autowired
	private UploadManager qiniuUploadManager;

	@Autowired(required = false)
	private RedisUtil redisUtil;

	@PostConstruct
	public void init() {
		instance = this;
	}

	private static QiniuUploadUtils getInstance() {
		if (instance == null) {
			throw new RuntimeException("QiniuUploadUtils 未初始化，请检查Spring配置");
		}
		return instance;
	}

	// ==================== 静态方法 ====================

	/** 上传文件，返回对象 key */
	public static String uploadFile(MultipartFile file, String dir) {
		return getInstance().fileUpload(file, dir);
	}

	/** 上传视频，返回对象 key */
	public static String uploadVideo(MultipartFile file, String dir) {
		return getInstance().videoUpload(file, dir);
	}

	public static String upload(byte[] data, String fileName) {
		return getInstance().doUpload(data, fileName, null);
	}

	public static String upload(byte[] data, String fileName, String dir) {
		return getInstance().doUpload(data, fileName, dir);
	}

	public static String upload(InputStream inputStream, String fileName) {
		return getInstance().doUpload(inputStream, fileName, null);
	}

	public static String upload(InputStream inputStream, String fileName, String dir) {
		return getInstance().doUpload(inputStream, fileName, dir);
	}

	public static String uploadWithFullPath(MultipartFile file, String fullPath) {
		return getInstance().doUploadWithFullPath(file, fullPath);
	}

	public static String uploadWithFullPath(byte[] data, String fullPath) {
		return getInstance().doUploadWithFullPath(data, fullPath);
	}

	public static boolean delete(String fileName) {
		return getInstance().doDelete(getInstance().qiniuConfig.extractKey(fileName));
	}

	public static boolean deleteByUrl(String fileUrl) {
		return getInstance().doDeleteByUrl(fileUrl);
	}

	/** 提取对象 key（兼容历史完整 URL） */
	public static String extractKey(String keyOrUrl) {
		return getInstance().qiniuConfig.extractKey(keyOrUrl);
	}

	/** 判断七牛对象是否存在 */
	public static boolean exists(String keyOrUrl) {
		return getInstance().doExists(keyOrUrl);
	}

	/**
	 * 读时访问地址（私有则签名）。
	 * @deprecated 业务侧优先注入 {@link com.playlet.internal.service.MediaUrlService}
	 */
	@Deprecated
	public static String getFileUrl(String fileName) {
		return toAccessUrl(fileName);
	}

	/** 读时访问地址：封面等默认过期时间 */
	public static String toAccessUrl(String keyOrUrl) {
		QiniuUploadUtils utils = getInstance();
		return utils.qiniuConfig.toAccessUrl(keyOrUrl, utils.qiniuConfig.getUrlExpireSeconds(), utils.qiniuAuth);
	}

	/** 读时访问地址：自定义过期秒数 */
	public static String toAccessUrl(String keyOrUrl, long expireSeconds) {
		QiniuUploadUtils utils = getInstance();
		return utils.qiniuConfig.toAccessUrl(keyOrUrl, expireSeconds, utils.qiniuAuth);
	}

	// ==================== 实例方法 ====================

	private String videoUpload(MultipartFile file, String dir) {
		assertSafeMultipart(file);
		try {
			String fileName = file.getOriginalFilename();
			String fullPath = buildFullPath(fileName, dir);
			String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
			Response response = qiniuUploadManager.put(file.getBytes(), fullPath, upToken);
			if (response.isOK()) {
				log.info("文件上传成功 key={}", fullPath);
				return fullPath;
			}
			log.error("上传失败: {}", response.bodyString());
			throw new RuntimeException("上传失败: " + response.bodyString());
		} catch (IOException e) {
			log.error("文件读取失败", e);
			throw new RuntimeException("文件读取失败", e);
		}
	}

	private String fileUpload(MultipartFile file, String dir) {
		assertSafeMultipart(file);
		try {
			String fileName = generateFileName(file.getOriginalFilename());
			String fullPath = buildFullPath(fileName, dir);
			String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
			Response response = qiniuUploadManager.put(file.getBytes(), fullPath, upToken);
			if (response.isOK()) {
				log.info("文件上传成功 key={}", fullPath);
				return fullPath;
			}
			log.error("上传失败: {}", response.bodyString());
			throw new RuntimeException("上传失败: " + response.bodyString());
		} catch (IOException e) {
			log.error("文件读取失败", e);
			throw new RuntimeException("文件读取失败", e);
		}
	}

	private String doUpload(byte[] data, String fileName, String dir) {
		if (data == null || data.length == 0) {
			throw new RuntimeException("文件数据为空");
		}
		assertSafeUpload(fileName, null, data);
		try {
			String fullFileName = generateFileName(fileName);
			String fullPath = buildFullPath(fullFileName, dir);
			String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
			Response response = qiniuUploadManager.put(data, fullPath, upToken);
			if (response.isOK()) {
				log.info("文件上传成功 key={}", fullPath);
				return fullPath;
			}
			log.error("上传失败: {}", response.bodyString());
			throw new RuntimeException("上传失败: " + response.bodyString());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			log.error("上传失败", e);
			throw new RuntimeException("上传失败", e);
		}
	}

	private String doUpload(InputStream inputStream, String fileName, String dir) {
		if (inputStream == null) {
			throw new RuntimeException("输入流为空");
		}
		try {
			byte[] data = inputStream.readAllBytes();
			return doUpload(data, fileName, dir);
		} catch (IOException e) {
			log.error("读取输入流失败", e);
			throw new RuntimeException("读取输入流失败", e);
		}
	}

	private String doUploadWithFullPath(MultipartFile file, String fullPath) {
		assertSafeMultipart(file);
		assertSafePath(fullPath);
		try {
			String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
			Response response = qiniuUploadManager.put(file.getBytes(), fullPath, upToken);
			if (response.isOK()) {
				log.info("文件上传成功 key={}", fullPath);
				return fullPath;
			}
			log.error("上传失败: {}", response.bodyString());
			throw new RuntimeException("上传失败: " + response.bodyString());
		} catch (IOException e) {
			log.error("文件读取失败", e);
			throw new RuntimeException("文件读取失败", e);
		}
	}

	private String doUploadWithFullPath(byte[] data, String fullPath) {
		if (data == null || data.length == 0) {
			throw new RuntimeException("文件数据为空");
		}
		assertSafePath(fullPath);
		assertSafeUpload(fullPath, null, data);
		try {
			String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
			Response response = qiniuUploadManager.put(data, fullPath, upToken);
			if (response.isOK()) {
				log.info("文件上传成功 key={}", fullPath);
				return fullPath;
			}
			log.error("上传失败: {}", response.bodyString());
			throw new RuntimeException("上传失败: " + response.bodyString());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			log.error("上传失败", e);
			throw new RuntimeException("上传失败", e);
		}
	}

	private void assertSafeMultipart(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new RuntimeException("文件为空");
		}
		byte[] head;
		try {
			byte[] all = file.getBytes();
			head = all.length > 512 ? Arrays.copyOf(all, 512) : all;
		} catch (IOException e) {
			throw new RuntimeException("文件读取失败", e);
		}
		assertSafeUpload(file.getOriginalFilename(), file.getContentType(), head);
	}

	private void assertSafePath(String path) {
		if (path == null || path.isEmpty()) {
			throw new RuntimeException("上传路径为空");
		}
		assertExtensionAllowed(extractExtension(path));
	}

	/** 扩展名白名单 + 危险 MIME + 内容嗅探（拦截 HTML/SVG/脚本伪装）。 */
	private void assertSafeUpload(String fileName, String contentType, byte[] headBytes) {
		assertExtensionAllowed(extractExtension(fileName));
		if (contentType != null && !contentType.isEmpty()) {
			String ct = contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
			if (BLOCKED_CONTENT_TYPES.contains(ct)) {
				throw new RuntimeException("不支持的文件类型: " + ct);
			}
		}
		if (looksLikeActiveContent(headBytes)) {
			throw new RuntimeException("不支持的文件内容（疑似 HTML/SVG/脚本）");
		}
	}

	private void assertExtensionAllowed(String ext) {
		if (ext == null || ext.isEmpty()) {
			throw new RuntimeException("文件缺少扩展名");
		}
		if (BLOCKED_EXT.contains(ext) || !ALLOWED_EXT.contains(ext)) {
			throw new RuntimeException("不支持的文件扩展名: ." + ext);
		}
	}

	private static String extractExtension(String fileName) {
		if (fileName == null) {
			return "";
		}
		String name = fileName;
		int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		if (slash >= 0) {
			name = name.substring(slash + 1);
		}
		int q = name.indexOf('?');
		if (q >= 0) {
			name = name.substring(0, q);
		}
		int dot = name.lastIndexOf('.');
		if (dot < 0 || dot == name.length() - 1) {
			return "";
		}
		return name.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	private static boolean looksLikeActiveContent(byte[] head) {
		if (head == null || head.length == 0) {
			return false;
		}
		String sample = new String(head, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT).trim();
		if (sample.startsWith("\ufeff")) {
			sample = sample.substring(1).trim();
		}
		return sample.startsWith("<!doctype html")
				|| sample.startsWith("<html")
				|| sample.startsWith("<svg")
				|| sample.contains("<script")
				|| (sample.startsWith("<?xml") && sample.contains("<svg"))
				|| sample.startsWith("<!entity");
	}

	private boolean doDelete(String fileName) {
		try {
			Configuration cfg = new Configuration(Region.autoRegion());
			BucketManager bucketManager = new BucketManager(qiniuAuth, cfg);
			Response response = bucketManager.delete(qiniuConfig.getBucket(), fileName);
			if (response.isOK()) {
				log.info("文件删除成功: {}", fileName);
				return true;
			}
			log.error("文件删除失败: {}", response.bodyString());
			return false;
		} catch (Exception e) {
			log.error("删除异常", e);
			return false;
		}
	}

	private boolean doExists(String keyOrUrl) {
		String key = qiniuConfig.extractKey(keyOrUrl);
		if (key == null || key.isEmpty()) {
			return false;
		}
		String cacheKey = RedisKeyConstants.QINIU_EXISTS_KEY + key;
		if (redisUtil != null) {
			try {
				Object cached = redisUtil.get(cacheKey);
				if (cached != null) {
					return "1".equals(String.valueOf(cached));
				}
			} catch (Exception e) {
				log.debug("七牛 exists 读缓存失败 key={}: {}", key, e.getMessage());
			}
		}
		boolean exists = probeExists(key);
		if (redisUtil != null) {
			try {
				redisUtil.set(cacheKey, exists ? "1" : "0", RedisKeyConstants.QINIU_EXISTS_TTL_SEC);
			} catch (Exception e) {
				log.debug("七牛 exists 写缓存失败 key={}: {}", key, e.getMessage());
			}
		}
		return exists;
	}

	private boolean probeExists(String key) {
		try {
			Configuration cfg = new Configuration(Region.autoRegion());
			BucketManager bucketManager = new BucketManager(qiniuAuth, cfg);
			return bucketManager.stat(qiniuConfig.getBucket(), key) != null;
		} catch (com.qiniu.common.QiniuException e) {
			// 612: no such file or directory
			if (e.response != null && e.response.statusCode == 612) {
				return false;
			}
			log.warn("七牛对象存在性检查失败 key={}, code={}, err={}",
					key, e.response == null ? null : e.response.statusCode, e.getMessage());
			return false;
		} catch (Exception e) {
			log.warn("七牛对象存在性检查异常 key={}: {}", key, e.getMessage());
			return false;
		}
	}

	private boolean doDeleteByUrl(String fileUrl) {
		if (fileUrl == null || fileUrl.isEmpty()) {
			return false;
		}
		try {
			return doDelete(qiniuConfig.extractKey(fileUrl));
		} catch (Exception e) {
			log.error("删除失败", e);
			return false;
		}
	}

	private String generateFileName(String originalFilename) {
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}
		String uuid = UUID.randomUUID().toString().replace("-", "");
		return uuid + extension;
	}

	private String buildFullPath(String fileName, String dir) {
		if (dir == null || dir.isEmpty()) {
			String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
			return datePath + "/" + fileName;
		}
		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}
		return dir + fileName;
	}

	/** 替换扩展名（作用于 key 或 URL 路径，不含 query） */
	public static String replaceFileExtension(String url, String newExtension) {
		if (url == null || url.isEmpty()) {
			return url;
		}
		String path = url;
		String query = "";
		int q = url.indexOf('?');
		if (q >= 0) {
			path = url.substring(0, q);
			query = url.substring(q);
		}
		String ext = newExtension == null ? "" : newExtension;
		if (ext.startsWith(".")) {
			ext = ext.substring(1);
		}
		int lastDotIndex = path.lastIndexOf(".");
		String replaced = lastDotIndex == -1 ? path + "." + ext : path.substring(0, lastDotIndex) + "." + ext;
		return replaced + query;
	}
}
