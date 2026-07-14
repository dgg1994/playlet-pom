package com.playlet.internal.utils.oidc;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OidcTokenVerifier {

    public static final String GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    public static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";

    private static final long MAX_TOKEN_AGE_MS = TimeUnit.MINUTES.toMillis(15);
    private static final long CLOCK_SKEW_MS = TimeUnit.MINUTES.toMillis(5);

    public static OidcIdTokenPayload verifyGoogleIdToken(String idToken, List<String> allowedClientIds)
            throws ParseException, IOException, JOSEException {
        SignedJWT jwt = SignedJWT.parse(idToken);
        verifyRsa(jwt, GOOGLE_JWKS_URL);

        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        validateCommonClaims(claims);

        String iss = claims.getIssuer();
        if (!"https://accounts.google.com".equals(iss) && !"accounts.google.com".equals(iss)) {
            throw new JOSEException("google iss invalid");
        }
        List<String> auds = claims.getAudience();
        if (auds == null || auds.isEmpty() || !audienceIntersects(allowedClientIds, auds)) {
            throw new JOSEException("google aud invalid");
        }

        String sub = claims.getSubject();
        String email = claims.getStringClaim("email");
        Boolean emailVerified = claims.getBooleanClaim("email_verified");
        return new OidcIdTokenPayload(sub, email, emailVerified);
    }

    public static OidcIdTokenPayload verifyAppleIdToken(String idToken, List<String> allowedClientIds)
            throws ParseException, IOException, JOSEException {
        SignedJWT jwt = SignedJWT.parse(idToken);
        verifyRsa(jwt, APPLE_JWKS_URL);

        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        validateCommonClaims(claims);

        String iss = claims.getIssuer();
        if (!"https://appleid.apple.com".equals(iss)) {
            throw new JOSEException("apple iss invalid");
        }
        List<String> auds = claims.getAudience();
        if (auds == null || auds.isEmpty() || !audienceIntersects(allowedClientIds, auds)) {
            throw new JOSEException("apple aud invalid");
        }

        String sub = claims.getSubject();
        String email = claims.getStringClaim("email");
        Boolean emailVerified = null;
        Object ev = claims.getClaim("email_verified");
        if (ev instanceof Boolean) {
            emailVerified = (Boolean) ev;
        } else if (ev instanceof String) {
            emailVerified = Boolean.parseBoolean((String) ev);
        }
        return new OidcIdTokenPayload(sub, email, emailVerified);
    }

    private static boolean audienceIntersects(List<String> allowed, List<String> tokenAud) {
        if (allowed == null || allowed.isEmpty() || tokenAud == null || tokenAud.isEmpty()) {
            return false;
        }
        for (String a : tokenAud) {
            if (a != null && allowed.contains(a)) {
                return true;
            }
        }
        return false;
    }

    private static void validateCommonClaims(JWTClaimsSet claims) throws JOSEException {
        long now = System.currentTimeMillis();
        Date exp = claims.getExpirationTime();
        if (exp == null || exp.before(new Date(now - CLOCK_SKEW_MS))) {
            throw new JOSEException("token expired");
        }
        if (claims.getSubject() == null || claims.getSubject().trim().isEmpty()) {
            throw new JOSEException("sub missing");
        }
        Date iat = claims.getIssueTime();
        if (iat != null) {
            if (iat.after(new Date(now + CLOCK_SKEW_MS))) {
                throw new JOSEException("iat in future");
            }
            if (now - iat.getTime() > MAX_TOKEN_AGE_MS + CLOCK_SKEW_MS) {
                throw new JOSEException("token too old");
            }
        }
    }

    private static void verifyRsa(SignedJWT jwt, String jwksUrl) throws IOException, ParseException, JOSEException {
        JWSHeader header = jwt.getHeader();
        if (header.getAlgorithm() == null || !JWSAlgorithm.RS256.equals(header.getAlgorithm())) {
            throw new JOSEException("alg not RS256");
        }
        String kid = header.getKeyID();
        if (kid == null || kid.trim().isEmpty()) {
            throw new JOSEException("kid missing");
        }

        JWKSet jwkSet = OidcJwksCache.get(jwksUrl);
        JWK jwk = jwkSet.getKeyByKeyId(kid);
        if (jwk == null) {
            jwkSet = OidcJwksCache.get(jwksUrl, true);
            jwk = jwkSet.getKeyByKeyId(kid);
        }
        if (jwk == null) {
            throw new JOSEException("jwk not found");
        }
        RSAKey rsaKey = jwk.toRSAKey();
        RSASSAVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
        if (!jwt.verify(verifier)) {
            throw new JOSEException("signature invalid");
        }
    }
}
