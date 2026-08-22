package com.playlet.oversea.config.heard;

/**
 * 设备 ID 请求头上下文（x-playlet-deviceid）。
 */
public class DeviceIdContext {

	private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

	public static void setDeviceId(String deviceId) {
		CONTEXT.set(deviceId);
	}

	public static String getDeviceId() {
		return CONTEXT.get();
	}

	public static void clear() {
		CONTEXT.remove();
	}
}
