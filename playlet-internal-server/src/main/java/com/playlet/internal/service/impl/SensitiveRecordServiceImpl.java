package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.SensitiveRecordEntity;
import com.playlet.internal.base.SensitiveCheckResult;
import com.playlet.internal.dao.security.IllegalCommentRecordDao;
import com.playlet.internal.entity.security.IllegalCommentRecordEntity;
import com.playlet.internal.security.sensitive.SensitiveMatch;
import com.playlet.internal.service.SensitiveRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * 类描述：敏感词处理
 *
 * @author GeminiSun
 * @date 2026/08/12 11:54
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class SensitiveRecordServiceImpl implements SensitiveRecordService {

    @Resource
    private IllegalCommentRecordDao illegalCommentRecordDao;


    /**
     * 保存违规记录
     *
     * @param dto    基础信息
     * @param result 敏感检测结果
     */
    public void saveRecord(SensitiveRecordEntity dto, SensitiveCheckResult result) {
        if (result == null || result.getPass()) {
            return;
        }
        IllegalCommentRecordEntity entity = new IllegalCommentRecordEntity();
        entity.setCommentId(dto.getCommentId());
        entity.setUserId(dto.getUserId());
        entity.setDramaId(dto.getDramaId());
        entity.setEpisodeId(dto.getEpisodeId());
        entity.setContent(dto.getContent());
        /**
         * 敏感词拼接
         */
        String words = result.getMatches()
                .stream()
                .map(
                        SensitiveMatch::getWord
                )
                .collect(
                        Collectors.joining(",")
                );
        entity.setSensitiveWords(words);
        entity.setRiskLevel(result.getLevel());

        /**
         * 默认待处理
         */
        entity.setStatus(0);
        entity.setSourceType(dto.getSourceType());
        illegalCommentRecordDao.insert(entity);
    }
}