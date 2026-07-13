package com.playlet.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Component
public class IpRegionRouteFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(IpRegionRouteFilter.class);

    private static final String DOMESTIC_SERVICE = "lb://playlet-internal-server";
    private static final String OVERSEA_SERVICE = "lb://playlet-oversea-server";

    @Value("${playlet.gateway.route-mode:3}")
    private int routeMode;

    private final WebClient webClient = WebClient.create("http://ip-api.com");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        // 管理端路由直接放行，不做IP判断
        if (path.startsWith("/china/admin/") || path.startsWith("/global/admin/")) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(exchange);

        Mono<Boolean> isDomestic;

        switch (routeMode) {
            case 1:
                log.info("Route mode: 1 (fixed domestic)");
                isDomestic = Mono.just(true);
                break;
            case 2:
                log.info("Route mode: 2 (fixed oversea)");
                isDomestic = Mono.just(false);
                break;
            default:
                log.info("Route mode: 3 (IP region)");
                isDomestic = checkIpRegion(clientIp);
                break;
        }

        return isDomestic
                .flatMap(domestic -> {
                    String targetUri = domestic ? DOMESTIC_SERVICE : OVERSEA_SERVICE;
                    String region = domestic ? "domestic" : "oversea";
                    log.info("IP: {} -> region: {} -> route to: {}", clientIp, region, targetUri);

                    Route newRoute = Route.async()
                            .id(route.getId())
                            .order(route.getOrder())
                            .asyncPredicate(route.getPredicate())
                            .filters(route.getFilters())
                            .uri(targetUri)
                            .build();

                    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);

                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                            .header("X-Real-IP", clientIp)
                            .header("X-User-Region", region)
                            .build();

                    return chain.filter(exchange.mutate().request(newRequest).build());
                });
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }

    private Mono<Boolean> checkIpRegion(String ip) {
        if (isInternalIp(ip)) {
            return Mono.just(true);
        }

        return webClient.get()
                .uri("/json/{ip}?lang=zh-CN", ip)
                .retrieve()
                .bodyToMono(IpInfo.class)
                .map(info -> "CN".equals(info.getCountryCode()))
                .onErrorReturn(true);
    }

    private boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        return "127.0.0.1".equals(ip)
                || "0:0:0:0:0:0:0:1".equals(ip)
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("172.");
    }

    @Override
    public int getOrder() {
        return -100;
    }

    public static class IpInfo {
        private String status;
        private String country;
        private String countryCode;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    }
}
