package com.playlet.internal.service.impl;

import com.playlet.internal.config.QiniuConfig;
import com.playlet.internal.service.MediaUrlService;
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
		return qiniuConfig.toAccessUrl(keyOrUrl, qiniuConfig.getUrlExpireSeconds(), qiniuAuth);
	}

	@Override
	public String signVideo(String keyOrUrl) {
		String key = qiniuConfig.extractKey(keyOrUrl);
		if (key != null && key.toLowerCase().endsWith(".m3u8")) {
			return qiniuConfig.toPrivateM3u8Url(keyOrUrl, qiniuConfig.getVideoExpireSeconds(), qiniuAuth);
		}
		return qiniuConfig.toAccessUrl(keyOrUrl, qiniuConfig.getVideoExpireSeconds(), qiniuAuth);
	}
}
