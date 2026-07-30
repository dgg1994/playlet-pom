package com.playlet.internal.utils;

import cn.jiguang.sdk.api.PushApi;
import cn.jiguang.sdk.bean.push.PushSendParam;
import cn.jiguang.sdk.bean.push.PushSendResult;
import cn.jiguang.sdk.bean.push.audience.Audience;
import cn.jiguang.sdk.bean.push.message.notification.NotificationMessage;
import cn.jiguang.sdk.bean.push.options.Options;
import cn.jiguang.sdk.constants.ApiConstants;
import cn.jiguang.sdk.enums.platform.Platform;
import com.alibaba.fastjson.JSONObject;
import com.playlet.internal.api.request.JpushReqEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 极光推送工具类
 */
public class JPushUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(JPushUtils.class);
    
    /** PushApi 实例 */
    private static PushApi PUSH_API;
    
    /**生产环境测试环境*/
    private static boolean ENVIRONMENT;
    
    /** 异步推送线程池 */
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(10);
    
    /**
     * 初始化极光推送（由 Spring 配置类调用）
     * @param appKey 极光应用的 AppKey
     * @param masterSecret 极光主密钥
     * @param environment 
     */
    public static void init(String appKey, String masterSecret, boolean environment) {
        try {
            LOG.info("开始初始化极光推送");
            PUSH_API = new PushApi.Builder()
                    .setAppKey(appKey)
                    .setMasterSecret(masterSecret)
                    .build();
            ENVIRONMENT = environment;
            LOG.info("极光推送初始化成功");
        } catch (Exception e) {
            LOG.error("极光推送初始化失败", e);
            throw new RuntimeException("极光推送初始化失败", e);
        }
    }
    
    /**
     * 关闭线程池（在应用关闭时调用）
     */
    public static void shutdown() {
        if (ASYNC_EXECUTOR != null && !ASYNC_EXECUTOR.isShutdown()) {
            ASYNC_EXECUTOR.shutdown();
            LOG.info("极光推送线程池已关闭");
        }
    }
    
    /**
     * 检查是否已初始化
     */
    private static void checkInitialized() {
        if (PUSH_API == null) {
            throw new RuntimeException("极光推送未初始化，请检查配置");
        }
    }

    /**
     * 推送消息（同步）
     */
    public static void send(JpushReqEntity pushVo) {
        if (pushVo == null) {
            LOG.error("[极光推送] 推送参数为空");
            return;
        }
        if (PUSH_API == null) {
            LOG.warn("[极光推送] 未初始化，跳过推送");
            return;
        }

        // 构建推送参数
        PushSendParam param = new PushSendParam();
        
        // 1. 构建通知消息
        param.setNotification(buildNotificationMessage(pushVo));
        
        // 2. 设置目标受众
        if (pushVo.isBroadcasting()) {
            param.setAudience(ApiConstants.Audience.ALL);
            LOG.info("[极光推送] 广播模式");
        } else {
            param.setAudience(buildAudience(pushVo));
            LOG.info("[极光推送] 定向模式, registrationIdList={}, aliasList={}", 
                     pushVo.getRegistrationIdList(), pushVo.getAliasList());
        }

        // 3. 设置推送平台（Android + iOS）
        param.setPlatform(Arrays.asList(Platform.android, Platform.ios));

        // 4. 配置推送选项（可选）
        Options options = new Options();
        options.setApnsProduction(ENVIRONMENT); 
        param.setOptions(options);

        try {
            LOG.info("[极光推送] 请求参数: {}", JSONObject.toJSONString(param));
            PushSendResult result = PUSH_API.send(param);
            LOG.info("[极光推送] 推送成功: msgId={}", result.getMessageId());
        } catch (Exception e) {
            LOG.error("[极光推送] 系统异常: {}", e.getMessage(), e);
        }
    }
    
    // ==================== 异步推送方法 ====================
    
    /**
     * 异步推送消息
     */
    public static CompletableFuture<Void> sendAsync(JpushReqEntity pushVo) {
        return CompletableFuture.runAsync(() -> send(pushVo), ASYNC_EXECUTOR);
    }
    
    /**
     * 异步推送：根据设备ID（registrationId）推送
     * @param registrationId 设备注册ID
     * @param title 标题
     * @param msg 消息内容
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> sendToDeviceAsync(String registrationId, String title, String msg) {
        return CompletableFuture.runAsync(() -> {
            try {
                LOG.info("[异步推送] 开始推送到设备: registrationId={}", registrationId);
                sendToDevice(registrationId, title, msg);
                LOG.info("[异步推送] 推送到设备成功: registrationId={}", registrationId);
            } catch (Exception e) {
                LOG.error("[异步推送] 推送到设备失败: registrationId={}", registrationId, e);
            }
        }, ASYNC_EXECUTOR);
    }
    
    /**
     * 异步推送：根据用户ID（别名）推送
     * @param userId 用户ID/别名
     * @param title 标题
     * @param msg 消息内容
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> sendToUserAsync(String userId, String title, String msg) {
        return CompletableFuture.runAsync(() -> {
            try {
                LOG.info("[异步推送] 开始推送到用户: userId={}", userId);
                sendToUser(userId, title, msg);
                LOG.info("[异步推送] 推送到用户成功: userId={}", userId);
            } catch (Exception e) {
                LOG.error("[异步推送] 推送到用户失败: userId={}", userId, e);
            }
        }, ASYNC_EXECUTOR);
    }
    
    /**
     * 异步推送：广播推送
     * @param title 标题
     * @param msg 消息内容
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> sendToAllAsync(String title, String msg) {
        return CompletableFuture.runAsync(() -> {
            try {
                LOG.info("[异步推送] 开始广播推送");
                sendToAll(title, msg);
                LOG.info("[异步推送] 广播推送成功");
            } catch (Exception e) {
                LOG.error("[异步推送] 广播推送失败", e);
            }
        }, ASYNC_EXECUTOR);
    }
    
    /**
     * 异步推送：批量推送到多个设备
     * @param registrationIds 设备ID列表
     * @param title 标题
     * @param msg 消息内容
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> sendToDevicesAsync(List<String> registrationIds, String title, String msg) {
        return CompletableFuture.runAsync(() -> {
            try {
                LOG.info("[异步推送] 开始批量推送到设备: count={}", registrationIds.size());
                JpushReqEntity pushVo = new JpushReqEntity();
                pushVo.setTitle(title);
                pushVo.setMsg(msg);
                pushVo.setBroadcasting(false);
                pushVo.setRegistrationIdList(registrationIds);
                send(pushVo);
                LOG.info("[异步推送] 批量推送完成: count={}", registrationIds.size());
            } catch (Exception e) {
                LOG.error("[异步推送] 批量推送失败", e);
            }
        }, ASYNC_EXECUTOR);
    }
    
    /**
     * 异步推送：批量推送到多个用户
     * @param userIds 用户ID列表
     * @param title 标题
     * @param msg 消息内容
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> sendToUsersAsync(List<String> userIds, String title, String msg) {
        return CompletableFuture.runAsync(() -> {
            try {
                LOG.info("[异步推送] 开始批量推送到用户: count={}", userIds.size());
                JpushReqEntity pushVo = new JpushReqEntity();
                pushVo.setTitle(title);
                pushVo.setMsg(msg);
                pushVo.setBroadcasting(false);
                pushVo.setAliasList(userIds);
                send(pushVo);
                LOG.info("[异步推送] 批量推送完成: count={}", userIds.size());
            } catch (Exception e) {
                LOG.error("[异步推送] 批量推送失败", e);
            }
        }, ASYNC_EXECUTOR);
    }

    // ==================== 同步推送方法（原有方法） ====================
    
    /**
     * 便捷方法：根据用户ID（别名）推送（同步）
     */
    public static void sendToUser(String userId, String title, String msg) {
        JpushReqEntity pushVo = new JpushReqEntity();
        pushVo.setTitle(title);
        pushVo.setMsg(msg);
        pushVo.setBroadcasting(false);
        pushVo.setAliasList(Collections.singletonList(userId));
        send(pushVo);
    }

    /**
     * 便捷方法：根据设备ID（registrationId）推送（同步）
     */
    public static void sendToDevice(String registrationId, String title, String msg) {
        JpushReqEntity pushVo = new JpushReqEntity();
        pushVo.setTitle(title);
        pushVo.setMsg(msg);
        pushVo.setBroadcasting(false);
        pushVo.setRegistrationIdList(Collections.singletonList(registrationId));
        send(pushVo);
    }

    /**
     * 便捷方法：广播推送（同步）
     */
    public static void sendToAll(String title, String msg) {
        JpushReqEntity pushVo = new JpushReqEntity();
        pushVo.setTitle(title);
        pushVo.setMsg(msg);
        pushVo.setBroadcasting(true);
        send(pushVo);
    }

    // -------------------- private method --------------------

    /**
     * 构建Audience对象（优先使用registrationId）
     */
    private static Audience buildAudience(JpushReqEntity pushVo) {
        Audience audience = new Audience();
        if (pushVo.getRegistrationIdList() != null && !pushVo.getRegistrationIdList().isEmpty()) {
            audience.setRegistrationIdList(pushVo.getRegistrationIdList());
        } else if (pushVo.getAliasList() != null && !pushVo.getAliasList().isEmpty()) {
            audience.setAliasList(pushVo.getAliasList());
        }
        return audience;
    }

    /**
     * 构建通知消息
     */
    private static NotificationMessage buildNotificationMessage(JpushReqEntity pushVo) {
        String title = pushVo.getTitle();
        String msg = pushVo.getMsg();
        Map<String, Object> extrasMap = pushVo.getExtrasMap();

        NotificationMessage.Android android = new NotificationMessage.Android();
        android.setTitle(title);
        android.setAlert(msg);
        if (extrasMap != null && !extrasMap.isEmpty()) {
            android.setExtras(filterSensitiveExtras(extrasMap));
        }

        NotificationMessage.IOS ios = new NotificationMessage.IOS();
        Map<String, String> iosAlert = new HashMap<>();
        iosAlert.put("title", title);
        iosAlert.put("subtitle", msg);
        ios.setAlert(iosAlert);
        ios.setBadge("+1");
        ios.setSound("default");
        if (extrasMap != null && !extrasMap.isEmpty()) {
            ios.setExtras(filterSensitiveExtras(extrasMap));
        }

        NotificationMessage notification = new NotificationMessage();
        notification.setAlert(msg);
        notification.setAndroid(android);
        notification.setIos(ios);

        return notification;
    }

    /**
     * 过滤敏感字段
     */
    private static Map<String, Object> filterSensitiveExtras(Map<String, Object> param) {
        if (ObjectUtils.isEmpty(param)) {
            return new HashMap<>();
        }
        param.remove("token");
        param.remove("password");
        param.remove("secret");
        param.remove("authorization");
        return param;
    }
}