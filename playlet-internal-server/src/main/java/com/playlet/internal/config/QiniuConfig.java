package com.playlet.internal.config;

import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class QiniuConfig {

	@Value("${qiniu.access-key}")
	private String accessKey;

	@Value("${qiniu.secret-key}")
	private String secretKey;

	@Value("${qiniu.bucket}")
	private String bucket;

	@Value("${qiniu.domain}")
	private String domain;

	/** 是否私有空间：true 时读链路签发临时下载地址 */
	@Value("${qiniu.private-enabled:false}")
	private boolean privateEnabled;

	@Value("${qiniu.url-expire-seconds:3600}")
	private long urlExpireSeconds;

	@Value("${qiniu.video-expire-seconds:7200}")
	private long videoExpireSeconds;

	@Value("${qiniu.pm3u8-expires-seconds:43200}")
	private long pm3u8ExpiresSeconds;

	@PostConstruct
	public void init() {
		log.info("========== 七牛云配置加载 ==========");
		log.info("access-key: {}", maskKey(accessKey));
		log.info("secret-key: {}", maskKey(secretKey));
		log.info("bucket: {}", bucket);
		log.info("domain: {}", domain);
		log.info("private-enabled: {}", privateEnabled);
		log.info("url-expire-seconds: {}", urlExpireSeconds);
		log.info("video-expire-seconds: {}", videoExpireSeconds);
		log.info("pm3u8-expires-seconds: {}", pm3u8ExpiresSeconds);
		log.info("====================================");
	}

	private String maskKey(String key) {
		if (key == null || key.isEmpty()) {
			return "null 或 空字符串";
		}
		if (key.length() <= 10) {
			return "*** (长度: " + key.length() + ")";
		}
		return key.substring(0, 6) + "****" + key.substring(key.length() - 4);
	}

    @Bean
    public Auth auth() {
        log.info("【七牛云】创建 Auth 对象，access-key: {}", maskKey(accessKey));
        return Auth.create(accessKey, secretKey);
    }

    @Bean
    public UploadManager uploadManager() {
        log.info("【七牛云】创建 UploadManager，使用 autoRegion");
        // 使用全限定名创建七牛云的Configuration对象
        com.qiniu.storage.Configuration cfg = 
            new com.qiniu.storage.Configuration(Region.autoRegion());
        return new UploadManager(cfg);
    }

	/** 公有拼接：domain + key（无签名） */
	public String buildPublicUrl(String key) {
		if (key == null || key.isEmpty()) {
			return key;
		}
		String path = key.startsWith("/") ? key.substring(1) : key;
		String baseUrl = domain.endsWith("/") ? domain : domain + "/";
		return baseUrl + path;
	}

	/**
	 * @deprecated 使用 {@link #buildPublicUrl(String)}；历史上曾直接返回公有 URL
	 */
	@Deprecated
	public String getFileUrl(String fileName) {
		return buildPublicUrl(fileName);
	}

	/**
	 * 从 key 或历史完整 URL 中提取对象 key（去掉域名与 query）。
	 */
	public String extractKey(String keyOrUrl) {
		if (keyOrUrl == null || keyOrUrl.isEmpty()) {
			return keyOrUrl;
		}
		String value = keyOrUrl.trim();
		int q = value.indexOf('?');
		if (q >= 0) {
			value = value.substring(0, q);
		}
		if (value.contains("://")) {
			String normalizedDomain = domain == null ? "" : domain.trim();
			if (normalizedDomain.endsWith("/")) {
				normalizedDomain = normalizedDomain.substring(0, normalizedDomain.length() - 1);
			}
			if (!normalizedDomain.isEmpty() && value.startsWith(normalizedDomain + "/")) {
				value = value.substring(normalizedDomain.length() + 1);
			} else {
				int scheme = value.indexOf("://");
				int pathStart = value.indexOf('/', scheme + 3);
				if (pathStart >= 0 && pathStart + 1 < value.length()) {
					value = value.substring(pathStart + 1);
				}
			}
		}
		while (value.startsWith("/")) {
			value = value.substring(1);
		}
		return value;
	}

	/**
	 * 读时访问地址：私有空间签发临时 URL；公有空间返回直链。
	 * 入参可为 key 或历史完整 URL。
	 */
	public String toAccessUrl(String keyOrUrl, Long expireSeconds, Auth auth) {
		if (keyOrUrl == null || keyOrUrl.isEmpty()) {
			return keyOrUrl;
		}
		String key = extractKey(keyOrUrl);
		if (key == null || key.isEmpty()) {
			return keyOrUrl;
		}
		String publicUrl = buildPublicUrl(key);
		if (!privateEnabled) {
			return publicUrl;
		}
		long expire = expireSeconds == null || expireSeconds <= 0
				? urlExpireSeconds
				: expireSeconds;
		return auth.privateDownloadUrl(publicUrl, expire);
	}

	/**
	 * 私有 m3u8 播放地址：通过 pm3u8 批量为 ts 分片授权。
	 */
	public String toPrivateM3u8Url(String keyOrUrl, Long expireSeconds, Auth auth) {
		if (keyOrUrl == null || keyOrUrl.isEmpty()) {
			return keyOrUrl;
		}
		String key = extractKey(keyOrUrl);
		if (key == null || key.isEmpty()) {
			return keyOrUrl;
		}
		String publicUrl = buildPublicUrl(key);
		if (!privateEnabled) {
			return publicUrl;
		}
		long expire = expireSeconds == null || expireSeconds <= 0
				? videoExpireSeconds
				: expireSeconds;
		String pm3u8Url = publicUrl + "?pm3u8/0/expires/" + pm3u8ExpiresSeconds;
		return auth.privateDownloadUrl(pm3u8Url, expire);
	}

	public String getBucket() {
		return bucket;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public String getDomain() {
		return domain;
	}

	public boolean isPrivateEnabled() {
		return privateEnabled;
	}

	public long getUrlExpireSeconds() {
		return urlExpireSeconds;
	}

	public long getVideoExpireSeconds() {
		return videoExpireSeconds;
	}

	public long getPm3u8ExpiresSeconds() {
		return pm3u8ExpiresSeconds;
	}
}
