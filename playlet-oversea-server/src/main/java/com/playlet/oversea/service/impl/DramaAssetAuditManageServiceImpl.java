package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.api.response.DramaAssetAuditEpisodeRespEntity;
import com.playlet.oversea.api.response.DramaAuditDetailRespEntity;
import com.playlet.oversea.api.response.DramaWorkAuditListRespEntity;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.constants.CreatorConstants;
import com.playlet.oversea.dao.creator.CreatorAccountDao;
import com.playlet.oversea.dao.drama.*;
import com.playlet.oversea.dao.template.EmailTemplateDao;
import com.playlet.oversea.entity.creator.CreatorAccountEntity;
import com.playlet.oversea.entity.drama.DramaAssetAuditStepEntity;
import com.playlet.oversea.entity.drama.DramaAssetEntity;
import com.playlet.oversea.entity.drama.DramaAuditStepEntity;
import com.playlet.oversea.entity.drama.DramaEntity;
import com.playlet.oversea.entity.template.EmailTemplateEntity;
import com.playlet.oversea.enums.DramaAssetAuditStatusEnums;
import com.playlet.oversea.enums.DramaAssetAuditStepStatusEnums;
import com.playlet.oversea.enums.DramaAssetAuditStepTypeEnums;
import com.playlet.oversea.enums.CreatorSystemMessageTypeEnums;
import com.playlet.oversea.enums.LanguageEnums;
import com.playlet.oversea.enums.MessageEnums;
import com.playlet.oversea.query.drama.DramaAssetAuditHandleQuery;
import com.playlet.oversea.query.drama.DramaAuditHandleQuery;
import com.playlet.oversea.query.drama.DramaWorkAuditQuery;
import com.playlet.oversea.service.DramaAssetAuditManageService;
import com.playlet.oversea.service.DramaAssetAuditService;
import com.playlet.oversea.service.DramaAuditService;
import com.playlet.oversea.service.MediaUrlService;
import com.playlet.oversea.service.CreatorSystemMessageSendService;
import com.playlet.oversea.utils.*;
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

