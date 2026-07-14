package com.onetoken.utils.oidc;

import com.nimbusds.jose.jwk.JWKSet;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;

public class OidcJwksCache {

    private static final OkHttpClient CLIENT = new OkHttpClient();

    private static class Cached {
        final JWKSet jwkSet;
        final long fetchedAtMs;

        Cached(JWKSet jwkSet, long fetchedAtMs) {
            this.jwkSet = jwkSet;
            this.fetchedAtMs = fetchedAtMs;
        }
    }

    private static final ConcurrentHashMap<String, Cached> CACHE = new ConcurrentHashMap<>();
    private static final long TTL_MS = 60 * 60 * 1000L;

    public static JWKSet get(String jwksUrl) throws IOException, ParseException {
        return get(jwksUrl, false);
    }

    public static JWKSet get(String jwksUrl, boolean forceRefresh) throws IOException, ParseException {
        long now = System.currentTimeMillis();
        if (!forceRefresh) {
            Cached cached = CACHE.get(jwksUrl);
            if (cached != null && (now - cached.fetchedAtMs) < TTL_MS) {
                return cached.jwkSet;
            }
        } else {
            CACHE.remove(jwksUrl);
        }
        Request request = new Request.Builder().url(jwksUrl).get().build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("fetch jwks failed, code=" + response.code());
            }
            String json = response.body().string();
            JWKSet set = JWKSet.parse(json);
            CACHE.put(jwksUrl, new Cached(set, now));
            return set;
        }
    }
}
