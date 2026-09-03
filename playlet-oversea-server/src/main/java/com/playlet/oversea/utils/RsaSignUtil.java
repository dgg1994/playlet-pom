package com.playlet.oversea.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * RSA 签名工具。
 */
public final class RsaSignUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final byte[] PKCS8_ALGORITHM_IDENTIFIER = {
            0x30, 0x0d,
            0x06, 0x09, 0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01,
            0x05, 0x00
    };

    private static final byte[] PKCS8_VERSION = {0x02, 0x01, 0x00};

    private RsaSignUtil() {
    }

    public static String generateSign(String appId, String nonce, String timestamp,
                                      Object body, String privateKeyStr) throws Exception {
        String signContent = buildSignContent(appId, nonce, timestamp, body);
        return signData(signContent, privateKeyStr);
    }

    /**
     * 构建签名原文，规则与 pay_demo 保持一致。
     * 格式：appId=..&nonce=..&timestamp=..&{bodyKey}={bodyValue}...
     */
    public static String buildSignContent(String appId, String nonce, String timestamp, Object body)
            throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("appId=").append(nullToEmpty(appId));
        sb.append("&nonce=").append(nullToEmpty(nonce));
        sb.append("&timestamp=").append(nullToEmpty(timestamp));

        if (body != null) {
            Map<String, Object> bodyMap = OBJECT_MAPPER.convertValue(body, Map.class);
            TreeMap<String, Object> sortedMap = sortAndFilterMap(bodyMap);
            // 每个 body 参数前都加 &，避免 timestamp 与首个字段粘连
            for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
                sb.append('&').append(entry.getKey()).append('=').append(entry.getValue());
            }
        }
        return sb.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @SuppressWarnings("unchecked")
    private static TreeMap<String, Object> sortAndFilterMap(Map<String, Object> map) {
        TreeMap<String, Object> sortedMap = new TreeMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null || "".equals(value.toString().trim())) {
                continue;
            }
            if (value instanceof Map) {
                value = sortAndFilterMap((Map<String, Object>) value);
            }
            sortedMap.put(entry.getKey(), value);
        }
        return sortedMap;
    }

    private static String signData(String data, String privateKeyStr) throws Exception {
        PrivateKey privateKey = loadPrivateKey(privateKeyStr);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        byte[] keyBytes = decodeBase64Key(normalizePrivateKey(privateKeyStr));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        try {
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (InvalidKeySpecException ex) {
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(wrapPkcs1InPkcs8(keyBytes)));
        }
    }

    private static String normalizePrivateKey(String privateKeyStr) {
        if (privateKeyStr == null || privateKeyStr.trim().isEmpty()) {
            throw new IllegalArgumentException("privateKey 不能为空");
        }

        String normalized = privateKeyStr.trim();
        if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'"))) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        return normalized
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\\n", "")
                .replace("\\r", "")
                .replaceAll("\\s+", "");
    }

    private static byte[] decodeBase64Key(String base64Key) {
        int remainder = base64Key.length() % 4;
        if (remainder == 1) {
            throw new IllegalArgumentException("privateKey Base64 格式不正确，请检查私钥是否完整复制");
        }
        if (remainder > 0) {
            base64Key = base64Key + "====".substring(remainder);
        }

        try {
            return Base64.getDecoder().decode(base64Key);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("privateKey Base64 解码失败，请检查私钥格式是否完整", ex);
        }
    }

    private static byte[] wrapPkcs1InPkcs8(byte[] pkcs1Bytes) {
        byte[] octetStringHeader = createDerOctetStringHeader(pkcs1Bytes.length);
        int innerLen = PKCS8_VERSION.length + PKCS8_ALGORITHM_IDENTIFIER.length
                + octetStringHeader.length + pkcs1Bytes.length;
        byte[] sequenceHeader = createDerSequenceHeader(innerLen);

        byte[] pkcs8 = new byte[sequenceHeader.length + innerLen];
        int offset = 0;
        offset = copy(sequenceHeader, pkcs8, offset);
        offset = copy(PKCS8_VERSION, pkcs8, offset);
        offset = copy(PKCS8_ALGORITHM_IDENTIFIER, pkcs8, offset);
        offset = copy(octetStringHeader, pkcs8, offset);
        copy(pkcs1Bytes, pkcs8, offset);
        return pkcs8;
    }

    private static byte[] createDerSequenceHeader(int length) {
        return createDerHeader((byte) 0x30, length);
    }

    private static byte[] createDerOctetStringHeader(int length) {
        return createDerHeader((byte) 0x04, length);
    }

    private static byte[] createDerHeader(byte tag, int length) {
        if (length < 128) {
            return new byte[] {tag, (byte) length};
        }
        if (length < 256) {
            return new byte[] {tag, (byte) 0x81, (byte) length};
        }
        return new byte[] {tag, (byte) 0x82, (byte) ((length >> 8) & 0xff), (byte) (length & 0xff)};
    }

    private static int copy(byte[] source, byte[] target, int offset) {
        System.arraycopy(source, 0, target, offset, source.length);
        return offset + source.length;
    }
}
