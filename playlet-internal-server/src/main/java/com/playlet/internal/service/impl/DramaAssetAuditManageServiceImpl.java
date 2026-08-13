package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.response.DramaAssetAuditEpisodeRespEntity;
import com.playlet.internal.api.response.DramaAuditDetailRespEntity;
import com.playlet.internal.api.response.DramaWorkAuditListRespEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.drama.*;
import com.playlet.internal.entity.drama.DramaAssetAuditStepEntity;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaAuditStepEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.enums.DramaAssetAuditStatusEnums;
import com.playlet.internal.enums.DramaAssetAuditStepStatusEnums;
import com.playlet.internal.enums.DramaAssetAuditStepTypeEnums;
import com.playlet.internal.query.drama.DramaAssetAuditHandleQuery;
import com.playlet.internal.query.drama.DramaAuditHandleQuery;
import com.playlet.internal.query.drama.DramaWorkAuditQuery;
import com.playlet.internal.service.DramaAssetAuditManageService;
import com.playlet.internal.service.DramaAssetAuditService;
import com.playlet.internal.service.DramaAuditService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class DramaAssetAuditManageServiceImpl implements DramaAssetAuditManageService {

    @Autowired
    private DramaWorkAuditDao dramaWorkAuditDao;
    @Autowired
    private DramaDao dramaDao;
    @Autowired
    private DramaAuditStepDao dramaAuditStepDao;
    @Autowired
    private DramaAuditService dramaAuditService;
    @Autowired
    private DramaAssetDao dramaAssetDao;
    @Autowired
    private DramaAssetAuditStepDao dramaAssetAuditStepDao;
    @Autowired
    private DramaAssetAuditService dramaAssetAuditService;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private MediaUrlService mediaUrlService;

    @Override
    @SysLogAnnotation(module = "作品评审", type = "POST", remark = "作品管理列表")
    public ResponseBase findList(@RequestBody DramaWorkAuditQuery query) {
        if (query == null) {
            query = new DramaWorkAuditQuery();
        }
        PageHelper.startPage(query.getPageNumber(), query.getPageSize());
        List<DramaWorkAuditListRespEntity> list = dramaWorkAuditDao.findDramaList(query);
        if (list != null) {
            String language = StringUtils.isEmpty(query.getLangue())
                    ? LanguageContext.getLanguage()
                    : query.getLangue();
            for (DramaWorkAuditListRespEntity item : list) {
                item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
                if (item.getDramaId() != null) {
                    item.setTagList(tagDao.findGroupLang(language, item.getDramaId()));
                }
            }
        }
        return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
    }

    @Override
    @SysLogAnnotation(module = "作品评审", type = "POST", remark = "短剧集列表")
    public ResponseBase findEpisodeList(@RequestBody DramaWorkAuditQuery query) {
        if (query == null) {
            query = new DramaWorkAuditQuery();
        }
        PageHelper.startPage(query.getPageNumber(), query.getPageSize());
        List<DramaWorkAuditListRespEntity> list = dramaWorkAuditDao.findEpisodeList(query);
        if (list != null) {
            for (DramaWorkAuditListRespEntity item : list) {
                item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
                item.setVideoUrl(mediaUrlService.signVideo(item.getVideoUrl()));
            }
        }
        return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
    }

    @Override
    @SysLogAnnotation(module = "作品评审", type = "POST", remark = "剧评审详情")
    public ResponseBase dramaDetail(@RequestBody DramaEntity entity) {
        if (entity == null || entity.getId() == null) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        // 获取剧信息
        DramaEntity drama = dramaDao.selectById(entity.getId());
        if (drama == null || (drama.getDeleteState() != null && drama.getDeleteState() == 1)) {
            return setResultError(I18nUtil.getMessage("base_data_null"));
        }
        drama.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
        drama.setDramaSteps(dramaAuditStepDao.findByDramaId(drama.getId()));

        DramaWorkAuditQuery episodeQuery = new DramaWorkAuditQuery();
        episodeQuery.setDramaId(drama.getId());
        // 获取集信息
        List<DramaWorkAuditListRespEntity> rawEpisodes = dramaWorkAuditDao.findEpisodeList(episodeQuery);
        List<DramaAssetAuditEpisodeRespEntity> episodeList = new ArrayList<>();
        if (rawEpisodes != null && !rawEpisodes.isEmpty()) {
            List<Integer> assetIds = new ArrayList<>();
            for (DramaWorkAuditListRespEntity item : rawEpisodes) {
                if (item.getAssetId() != null) {
                    assetIds.add(item.getAssetId());
                }
            }
            Map<Integer, List<DramaAssetAuditStepEntity>> stepsByAssetId = new HashMap<>();
            if (!assetIds.isEmpty()) {
                // 获取集审核步骤
                List<DramaAssetAuditStepEntity> allSteps = dramaAssetAuditStepDao.findByAssetIds(assetIds);
                if (allSteps != null) {
                    for (DramaAssetAuditStepEntity step : allSteps) {
                        stepsByAssetId.computeIfAbsent(step.getAssetId(), k -> new ArrayList<>()).add(step);
                    }
                }
            }
            for (DramaWorkAuditListRespEntity item : rawEpisodes) {
                DramaAssetAuditEpisodeRespEntity episode = new DramaAssetAuditEpisodeRespEntity();
                BeanUtils.copyProperties(item, episode);
                episode.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
                episode.setVideoUrl(mediaUrlService.signVideo(item.getVideoUrl()));
                episode.setSteps(stepsByAssetId.getOrDefault(item.getAssetId(), Collections.emptyList()));
                episodeList.add(episode);
            }
            episodeList.sort(Comparator.comparing(e -> e.getSetNum() == null ? 0 : e.getSetNum()));
        }

        // 获取集信息
        DramaAuditDetailRespEntity data = new DramaAuditDetailRespEntity();
        data.setDrama(drama);
        data.setEpisodeList(episodeList);
        return setResultSuccess(data, I18nUtil.getMessage("base_success"));
    }

    @Override
    @SysLogAnnotation(module = "作品评审", type = "POST", remark = "剧评审A/B")
    public ResponseBase dramaHandle(@Valid @RequestBody DramaAuditHandleQuery query, HttpServletRequest request) {
        try {
            // 参数校验
            ResponseBase basicErr = validateHandleBasics(query.getAction(), query.getStepType(), query.getRemark());
            if (basicErr != null) {
                return basicErr;
            }
            // 数据校验
            DramaEntity drama = dramaDao.selectById(query.getDramaId());
            if (drama == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            // 状态校验
            if (isRejected(drama.getAuditStatus())) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            // 审核组校验
            DramaAuditStepEntity ai = dramaAuditStepDao.findByDramaIdAndStepType(
                    query.getDramaId(), DramaAssetAuditStepTypeEnums.AI.getCode());
            if (!isPass(ai == null ? null : ai.getStatus())) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            // 步骤校验
            DramaAuditStepEntity step = dramaAuditStepDao.findByDramaIdAndStepType(
                    query.getDramaId(), query.getStepType());
            if (step == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            // 状态校验
            if (!isPending(step.getStatus())) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            // 获取当前登录用户id
            Integer adminId = AppTokenUtil.resolveUid(request);
            if (adminId == null) {
                return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
            }
            // 处理步骤
            applyStepHandle(step, query.getAction(), adminId, query.getRemark());
            dramaAuditStepDao.updateById(step);
            dramaAuditService.refreshAggregateAndAutoShelf(query.getDramaId());
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("drama audit handle error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @SysLogAnnotation(module = "作品评审", type = "POST", remark = "集评审A/B")
    public ResponseBase handle(@Valid @RequestBody DramaAssetAuditHandleQuery query, HttpServletRequest request) {
        try {
            // 参数校验
            ResponseBase basicErr = validateHandleBasics(query.getAction(), query.getStepType(), query.getRemark());
            if (basicErr != null) {
                return basicErr;
            }
            // 数据校验
            DramaAssetEntity asset = dramaAssetDao.selectById(query.getAssetId());
            if (asset == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            // 状态校验
            if (isRejected(asset.getAuditStatus())) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            // 步骤校验
            DramaAssetAuditStepEntity ai = dramaAssetAuditStepDao.findByAssetIdAndStepType(
                    query.getAssetId(), DramaAssetAuditStepTypeEnums.AI.getCode());
            if (!isPass(ai == null ? null : ai.getStatus())) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            // 步骤校验
            DramaAssetAuditStepEntity step = dramaAssetAuditStepDao.findByAssetIdAndStepType(
                    query.getAssetId(), query.getStepType());
            if (step == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            // 状态校验
            if (!isPending(step.getStatus())) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            // 获取当前登录用户id
            Integer adminId = AppTokenUtil.resolveUid(request);
            if (adminId == null) {
                return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
            }
            // 处理步骤
            applyStepHandle(step, query.getAction(), adminId, query.getRemark());
            dramaAssetAuditStepDao.updateById(step);
            dramaAssetAuditService.refreshAggregateAndAutoShelf(query.getAssetId());
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("drama asset audit handle error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验动作、审核组；驳回时备注必填。
     */
    private ResponseBase validateHandleBasics(Integer action, Integer stepType, String remark) {
        if (action == null || (action != 1 && action != 2)) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        DramaAssetAuditStepTypeEnums type = DramaAssetAuditStepTypeEnums.fromCode(stepType);
        if (type == null || type == DramaAssetAuditStepTypeEnums.AI) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        if (action == 2 && StringUtils.isEmpty(remark)) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        return null;
    }

    /**
     * 转为处理状态。
     */
    private static boolean isRejected(Integer auditStatus) {
        return auditStatus != null && auditStatus.equals(DramaAssetAuditStatusEnums.REJECTED.getCode());
    }

    /**
     * 转为处理状态。
     */
    private static boolean isPass(Integer status) {
        return status != null && status.equals(DramaAssetAuditStepStatusEnums.PASS.getCode());
    }

    /**
     * 转为处理状态。
     * @param status
     * @return
     */
    private static boolean isPending(Integer status) {
        return status != null && status.equals(DramaAssetAuditStepStatusEnums.PENDING.getCode());
    }

    /**
     * 提交处理。
     */
    private void applyStepHandle(DramaAuditStepEntity step, Integer action, Integer adminId, String remark)
            throws Exception {
        step.setStatus(toHandleStatus(action));
        step.setHandlerId(adminId);
        step.setHandleRemark(remark);
        step.setHandleTime(new Date());
        GenericityUtil.updateDate(step);
    }

    /**
     * 提交处理。
     */
    private void applyStepHandle(DramaAssetAuditStepEntity step, Integer action, Integer adminId, String remark)
            throws Exception {
        step.setStatus(toHandleStatus(action));
        step.setHandlerId(adminId);
        step.setHandleRemark(remark);
        step.setHandleTime(new Date());
        GenericityUtil.updateDate(step);
    }

    /**
     * 转为处理状态。
     */
    private static int toHandleStatus(Integer action) {
        return action != null && action == 1
                ? DramaAssetAuditStepStatusEnums.PASS.getCode()
                : DramaAssetAuditStepStatusEnums.REJECT.getCode();
    }
}
