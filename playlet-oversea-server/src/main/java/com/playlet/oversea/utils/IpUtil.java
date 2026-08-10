package com.playlet.oversea.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;


@Component
public class IpUtil {

	/**
	 * 获取客户端真实 IP。仅当 remoteAddr 为内网/本机（受信任代理）时采信转发头。
	 */
	public String getClientIp(HttpServletRequest request) {
		String remote = request.getRemoteAddr();
		if (!isLocalIp(remote)) {
			return remote;
		}
		String ip = request.getHeader("X-Forwarded-For");
		if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
			return ip.split(",")[0].trim();
		}
		ip = request.getHeader("X-Real-IP");
		if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
			return ip.trim();
		}
		ip = request.getHeader("Proxy-Client-IP");
		if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}
		ip = request.getHeader("WL-Proxy-Client-IP");
		if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}
		return remote;
	}

	/**
	 * 判断是否为本机或内网 IP
	 */
	public boolean isLocalIp(String ipAddress) {
		if (ipAddress == null || ipAddress.trim().isEmpty()) {
			return false;
		}
		ipAddress = ipAddress.trim();
		if ("127.0.0.1".equals(ipAddress) || "::1".equals(ipAddress)
				|| "https://example.net/id/garnet".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
			return true;
		}
		if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.")) {
			return true;
		}
		if (ipAddress.startsWith("172.")) {
			try {
				int second = Integer.parseInt(ipAddress.split("\\.")[1]);
				return second >= 16 && second <= 31;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

}
