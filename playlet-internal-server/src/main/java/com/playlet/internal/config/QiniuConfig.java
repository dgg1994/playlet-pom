package com.playlet.internal.config;

import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class QiniuConfig {

    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    @PostConstruct
    public void init() {
        log.info("========== 七牛云配置加载 ==========");
        log.info("access-key: {}", maskKey(accessKey));
        log.info("secret-key: {}", maskKey(secretKey));
        log.info("bucket: {}", bucket);
        log.info("domain: {}", domain);
        log.info("access-key 长度: {}", accessKey == null ? "null" : accessKey.length());
        log.info("secret-key 长度: {}", secretKey == null ? "null" : secretKey.length());
        log.info("====================================");
    }

    /**
     * 脱敏显示密钥（只显示前6位和后4位）
     */
    private String maskKey(String key) {
        if (key == null || key.isEmpty()) {
            return "null 或 空字符串";
        }
        if (key.length() <= 10) {
            return "*** (长度: " + key.length() + ")";
        }
        return key.substring(0, 6) + "****" + key.substring(key.length() - 4);
    }

    @Bean
    public Auth auth() {
        log.info("【七牛云】创建 Auth 对象，access-key: {}", maskKey(accessKey));
        return Auth.create(accessKey, secretKey);
    }

    @Bean
    public UploadManager uploadManager() {
        log.info("【七牛云】创建 UploadManager，使用 autoRegion");
        // 使用全限定名创建七牛云的Configuration对象
        com.qiniu.storage.Configuration cfg = 
            new com.qiniu.storage.Configuration(Region.autoRegion());
        return new UploadManager(cfg);
    }

    public String getFileUrl(String fileName) {
        String baseUrl = domain.endsWith("/") ? domain : domain + "/";
        return baseUrl + fileName;
    }

    public String getBucket() { 
        return bucket; 
    }
    
    public String getAccessKey() {
        return accessKey;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
}