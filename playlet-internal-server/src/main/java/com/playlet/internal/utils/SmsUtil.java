package com.playlet.internal.utils;

import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;

/**
 * 短信发送工具类
 * 基于官方示例实现
 */
@Component
public class SmsUtil extends BaseApiService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SmsUtil.class);
    
    private static String URL;       // 接口地址
    private static String ACCOUNT;   // 账号
    private static String PASSWORD;  // 密码
    
    @Value("${sms.url}")
    public void setUrl(String url) {
        SmsUtil.URL = url;
    }
    
    @Value("${sms.account}")
    public void setAccount(String account) {
        SmsUtil.ACCOUNT = account;
    }
    
    @Value("${sms.password}")
    public void setPassword(String password) {
        SmsUtil.PASSWORD = password;
    }
    
    @PostConstruct
    public void init() {
        LOG.info("短信工具初始化完成, URL={}, ACCOUNT={}", URL, ACCOUNT);
    }
    
    /**
     * 发送验证码短信
     * @param mobile 手机号码
     * @param code 验证码
     * @return 发送结果
     */
    public static ResponseBase sendVerificationCode(String mobile, String code) {
        // 短信内容（英文，避免中文编码问题）
        String content = "Your verification code is: " + code + ", valid for 5 minutes. Please do not disclose it to others. [oneToken]";
        
        try {
            String result = sendPost(mobile, content, null);
            LOG.info("短信发送结果: mobile={}, result={}", mobile, result);
            
            // 根据官方文档，成功返回包含 "mterrcode=000"
            if (result != null && result.contains("mterrcode=000")) {
                return setResultSuccess("验证码发送成功");
            } else {
                LOG.error("短信发送失败: mobile={}, result={}", mobile, result);
                return setResultError("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            LOG.error("短信发送异常: mobile={}", mobile, e);
            return setResultError("验证码发送失败：" + e.getMessage());
        }
    }
    
    /**
     * 发送自定义内容短信
     * @param mobile 手机号码
     * @param content 短信内容
     * @return 发送结果
     */
    public static ResponseBase sendCustomSms(String mobile, String content) {
        try {
            String result = sendPost(mobile, content, null);
            if (result != null && result.contains("mterrcode=000")) {
                return setResultSuccess("短信发送成功");
            } else {
                return setResultError("短信发送失败");
            }
        } catch (Exception e) {
            LOG.error("短信发送异常", e);
            return setResultError("短信发送失败：" + e.getMessage());
        }
    }
    
    /**
     * POST 方式发送短信（完全按照官方示例）
     * @param mobile 手机号码，多个号码使用","分割
     * @param content 短信内容
     * @param sign 自定义发送者号码（可选）
     * @return 返回值定义参见 HTTP 协议文档
     * @throws Exception
     */
    public static String sendPost(String mobile, String content, String sign) throws Exception {
        HttpClient client = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true));
        PostMethod method = new PostMethod();
        
        try {
            org.apache.commons.httpclient.URI base = new org.apache.commons.httpclient.URI(URL, false);
            method.setURI(new org.apache.commons.httpclient.URI(base, "", false));
            
            // 构建请求参数（完全按照官方示例）
            NameValuePair[] params;
            if (sign != null && !sign.trim().isEmpty()) {
                params = new NameValuePair[] {
                    new NameValuePair("command", "MT_REQUEST"),
                    new NameValuePair("cpid", ACCOUNT),
                    new NameValuePair("cppwd", PASSWORD),
                    new NameValuePair("da", mobile),
                    new NameValuePair("sm", content),
                    new NameValuePair("sa", sign)
                };
            } else {
                params = new NameValuePair[] {
                    new NameValuePair("command", "MT_REQUEST"),
                    new NameValuePair("cpid", ACCOUNT),
                    new NameValuePair("cppwd", PASSWORD),
                    new NameValuePair("da", mobile),
                    new NameValuePair("sm", content)
                };
            }
            
            method.setRequestBody(params);
            
            // 设置字符编码
            HttpMethodParams methodParams = new HttpMethodParams();
            methodParams.setContentCharset("UTF-8");
            method.setParams(methodParams);
            
            // 执行请求
            int result = client.executeMethod(method);
            
            if (result == HttpStatus.SC_OK) {
                InputStream in = method.getResponseBodyAsStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                in.close();
                baos.close();
                // 对响应进行 URL 解码（官方示例）
                return URLDecoder.decode(baos.toString(), "UTF-8");
            } else {
                throw new Exception("HTTP ERROR Status: " + method.getStatusCode() + ":" + method.getStatusText());
            }
            
        } finally {
            method.releaseConnection();
        }
    }
    
    /**
     * GET 方式发送短信（完全按照官方示例）
     * @param mobile 手机号码，多个号码使用","分割
     * @param content 短信内容
     * @param sign 自定义发送者号码（可选）
     * @return 返回值定义参见 HTTP 协议文档
     * @throws Exception
     */
    public static String sendGet(String mobile, String content, String sign) throws Exception {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod();
        
        try {
            org.apache.commons.httpclient.URI base = new org.apache.commons.httpclient.URI(URL, false);
            method.setURI(new org.apache.commons.httpclient.URI(base, "", false));
            
            // 构建查询参数
            NameValuePair[] params;
            if (sign != null && !sign.trim().isEmpty()) {
                params = new NameValuePair[] {
                    new NameValuePair("command", "MT_REQUEST"),
                    new NameValuePair("cpid", ACCOUNT),
                    new NameValuePair("cppwd", PASSWORD),
                    new NameValuePair("da", mobile),
                    new NameValuePair("sm", content),
                    new NameValuePair("sa", sign)
                };
            } else {
                params = new NameValuePair[] {
                    new NameValuePair("command", "MT_REQUEST"),
                    new NameValuePair("cpid", ACCOUNT),
                    new NameValuePair("cppwd", PASSWORD),
                    new NameValuePair("da", mobile),
                    new NameValuePair("sm", content)
                };
            }
            
            method.setQueryString(params);
            
            // 执行请求
            int result = client.executeMethod(method);
            
            if (result == HttpStatus.SC_OK) {
                InputStream in = method.getResponseBodyAsStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                in.close();
                baos.close();
                // 对响应进行 URL 解码（官方示例）
                return URLDecoder.decode(baos.toString(), "UTF-8");
            } else {
                throw new Exception("HTTP ERROR Status: " + method.getStatusCode() + ":" + method.getStatusText());
            }
            
        } finally {
            method.releaseConnection();
        }
    }
    
    /**
     * 查询余额
     * @return 余额信息
     * @throws Exception
     */
    public static String queryBalance() throws Exception {
        String balanceUrl = URL.replace("/submit", "/get-balance");
        HttpClient client = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true));
        GetMethod method = new GetMethod();
        
        try {
            org.apache.commons.httpclient.URI base = new org.apache.commons.httpclient.URI(balanceUrl, false);
            method.setURI(base);
            method.setQueryString(new NameValuePair[] {
                new NameValuePair("cpid", ACCOUNT),
                new NameValuePair("cppwd", PASSWORD)
            });
            
            int result = client.executeMethod(method);
            
            if (result == HttpStatus.SC_OK) {
                InputStream in = method.getResponseBodyAsStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                in.close();
                baos.close();
                return URLDecoder.decode(baos.toString(), "UTF-8");
            } else {
                throw new Exception("HTTP ERROR Status: " + method.getStatusCode() + ":" + method.getStatusText());
            }
            
        } finally {
            method.releaseConnection();
        }
    }
    
    /**
     * 批量发送短信
     * @param mobiles 手机号码数组
     * @param content 短信内容
     * @return 发送结果
     */
    public static String sendBatch(String[] mobiles, String content) throws Exception {
        String mobileStr = String.join(",", mobiles);
        return sendPost(mobileStr, content, null);
    }
    
    /**
     * 批量发送短信(带签名)
     * @param mobiles 手机号码数组
     * @param content 短信内容
     * @param sign 签名
     * @return 发送结果
     */
    public static String sendBatch(String[] mobiles, String content, String sign) throws Exception {
        String mobileStr = String.join(",", mobiles);
        return sendPost(mobileStr, content, sign);
    }
    
    /**
     * 解析短信响应，判断是否成功
     * @param response 响应字符串
     * @return 是否成功
     */
    public static boolean isSuccess(String response) {
        if (response == null) {
            return false;
        }
        // 根据官方文档，成功响应包含 mterrcode=000
        return response.contains("mterrcode=000");
    }
    
    public static void main(String[] args) {
        try {
            // 测试发送验证码
            System.out.println("=== 测试发送短信 ===");
            String result = SmsUtil.sendPost("8618162505728", "Your verification code is: 123456, valid for 5 minutes. [oneToken]", null);
            System.out.println("发送结果: " + result);
            System.out.println("是否成功: " + isSuccess(result));
            
            // 测试查询余额
            System.out.println("\n=== 测试查询余额 ===");
            String balance = SmsUtil.queryBalance();
            System.out.println("余额信息: " + balance);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}