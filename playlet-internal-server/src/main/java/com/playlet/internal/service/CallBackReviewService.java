package com.playlet.internal.service;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 类描述：七牛云上传回调
 *
 * @author GeminiSun
 * @date 2026/08/14 09:45
 */
@RequestMapping("/api")
@Api(value = "上传审核回调", tags = "上传审核回调")
public interface CallBackReviewService {

    @RequestMapping("/callbackReview")
    void callbackReview(String key);
}
