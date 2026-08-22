package com.playlet.internal.service.support;

import com.playlet.internal.api.response.OnlineCountRespEntity;
import com.playlet.internal.config.heard.DeviceIdContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.utils.RedisUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端在线：Redis ZSet 按设备心跳（member=deviceId，含未登录）。
 * 已登录时可选顺带记 uid 日活。
 */
@Slf4j
@Service
public class UserOnlineHeartbeatService {

	/** 设备 ID 最大长度，防异常超长写入 */
	private static final int DEVICE_ID_MAX_LEN = 128;

	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private UserActiveStatService userActiveStatService;

	/**
	 * 设备心跳；uid 非空时额外记日活。
	 */
	public void heartbeat(String deviceId, Integer uid) {
		String id = normalizeDeviceId(deviceId);
		if (id == null) {
			return;
		}
		try {
			long now = System.currentTimeMillis();
			redisUtil.zAdd(RedisKeyConstants.ONLINE_DEVICE_ZSET, id, now);
			purgeStale(now);
			if (uid != null && uid > 0) {
				userActiveStatService.markActive(uid);
			}
		} catch (Exception e) {
			log.warn("online heartbeat failed deviceId={}: {}", id, e.getMessage());
		}
	}

	/**
	 * 主动下线：从在线设备集合移除。
	 */
	public void offline(String deviceId) {
		String id = normalizeDeviceId(deviceId);
		if (id == null) {
			return;
		}
		try {
			redisUtil.zRemove(RedisKeyConstants.ONLINE_DEVICE_ZSET, id);
		} catch (Exception e) {
			log.warn("online offline failed deviceId={}: {}", id, e.getMessage());
		}
	}

	/**
	 * 统计窗口内在线设备数。
	 */
	public OnlineCountRespEntity countOnline() {
		long now = System.currentTimeMillis();
		long windowMs = RedisKeyConstants.ONLINE_WINDOW_SEC * 1000L;
		purgeStale(now);
		long count = redisUtil.zCount(RedisKeyConstants.ONLINE_DEVICE_ZSET, now - windowMs, Double.MAX_VALUE);
		OnlineCountRespEntity resp = new OnlineCountRespEntity();
		resp.setOnlineCount(count);
		resp.setWindowSeconds(RedisKeyConstants.ONLINE_WINDOW_SEC);
		resp.setServerTimeMs(now);
		return resp;
	}

	/** 规范化设备 ID；非法返回 null。 */
	public static String normalizeDeviceId(String deviceId) {
		if (StringUtils.isEmpty(deviceId)) {
			return null;
		}
		String id = deviceId.trim();
		if (id.isEmpty() || id.length() > DEVICE_ID_MAX_LEN) {
			return null;
		}
		return id;
	}

	/**
	 * 从请求头 x-playlet-deviceid 解析设备 ID（优先 header，其次 ThreadLocal）。
	 */
	public static String resolveFromRequest(HttpServletRequest request) {
		if (request != null) {
			String fromHeader = normalizeDeviceId(request.getHeader(Constants.HEADER_DEVICE_ID));
			if (fromHeader != null) {
				return fromHeader;
			}
		}
		return normalizeDeviceId(DeviceIdContext.getDeviceId());
	}

	private void purgeStale(long nowMs) {
		long windowMs = RedisKeyConstants.ONLINE_WINDOW_SEC * 1000L;
		redisUtil.zRemoveRangeByScore(RedisKeyConstants.ONLINE_DEVICE_ZSET, 0, nowMs - windowMs - 1);
	}
}
