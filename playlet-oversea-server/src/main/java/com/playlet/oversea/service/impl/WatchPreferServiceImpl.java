package com.playlet.oversea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.playlet.oversea.api.response.WatchPreferRespEntity;
import com.playlet.oversea.api.response.WatchPreferTagRespEntity;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.prefer.WatchPreferCategoryDao;
import com.playlet.oversea.dao.prefer.WatchPreferTagDao;
import com.playlet.oversea.entity.prefer.WatchPreferCategoryEntity;
import com.playlet.oversea.entity.prefer.WatchPreferTagEntity;
import com.playlet.oversea.service.WatchPreferService;
import com.playlet.oversea.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 类描述：偏好
 *
 * @author GeminiSun
 * @date 2026/10/10 11:30
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WatchPreferServiceImpl implements WatchPreferService {

    @Autowired
    private WatchPreferCategoryDao watchPreferCategoryDao;

    @Autowired
    private WatchPreferTagDao watchPreferTagDao;

    @Override
    public ResponseBase list(HttpServletRequest request) {
        List<WatchPreferRespEntity>  list = new ArrayList<>();
        List<WatchPreferCategoryEntity> watchPreferCategoryEntities = watchPreferCategoryDao.selectList(null);
        for (WatchPreferCategoryEntity watchPreferCategoryEntity : watchPreferCategoryEntities) {
            WatchPreferRespEntity watchPreferRespEntity = new WatchPreferRespEntity();
            watchPreferRespEntity.setCategoryCode(watchPreferCategoryEntity.getCategoryCode());
            watchPreferRespEntity.setCategoryName(watchPreferCategoryEntity.getCategoryName());
            List<WatchPreferTagRespEntity> watchPreferTagRespEntities = watchPreferTagDao.selectListByCode(watchPreferCategoryEntity.getCategoryCode());
            if (watchPreferTagRespEntities != null && !watchPreferTagRespEntities.isEmpty()) {
                watchPreferRespEntity.setWatchPreferTagRespEntities(watchPreferTagRespEntities);
            }
            list.add(watchPreferRespEntity);
        }
        return setResultSuccess(list, I18nUtil.getMessage("base_success"));
    }
}