import cn.hutool.http.HtmlUtil;
import com.alibaba.fastjson.JSON;

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

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
    @Autowired
    private EmailTemplateDao emailTemplateDao;
    @Autowired
    private CreatorAccountDao creatorAccountDao;
    @Autowired
    private CreatorSystemMessageSendService creatorSystemMessageSendService;

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
            ResponseBase basicErr = validateHandleBasics(query.getAction(), query.getStepType(), query.getRemark());
            if (basicErr != null) {
                return basicErr;
            }
            AuditLoadResult<DramaHandleContext> loadResult = loadDramaHandleContext(query);
            if (loadResult.error != null) {
                return loadResult.error;
            }
            Integer adminId = resolveAdminId(request);
            if (adminId == null) {
                return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
            }
            DramaHandleContext ctx = loadResult.data;
            applyStepHandle(ctx.step, query.getAction(), adminId, query.getRemark());
            dramaAuditStepDao.updateById(ctx.step);
            dramaAuditService.refreshAggregateAndAutoShelf(ctx.drama.getId());
            if (isRejectAction(query.getAction())) {
                notifyAuditReject(ctx.drama.getBelongUser(), ctx.drama.getId(), null, ctx.drama.getDramaTitle(),
                        null, query.getRemark(), ctx.step.getId());
            }
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
            ResponseBase basicErr = validateHandleBasics(query.getAction(), query.getStepType(), query.getRemark());
            if (basicErr != null) {
                return basicErr;
            }
            AuditLoadResult<AssetHandleContext> loadResult = loadAssetHandleContext(query);
            if (loadResult.error != null) {
                return loadResult.error;
            }
            Integer adminId = resolveAdminId(request);
            if (adminId == null) {
                return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
            }
            AssetHandleContext ctx = loadResult.data;
            applyStepHandle(ctx.step, query.getAction(), adminId, query.getRemark());
            dramaAssetAuditStepDao.updateById(ctx.step);
            dramaAssetAuditService.refreshAggregateAndAutoShelf(ctx.asset.getId());
            if (isRejectAction(query.getAction())) {
                notifyAuditReject(ctx.creatorId, ctx.asset.getDramaId(), ctx.asset.getId(), ctx.dramaTitle,
                        ctx.asset.getSetNum(), query.getRemark(), ctx.step.getId());
            }
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
        if (!DramaAssetAuditStepStatusEnums.isHandleAction(action)) {
            return setResultError(I18nUtil.getMessage("audit.action_invalid"));
        }
        if (!DramaAssetAuditStepTypeEnums.isManualGroup(stepType)) {
            return setResultError(I18nUtil.getMessage("audit.step_type_invalid"));
        }
        if (isRejectAction(action) && StringUtils.isEmpty(remark)) {
            return setResultError(I18nUtil.getMessage("audit.reject_remark_required"));
        }
        return null;
    }

    /** 加载剧审核上下文，并集中完成状态前置校验。 */
    private AuditLoadResult<DramaHandleContext> loadDramaHandleContext(DramaAuditHandleQuery query) {
        DramaEntity drama = dramaDao.selectById(query.getDramaId());
        if (drama == null) {
            return AuditLoadResult.error(setResultError(I18nUtil.getMessage("base_data_null")));
        }
        ResponseBase aggregateErr = validateAggregateAuditStatus(drama.getAuditStatus(), true);
        if (aggregateErr != null) {
            return AuditLoadResult.error(aggregateErr);
        }
        DramaAuditStepEntity ai = dramaAuditStepDao.findByDramaIdAndStepType(
                query.getDramaId(), DramaAssetAuditStepTypeEnums.AI.getCode());
        ResponseBase aiErr = validateAiStepPassed(ai == null ? null : ai.getStatus());
        if (aiErr != null) {
            return AuditLoadResult.error(aiErr);
        }
        DramaAuditStepEntity step = dramaAuditStepDao.findByDramaIdAndStepType(
                query.getDramaId(), query.getStepType());
        if (step == null) {
            return AuditLoadResult.error(setResultError(I18nUtil.getMessage("base_data_null")));
        }
        ResponseBase pendingErr = validateManualStepPending(step.getStatus());
        if (pendingErr != null) {
            return AuditLoadResult.error(pendingErr);
        }
        return AuditLoadResult.success(new DramaHandleContext(drama, step));
    }

    /** 加载集审核上下文，并集中完成状态前置校验。 */
    private AuditLoadResult<AssetHandleContext> loadAssetHandleContext(DramaAssetAuditHandleQuery query) {
        DramaAssetEntity asset = dramaAssetDao.selectById(query.getAssetId());
        if (asset == null) {
            return AuditLoadResult.error(setResultError(I18nUtil.getMessage("base_data_null")));
        }
        ResponseBase aggregateErr = validateAggregateAuditStatus(asset.getAuditStatus(), false);
        if (aggregateErr != null) {
            return AuditLoadResult.error(aggregateErr);
        }
        DramaAssetAuditStepEntity ai = dramaAssetAuditStepDao.findByAssetIdAndStepType(
                query.getAssetId(), DramaAssetAuditStepTypeEnums.AI.getCode());
        ResponseBase aiErr = validateAiStepPassed(ai == null ? null : ai.getStatus());
        if (aiErr != null) {
            return AuditLoadResult.error(aiErr);
        }
        DramaAssetAuditStepEntity step = dramaAssetAuditStepDao.findByAssetIdAndStepType(
                query.getAssetId(), query.getStepType());
        if (step == null) {
            return AuditLoadResult.error(setResultError(I18nUtil.getMessage("base_data_null")));
        }
        ResponseBase pendingErr = validateManualStepPending(step.getStatus());
        if (pendingErr != null) {
            return AuditLoadResult.error(pendingErr);
        }
        DramaEntity drama = dramaDao.selectById(asset.getDramaId());
        String dramaTitle = drama == null ? null : drama.getDramaTitle();
        Integer creatorId = drama == null ? null : drama.getBelongUser();
        return AuditLoadResult.success(new AssetHandleContext(asset, step, dramaTitle, creatorId));
    }

    /** 统一获取当前登录管理员。 */
    private static Integer resolveAdminId(HttpServletRequest request) {
        return SysUserTokenUtil.resolveAdminId(request);
    }

    /** 聚合审核状态校验：已驳回即终态，不允许另一组继续处理。 */
    private static ResponseBase validateAggregateAuditStatus(Integer auditStatus, boolean dramaLevel) {
        if (!DramaAssetAuditStatusEnums.isRejected(auditStatus)) {
            return null;
        }
        return setResultError(I18nUtil.getMessage(dramaLevel
                ? "audit.drama_rejected" : "audit.episode_rejected"));
    }

    /** AI 必须先通过，人工审核才有意义。 */
    private static ResponseBase validateAiStepPassed(Integer aiStatus) {
        if (DramaAssetAuditStepStatusEnums.isPass(aiStatus)) {
            return null;
        }
        return setResultError(I18nUtil.getMessage("audit.ai_not_pass"));
    }

    /** A/B 组只能从待审核处理一次，状态已落地后禁止重复覆盖。 */
    private static ResponseBase validateManualStepPending(Integer stepStatus) {
        if (DramaAssetAuditStepStatusEnums.isPending(stepStatus)) {
            return null;
        }
        if (DramaAssetAuditStepStatusEnums.isPass(stepStatus)) {
            return setResultError(I18nUtil.getMessage("audit.step_already_passed"));
        }
        if (DramaAssetAuditStepStatusEnums.isReject(stepStatus)) {
            return setResultError(I18nUtil.getMessage("audit.step_already_rejected"));
        }
        return setResultError(I18nUtil.getMessage("audit.step_not_pending"));
    }

    /**
     * 评审驳回通知：邮件保留，同时写作家站内信。任一路失败不影响评审。
     * 模板 1006，占位 作者名 / 作品名 / 集序号 / 驳回原因；剧级驳回集序号为 "-"。
     */
    private void notifyAuditReject(Integer creatorId, Integer dramaId, Integer assetId,
            String dramaTitle, Integer setNum, String rejectReason, Long stepId) {
        if (creatorId == null) {
            log.warn("skip audit reject notify: creatorId null dramaTitle={}", dramaTitle);
            return;
        }
        CreatorAccountEntity creator;
        EmailTemplateEntity template;
        String language;
        String htmlContent;
        String inboxRawContent;
        try {
            creator = creatorAccountDao.selectById(creatorId);
            if (creator == null || StringUtils.isEmpty(creator.getUserAccount())) {
                log.warn("skip audit reject notify: creator missing creatorId={}", creatorId);
                return;
            }
            language = LanguageContext.getLanguage();
            template = emailTemplateDao.findByNum(
                    MessageEnums.CREATOR_AUDIT_REJECT.getIndex(), language);
            // 当前语言无模板时回退默认语言
            if (template == null || StringUtils.isEmpty(template.getTemplateContent())) {
                template = emailTemplateDao.findByNum(
                        MessageEnums.CREATOR_AUDIT_REJECT.getIndex(), LanguageEnums.DEFAULT_LANGUE);
            }
            if (template == null || StringUtils.isEmpty(template.getTemplateContent())) {
                log.warn("skip audit reject notify: template missing creatorId={}", creatorId);
                return;
            }
            String authorName = resolveCreatorDisplayName(creator);
            String title = StringUtils.isEmpty(dramaTitle) ? "-" : dramaTitle.trim();
            String episode = setNum == null ? "-" : String.valueOf(setNum);
            String reason = StringUtils.isEmpty(rejectReason) ? "-" : rejectReason.trim();
            htmlContent = MessageFormatUtils.format(
                    template.getTemplateContent(),
                    HtmlSanitizeUtils.plain(authorName),
                    HtmlSanitizeUtils.plain(title),
                    HtmlSanitizeUtils.plain(episode),
                    HtmlSanitizeUtils.plain(reason));
            // 站内信用未转义正文，再去 HTML 标签
            inboxRawContent = MessageFormatUtils.format(
                    template.getTemplateContent(), authorName, title, episode, reason);
        } catch (Exception e) {
            log.error("audit reject notify prepare failed creatorId={} dramaTitle={}", creatorId, dramaTitle, e);
            return;
        }
        sendAuditRejectEmail(creator, template, language, htmlContent, dramaTitle, setNum);
        sendAuditRejectInbox(creator, language, inboxRawContent, dramaId, assetId, stepId);
    }

    /** 评审驳回邮件，失败只记日志。 */
    private void sendAuditRejectEmail(CreatorAccountEntity creator, EmailTemplateEntity template,
            String language, String htmlContent, String dramaTitle, Integer setNum) {
        try {
            String html = MessageFormatUtils.saveHtml(htmlContent, language);
            EmailUtil.sendEmail(creator.getUserAccount(), template.getTemplateSubject(), html);
            log.info("audit reject email sent creatorId={} dramaTitle={} setNum={}",
                    creator.getId(), dramaTitle, setNum == null ? "-" : setNum);
        } catch (Exception e) {
            log.error("audit reject email failed creatorId={} dramaTitle={}", creator.getId(), dramaTitle, e);
        }
    }

    /** 评审驳回站内信，失败只记日志。 */
    private void sendAuditRejectInbox(CreatorAccountEntity creator, String language, String htmlContent,
            Integer dramaId, Integer assetId, Long stepId) {
        try {
            String inboxTitle = CreatorSystemMessageTypeEnums.AUDIT.title(language);
            String inboxContent = toInboxPlain(htmlContent);
            if (StringUtils.isEmpty(inboxContent)) {
                log.warn("skip audit reject inbox: empty content creatorId={}", creator.getId());
                return;
            }
            String bizId = buildAuditRejectBizId(dramaId, assetId, stepId);
            boolean assetJump = assetId != null;
            String jumpType = assetJump ? CreatorConstants.MSG_JUMP_ASSET : CreatorConstants.MSG_JUMP_DRAMA;
            Map<String, Object> jump = new HashMap<>();
            if (dramaId != null) {
                jump.put("dramaId", dramaId);
            }
            if (assetJump) {
                jump.put("assetId", assetId);
            }
            creatorSystemMessageSendService.sendToCreator(
                    creator.getId(),
                    CreatorSystemMessageTypeEnums.AUDIT.getCode(),
                    language,
                    inboxTitle,
                    inboxContent,
                    null,
                    dramaId,
                    assetId,
                    bizId,
                    jumpType,
                    JSON.toJSONString(jump));
        } catch (Exception e) {
            log.error("audit reject inbox failed creatorId={} dramaId={} assetId={}",
                    creator.getId(), dramaId, assetId, e);
        }
    }

    private static String buildAuditRejectBizId(Integer dramaId, Integer assetId, Long stepId) {
        StringBuilder bizId = new StringBuilder();
        if (assetId != null) {
            bizId.append(CreatorConstants.MSG_BIZ_AUDIT_REJECT_ASSET).append(assetId);
        } else {
            bizId.append(CreatorConstants.MSG_BIZ_AUDIT_REJECT_DRAMA).append(dramaId);
        }
        if (stepId != null) {
            bizId.append(CreatorConstants.MSG_BIZ_STEP).append(stepId);
        }
        return bizId.toString();
    }

    /** 邮件 HTML 转站内信纯文本。 */
    private static String toInboxPlain(String htmlContent) {
        if (StringUtils.isEmpty(htmlContent)) {
            return htmlContent;
        }
        String text = htmlContent.replace("<br/>", "").replace("<br />", "").replace("<br>", "");
        return HtmlUtil.cleanHtmlTag(text).trim();
    }

    private static String resolveCreatorDisplayName(CreatorAccountEntity creator) {
        if (creator == null) {
            return "-";
        }
        if (StringUtils.isNotEmpty(creator.getNickname())) {
            return creator.getNickname().trim();
        }
        return creator.getUserAccount();
    }

    private static boolean isRejectAction(Integer action) {
        return action != null && action.equals(DramaAssetAuditStepStatusEnums.REJECT.getCode());
    }

    private static boolean isPassAction(Integer action) {
        return action != null && action.equals(DramaAssetAuditStepStatusEnums.PASS.getCode());
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
     * 处理动作映射为步骤状态（action 与步骤状态码一致）。
     */
    private static int toHandleStatus(Integer action) {
        return isPassAction(action)
                ? DramaAssetAuditStepStatusEnums.PASS.getCode()
                : DramaAssetAuditStepStatusEnums.REJECT.getCode();
    }

    private static final class DramaHandleContext {
        private final DramaEntity drama;
        private final DramaAuditStepEntity step;

        private DramaHandleContext(DramaEntity drama, DramaAuditStepEntity step) {
            this.drama = drama;
            this.step = step;
        }
    }

    private static final class AssetHandleContext {
        private final DramaAssetEntity asset;
        private final DramaAssetAuditStepEntity step;
        private final String dramaTitle;
        private final Integer creatorId;

        private AssetHandleContext(DramaAssetEntity asset, DramaAssetAuditStepEntity step,
                String dramaTitle, Integer creatorId) {
            this.asset = asset;
            this.step = step;
            this.dramaTitle = dramaTitle;
            this.creatorId = creatorId;
        }
    }

    private static final class AuditLoadResult<T> {
        private final T data;
        private final ResponseBase error;

        private AuditLoadResult(T data, ResponseBase error) {
            this.data = data;
            this.error = error;
        }

        private static <T> AuditLoadResult<T> success(T data) {
            return new AuditLoadResult<>(data, null);
        }

        private static <T> AuditLoadResult<T> error(ResponseBase error) {
            return new AuditLoadResult<>(null, error);
        }
    }
}
