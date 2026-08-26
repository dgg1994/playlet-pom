package com.playlet.internal.service.third;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playlet.internal.api.request.RegisterRequest;
import com.playlet.internal.api.response.ThirdUserRegisterResp;
import com.playlet.internal.config.ThirdPartyProperties;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.RsaSignUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * worldPay 等三方 HTTP 调用。
 */
@Slf4j
@Service
public class ThirdService {

    private static final String HEADER_APP_ID = "appId";
    private static final String HEADER_NONCE = "nonce";
    private static final String HEADER_TIMESTAMP = "timestamp";
    private static final String HEADER_SIGN = "sign";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ThirdPartyProperties thirdPartyProperties;
    @Autowired
    private ObjectMapper objectMapper;

    private final static String registerPath = "/api/user/register";

    /**
     * 调用 worldPay 用户注册：鉴权参数放 Header，Body 仅 email/tel。
     *
     * @param email 用户邮箱（必填）
     * @param tel   手机号（可选）
     * @return worldPay uid
     */
    public Long registerUser(String email, String tel) {
        // 获取请求路径
        String registerUrl = thirdPartyProperties.getBaseUrl() + registerPath;
        // 请求体参数
        RegisterRequest body = new RegisterRequest();
        body.setEmail(email.trim());
        if (!StringUtils.isEmpty(tel)) {
            body.setTel(tel.trim());
        }
        String appId = thirdPartyProperties.getAppId();
        String nonce = UUID.randomUUID().toString().replace("-", "");
        // 文档要求 13 位毫秒时间戳
        String timestamp = String.valueOf(System.currentTimeMillis());

        try {
            String sign = RsaSignUtil.generateSign(appId, nonce, timestamp, body,
                    thirdPartyProperties.getPrivateKey());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HEADER_APP_ID, appId);
            headers.set(HEADER_NONCE, nonce);
            headers.set(HEADER_TIMESTAMP, timestamp);
            headers.set(HEADER_SIGN, sign);

            HttpEntity<RegisterRequest> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(registerUrl, entity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new BaseException("三方用户注册失败");
            }
            return parseRegisterUid(response.getBody(), email);
        } catch (Exception e) {
            throw new BaseException("三方用户注册失败", e);
        }
    }

    private Long parseRegisterUid(String responseBody, String email) throws Exception {
        if (StringUtils.isEmpty(responseBody)) {
            throw new BaseException("三方用户注册响应为空");
        }
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode codeNode = root.get("code");
        if (codeNode == null || codeNode.isNull()
                || codeNode.asInt() != Constants.HTTP_RES_CODE_200) {
            String msg = root.has("msg") ? root.get("msg").asText() : "unknown";

            throw new BaseException("三方用户注册失败: " + msg);
        }
        JsonNode dataNode = root.get("data");
        if (dataNode == null || dataNode.isNull() || !dataNode.has("uid")) {
            throw new BaseException("三方用户注册响应缺少 uid");
        }
        ThirdUserRegisterResp resp = objectMapper.treeToValue(dataNode, ThirdUserRegisterResp.class);
        if (resp == null || resp.getUid() == null) {
            throw new BaseException("三方用户注册响应 uid 为空");
        }
        return resp.getUid();
    }

}
