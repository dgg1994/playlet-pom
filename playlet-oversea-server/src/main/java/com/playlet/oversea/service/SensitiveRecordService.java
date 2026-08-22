package com.playlet.oversea.service;

import com.playlet.oversea.api.request.SensitiveRecordEntity;
import com.playlet.oversea.base.SensitiveCheckResult;
import org.springframework.stereotype.Service;

/**
 * 类描述：敏感词公共服务
 *
 * @author GeminiSun
 * @date 2026/08/12 10:45
 */
@Service
public interface SensitiveRecordService {


    /**
     * 保存违规记录
     *
     * @param dto    基础信息
     * @param result 敏感检测结果
     */
    public void saveRecord(SensitiveRecordEntity dto, SensitiveCheckResult result);


}