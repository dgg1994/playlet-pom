package com.playlet.internal.service;

/**
 * 媒体地址：库内存七牛 key，出参时签名（或公有直链）。
 */
public interface MediaUrlService {

	/** 封面等普通资源 */
	String sign(String keyOrUrl);

	/** 视频 / m3u8，使用更长过期时间 */
	String signVideo(String keyOrUrl);
}
