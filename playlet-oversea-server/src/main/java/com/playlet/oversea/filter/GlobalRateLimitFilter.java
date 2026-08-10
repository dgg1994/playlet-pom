package com.playlet.oversea.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.playlet.oversea.utils.RedisUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Component
@Order(1) // 确保优先执行
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(GlobalRateLimitFilter.class);

    @Autowired
    private RedisUtil redisUtil;

    // 每秒最大请求次数
    private static final int MAX_REQUESTS_PER_SECOND = 3;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 获取客户端IP和请求URI
        String ip = getClientIp(request);
        String uri = request.getRequestURI();

//        logger.info("请求IP：{}，请求URI：{}", ip, uri);

        // 构造 Redis Key: IP + URI + 当前秒
        String key = String.format("rate_limit:%s:%s", ip, uri);
        long secondKey = Instant.now().getEpochSecond();
        String redisKey = key + ":" + secondKey;

        // 自增计数
        Long count = redisUtil.incr(redisKey, 1);
        if (count == 1) {
            redisUtil.expire(redisKey, 2); // 2秒后过期
        }

        // 超过限制返回 429
        if (count > MAX_REQUESTS_PER_SECOND) {
            logger.warn("限流触发 - IP: {}, URI: {}, 当前计数: {}", ip, uri, count);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"rtncode\":429,\"msg\":\"操作太快，请稍后再试\"}");
            return;
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 获取客户端IP。仅当直连来源为内网/本机（视为受信任反向代理）时才采信 X-Forwarded-For / X-Real-IP。
     */
    private String getClientIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        if (!isTrustedProxy(remote)) {
            return remote == null ? "unknown" : remote;
        }
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty() && !"unknown".equalsIgnoreCase(xfHeader)) {
            return xfHeader.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp.trim();
        }
        return remote == null ? "unknown" : remote;
    }

    /** 本机或私网地址视为前置代理 */
    private static boolean isTrustedProxy(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        if ("127.0.0.1".equals(ip) || "https://example.net/id/garnet".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            try {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                return second >= 16 && second <= 31;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
