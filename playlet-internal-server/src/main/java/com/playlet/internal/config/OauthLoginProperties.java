package com.onetoken.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "oauth")
public class OauthLoginProperties {

    private Google google = new Google();
    private Apple apple = new Apple();

    @Data
    public static class Google {
        private List<String> clientIds = new ArrayList<>();
    }

    @Data
    public static class Apple {
        private String clientId;
    }

    public List<String> resolveAppleClientIds() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (apple.getClientId() != null && !apple.getClientId().trim().isEmpty()) {
            set.add(apple.getClientId().trim());
        }
        return new ArrayList<>(set);
    }
}
