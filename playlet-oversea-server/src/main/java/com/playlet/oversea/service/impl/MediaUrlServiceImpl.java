package com.playlet.oversea.service.impl;

import com.playlet.oversea.config.QiniuConfig;
import com.playlet.oversea.constants.QiniuConstants;
import com.playlet.oversea.service.MediaUrlService;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MediaUrlServiceImpl implements MediaUrlService {

	@Autowired
	private QiniuConfig qiniuConfig;

	@Autowired
	private Auth qiniuAuth;

	@Override
	public String sign(String keyOrUrl) {
		String key = qiniuConfig.extractKey(keyOrUrl);
		// 仅图片追加压缩参数，避免 APK 等非图片资源被 imageView2 破坏
		String imageFop = isImageKey(key) ? QiniuConstants.IMAGE_VIEW2_COMPRESS : null;
		return qiniuConfig.toAccessUrl(keyOrUrl, qiniuConfig.getUrlExpireSeconds(), qiniuAuth, imageFop);
	}

	@Override
	public String signVideo(String keyOrUrl) {
		String key = qiniuConfig.extractKey(keyOrUrl);
		if (key != null && key.toLowerCase().endsWith(".m3u8")) {
			return qiniuConfig.toPrivateM3u8Url(keyOrUrl, qiniuConfig.getVideoExpireSeconds(), qiniuAuth);
		}
		return qiniuConfig.toAccessUrl(keyOrUrl, qiniuConfig.getVideoExpireSeconds(), qiniuAuth);
	}

	/** 按后缀判断是否图片资源 */
	private static boolean isImageKey(String key) {
		if (key == null || key.isEmpty()) {
			return false;
		}
		String lower = key.toLowerCase();
		int q = lower.indexOf('?');
		if (q >= 0) {
			lower = lower.substring(0, q);
		}
		return lower.endsWith(".jpg")
				|| lower.endsWith(".jpeg")
				|| lower.endsWith(".png")
				|| lower.endsWith(".gif")
				|| lower.endsWith(".webp")
				|| lower.endsWith(".bmp")
				|| lower.endsWith(".heic")
				|| lower.endsWith(".heif");
	}
}
