package com.playlet.oversea.service.impl;

import com.playlet.oversea.service.CallBackReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类描述：七牛云上传审核回调
 *
 * @author GeminiSun
 * @date 2026/08/14 09:48
 */
@Slf4j
@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
public class CallBackReviewServiceImpl implements CallBackReviewService {

    @Override
    public void callbackReview(String key) {
        System.out.println("key:"+key);
    }
}