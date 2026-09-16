package com.playlet.internal.constants;

/**
 * 七牛云相关常量
 */
public final class QiniuConstants {

	/** 默认签名有效期（秒）：封面等 */
	public static final long DEFAULT_URL_EXPIRE_SECONDS = 3600L;

	/** 视频签名有效期（秒） */
	public static final long DEFAULT_VIDEO_EXPIRE_SECONDS = 7200L;

	/** 读图压缩：最长边 800、质量 75，降低流量与前端内存 */
	public static final String IMAGE_VIEW2_COMPRESS = "imageView2/2/w/800/q/75";

	private QiniuConstants() {
	}
}
