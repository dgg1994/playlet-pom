package com.playlet.internal.service.settle.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.request.SettleManageBatchConfirmReqEntity;
import com.playlet.internal.api.request.SettleManageConfirmReqEntity;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.settle.SettleManageDao;
import com.playlet.internal.entity.settle.SettleManageEntity;
import com.playlet.internal.service.settle.SettleManageService;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端分账结算实现
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class SettleManageServiceImpl implements SettleManageService {

    /** 结算状态 */
    public static final int STATUS_PENDING  = 0; // 待结算
    public static final int STATUS_SETTLED  = 1; // 已结算

    @Autowired
    private SettleManageDao settleManageDao;

    @Override
    @SysLogAnnotation(module = "分账结算", type = "POST", remark = "结算单列表")
    public ResponseBase findList(@RequestBody(required = false) SettleManageEntity entity) {
        if (entity == null) {
            entity = new SettleManageEntity();
        }
        // keyword 去空格
        if (entity.getKeyword() != null) {
            entity.setKeyword(entity.getKeyword().trim());
        }
        // SQL 层分页
        PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
        List<SettleManageEntity> list = settleManageDao.findAdminList(entity);
        if (list == null) {
            list = new ArrayList<>();
        }
        PageInfo<SettleManageEntity> page = new PageInfo<>(list);
        return setResultSuccess(page, I18nUtil.getMessage("base_success"));
    }

    @Override
    @SysLogAnnotation(module = "分账结算", type = "POST", remark = "确认单笔结算")
    public ResponseBase confirm(@Valid @RequestBody SettleManageConfirmReqEntity req) {
        try {
            SettleManageEntity row = settleManageDao.selectById(req.getId());
            if (row == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            if (STATUS_SETTLED == row.getStatus()) {
                return setResultError("单据已结算");
            }
            int updated = settleManageDao.markSettledByIds(Collections.singletonList(req.getId()));
            if (updated <= 0) {
                return setResultError("更新失败，请重试");
            }
            log.info("分账结算确认成功 id={} settleNo={}", row.getId(), row.getSettleNo());
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("confirm settle id={} error", req.getId(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @SysLogAnnotation(module = "分账结算", type = "POST", remark = "批量确认结算")
    public ResponseBase batchConfirm(@Valid @RequestBody SettleManageBatchConfirmReqEntity req) {
        try {
            List<Long> ids = req.getIds();
            if (ids == null || ids.isEmpty()) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            // 去重
            ids = new ArrayList<>(new LinkedHashSet<>(ids));

            int updated = settleManageDao.markSettledByIds(ids);
            if (updated <= 0) {
                return setResultError("无可结算的单据");
            }
            log.info("分账批量结算成功 count={}", updated);

            // 返回成功条数
            Map<String, Object> data = new HashMap<>();
            data.put("successCount", updated);
            return setResultSuccess(data, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("batchConfirm settle ids={} error", req.getIds(), e);
            throw new RuntimeException(e);
        }
    }
}
