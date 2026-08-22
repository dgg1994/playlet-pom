package com.playlet.oversea.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.playlet.oversea.config.QiniuConfig;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.constants.RedisKeyConstants;
import com.playlet.oversea.api.response.DramaVideoUploadRespEntity;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 七牛云上传工具类。
 * <p>
 * 上传成功返回对象 key（非签名 URL）；读时通过 {@link #toAccessUrl(String)} / MediaUrlService 签名。
 */
@Slf4j
@Component
public class QiniuUploadUtils {

	private static QiniuUploadUtils instance;

	/** 视频允许的扩展名（前端直传） */
	private static final Set<String> VIDEO_EXT = new HashSet<>(Arrays.asList(
			"mp4", "mov", "m4v", "webm", "mkv", "avi", "ts", "m3u8"
	));

	private static final OkHttpClient AVINFO_HTTP = new OkHttpClient.Builder()
			.connectTimeout(8, TimeUnit.SECONDS)
			.readTimeout(15, TimeUnit.SECONDS)
			.build();

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
	 * @deprecated 业务侧优先注入 {@link com.playlet.oversea.service.MediaUrlService}
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

	/**
	 * 生成剧集视频前端直传凭证（UploadToken 绑定固定 key）。
	 * key 形如 VD_{dramaId}/EP_{setNum}/{uuid}.{ext}
	 */
	public static DramaVideoUploadRespEntity createVideoUploadCredential(Integer dramaId, Integer setNum, String ext) {
		return getInstance().doCreateVideoUploadCredential(dramaId, setNum, ext);
	}

	/** 剧集视频 key 前缀：VD_{dramaId}/EP_{setNum}/ */
	public static String videoKeyPrefix(Integer dramaId, Integer setNum) {
		if (dramaId == null || setNum == null) {
			throw new RuntimeException("dramaId/setNum 不能为空");
		}
		return String.format(Constants.VIDEO_UPLOAD_SITE, dramaId, setNum);
	}

	// ==================== 实例方法 ====================

	private DramaVideoUploadRespEntity doCreateVideoUploadCredential(Integer dramaId, Integer setNum, String ext) {
		if (dramaId == null || setNum == null) {
			throw new RuntimeException("dramaId/setNum 不能为空");
		}
		String safeExt = normalizeVideoExt(ext);
		String key = videoKeyPrefix(dramaId, setNum)
				+ UUID.randomUUID().toString().replace("-", "") + "." + safeExt;
		UploadSafetyUtils.assertSafePath(key);
		long expire = qiniuConfig.getUploadTokenExpireSeconds();
		if (expire <= 0) {
			expire = 3600L;
		}
		String token = qiniuAuth.uploadToken(qiniuConfig.getBucket(), key, expire, null);
		DramaVideoUploadRespEntity resp = new DramaVideoUploadRespEntity();
		resp.setUploadToken(token);
		resp.setKey(key);
		resp.setDomain(qiniuConfig.getDomain());
		resp.setExpireSeconds(expire);
		resp.setUploadUrl(qiniuConfig.getUploadUrl());
		return resp;
	}

	private static String normalizeVideoExt(String ext) {
		String e = ext == null ? "mp4" : ext.trim().toLowerCase(Locale.ROOT);
		if (e.startsWith(".")) {
			e = e.substring(1);
		}
		if (e.isEmpty()) {
			e = "mp4";
		}
		if (!VIDEO_EXT.contains(e)) {
			throw new RuntimeException("不支持的视频扩展名: ." + e);
		}
		return e;
	}

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
		UploadSafetyUtils.assertSafeUpload(file.getOriginalFilename(), file.getContentType(), head);
	}

	private void assertSafePath(String path) {
		UploadSafetyUtils.assertSafePath(path);
	}

	private void assertSafeUpload(String fileName, String contentType, byte[] headBytes) {
		UploadSafetyUtils.assertSafeUpload(fileName, contentType, headBytes);
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

	/**
	 * 调七牛 avinfo 解析视频时长（秒）。
	 * 应对「原始上传文件」key（如 mp4），勿对尚未生成的转码 m3u8 调用。
	 * @return 时长秒；失败返回 null
	 */
	public static Integer fetchDurationSeconds(String keyOrUrl) {
		return getInstance().doFetchDurationSeconds(keyOrUrl);
	}

	private Integer doFetchDurationSeconds(String keyOrUrl) {
		String key = qiniuConfig.extractKey(keyOrUrl);
		if (key == null || key.isEmpty()) {
			return null;
		}
		// 公有直链或私有签名 URL，再挂 ?avinfo
		String baseUrl = qiniuConfig.toAccessUrl(key, qiniuConfig.getUrlExpireSeconds(), qiniuAuth);
		if (baseUrl == null || baseUrl.isEmpty()) {
			return null;
		}
		String avinfoUrl = baseUrl.contains("?") ? baseUrl + "&avinfo" : baseUrl + "?avinfo";
		Request request = new Request.Builder().url(avinfoUrl).get().build();
		try (okhttp3.Response response = AVINFO_HTTP.newCall(request).execute()) {
			if (response.body() == null || !response.isSuccessful()) {
				log.warn("qiniu avinfo http fail key={} code={}", key, response.code());
				return null;
			}
			String body = response.body().string();
			JSONObject root = JSON.parseObject(body);
			if (root == null) {
				return null;
			}
			JSONObject format = root.getJSONObject("format");
			if (format == null) {
				return null;
			}
			String durationText = format.getString("duration");
			if (durationText == null || durationText.isEmpty()) {
				return null;
			}
			double seconds = Double.parseDouble(durationText);
			if (seconds <= 0 || Double.isNaN(seconds)) {
				return null;
			}
			return (int) Math.round(seconds);
		} catch (Exception e) {
			log.warn("qiniu avinfo parse fail key={}: {}", key, e.getMessage());
			return null;
		}
	}
}
