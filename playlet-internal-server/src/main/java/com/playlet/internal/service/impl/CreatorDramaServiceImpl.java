package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.*;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.dao.creator.CreatorDramaDao;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.TagDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.DramaAppealStatusEnums;
import com.playlet.internal.enums.DramaAssetAuditStatusEnums;
import com.playlet.internal.enums.DramaAssetShelfStatusEnums;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.query.creator.CreatorDramaAnalyticsQuery;
import com.playlet.internal.query.creator.CreatorDramaListQuery;
import com.playlet.internal.query.drama.DramaAppealQuery;
import com.playlet.internal.query.drama.DramaAssetAppealQuery;
import com.playlet.internal.query.drama.DramaAssetBatchShelfQuery;
import com.playlet.internal.query.drama.DramaAssetShelfQuery;
import com.playlet.internal.service.CreatorDramaService;
import com.playlet.internal.service.DramaAssetAuditService;
import com.playlet.internal.service.DramaAuditService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 作家端作品：列表/详情/申诉/集上下架，均按 belong_user = 当前作家过滤。
 */
@Slf4j
@RestController
@CrossOrigin
public class CreatorDramaServiceImpl extends BaseApiService implements CreatorDramaService {

	@Autowired
	private CreatorDramaDao creatorDramaDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private TagDao tagDao;
	@Autowired
	private MediaUrlService mediaUrlService;
	@Autowired
	private DramaAuditService dramaAuditService;
	@Autowired
	private DramaAssetAuditService dramaAssetAuditService;

	@Override
	public ResponseBase findList(@RequestBody(required = false) CreatorDramaListQuery query,
			HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query == null) {
			query = new CreatorDramaListQuery();
		}
		if (StringUtils.isNotEmpty(query.getDramaTitle())) {
			query.setDramaTitle(query.getDramaTitle().trim());
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<CreatorDramaListRespEntity> list = creatorDramaDao.findList(query, account.getId());
		if (list == null) {
			list = Collections.emptyList();
		}
		for (CreatorDramaListRespEntity item : list) {
			item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
		}
		log.info("creator drama findList creatorId={} size={}", account.getId(), list.size());
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase findInfo(@RequestParam("id") Integer id, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		DramaEntity drama = dramaDao.selectById(id);
		if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
			return setResultError(I18nUtil.getMessage("drama_null"));
		}
		// 无归属或非本人：不泄露是否存在
		if (drama.getBelongUser() == null || !drama.getBelongUser().equals(account.getId())) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		CreatorDramaInfoRespEntity resp = toInfoResp(drama, LanguageContext.getLanguage());
		log.info("creator drama findInfo creatorId={} dramaId={}", account.getId(), id);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase appeal(@Valid @RequestBody DramaAppealQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		DramaEntity drama = dramaDao.selectById(query.getDramaId());
		if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
			return setResultError(I18nUtil.getMessage("drama_null"));
		}
		// 仅本人作品可申诉
		if (drama.getBelongUser() == null || !drama.getBelongUser().equals(account.getId())) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		// 仅驳回可申诉
		if (drama.getAuditStatus() == null
				|| !drama.getAuditStatus().equals(DramaAssetAuditStatusEnums.REJECTED.getCode())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 申诉中不可重复提交
		if (DramaAssetAuditStatusEnums.isAppealing(drama.getAuditStatus())
				|| (drama.getAppealStatus() != null
				&& drama.getAppealStatus().equals(DramaAppealStatusEnums.APPEALING.getCode()))) {
			return setResultError(I18nUtil.getMessage("base_info_exist"));
		}
		Date now = new Date();
		drama.setAppealStatus(DramaAppealStatusEnums.APPEALING.getCode());
		drama.setAppealReason(query.getRemark().trim());
		drama.setAppealTime(now);
		drama.setAuditStatus(DramaAssetAuditStatusEnums.APPEALING.getCode());
		drama.setGmtModified(now);
		try {
			dramaDao.updateById(drama);
			// 重置 AI/A/B 进入再审，聚合保持 audit_status=4
			dramaAuditService.initAuditSteps(drama.getId());
		} catch (Exception e) {
			log.error("creator drama appeal failed creatorId={} dramaId={}", account.getId(),
					query.getDramaId(), e);
			throw new RuntimeException(e);
		}
		log.info("creator drama appeal submitted creatorId={} dramaId={}", account.getId(), drama.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase assetAppeal(@Valid @RequestBody DramaAssetAppealQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		DramaAssetEntity asset = dramaAssetDao.selectById(query.getAssetId());
		if (asset == null || DeleteStateEnum.DELETE.getIndex().equals(asset.getDeleteState())) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		DramaEntity drama = dramaDao.selectById(asset.getDramaId());
		if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
			return setResultError(I18nUtil.getMessage("drama_null"));
		}
		// 仅本人作品下的集可申诉
		if (drama.getBelongUser() == null || !drama.getBelongUser().equals(account.getId())) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		// 仅驳回可申诉
		if (asset.getAuditStatus() == null
				|| !asset.getAuditStatus().equals(DramaAssetAuditStatusEnums.REJECTED.getCode())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 申诉中不可重复提交
		if (DramaAssetAuditStatusEnums.isAppealing(asset.getAuditStatus())
				|| (asset.getAppealStatus() != null
				&& asset.getAppealStatus().equals(DramaAppealStatusEnums.APPEALING.getCode()))) {
			return setResultError(I18nUtil.getMessage("base_info_exist"));
		}
		Date now = new Date();
		asset.setAppealStatus(DramaAppealStatusEnums.APPEALING.getCode());
		asset.setAppealReason(query.getRemark().trim());
		asset.setAppealTime(now);
		asset.setAuditStatus(DramaAssetAuditStatusEnums.APPEALING.getCode());
		asset.setGmtModified(now);
		try {
			dramaAssetDao.updateById(asset);
			// 重置 AI/A/B 进入再审，聚合保持 audit_status=4
			dramaAssetAuditService.initAuditStepsOnRelease(asset.getId(), asset.getDramaId());
		} catch (Exception e) {
			log.error("creator drama asset appeal failed creatorId={} assetId={}", account.getId(),
					query.getAssetId(), e);
			throw new RuntimeException(e);
		}
		log.info("creator drama asset appeal submitted creatorId={} assetId={} dramaId={}",
				account.getId(), asset.getId(), asset.getDramaId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase shelf(@Valid @RequestBody DramaAssetShelfQuery query, HttpServletRequest request) {
		Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
		if (creatorId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		DramaAssetShelfStatusEnums shelfStatus = DramaAssetShelfStatusEnums.fromCode(query.getShelfStatus());
		if (shelfStatus == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		boolean shelfOn = shelfStatus == DramaAssetShelfStatusEnums.ON;
		try {
			String err = shelfOne(query.getAssetId(), creatorId, shelfOn);
			if (err != null) {
				return setResultError(err);
			}
			log.info("creator drama asset shelf creatorId={} assetId={} shelfStatus={}",
					creatorId, query.getAssetId(), query.getShelfStatus());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("creator drama asset shelf failed creatorId={} assetId={} shelfStatus={}",
					creatorId, query.getAssetId(), query.getShelfStatus(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase batchShelf(@Valid @RequestBody DramaAssetBatchShelfQuery query,
			HttpServletRequest request) {
		Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
		if (creatorId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		DramaAssetShelfStatusEnums shelfStatus = DramaAssetShelfStatusEnums.fromCode(query.getShelfStatus());
		if (shelfStatus == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		boolean shelfOn = shelfStatus == DramaAssetShelfStatusEnums.ON;
		try {
			List<Integer> assetIds = normalizeAssetIds(query.getAssetIds());
			if (assetIds.isEmpty()) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (assetIds.size() > Constants.MAX_PAGESIZE) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			DramaAssetBatchShelfRespEntity resp = new DramaAssetBatchShelfRespEntity();
			Set<Integer> dramaIdsToSync = new HashSet<>();
			for (Integer assetId : assetIds) {
				ShelfOneResult result = shelfOneWithDrama(assetId, creatorId, shelfOn);
				if (result.errorMsg != null) {
					DramaAssetBatchShelfFailItem fail = new DramaAssetBatchShelfFailItem();
					fail.setAssetId(assetId);
					fail.setMessage(result.errorMsg);
					resp.getFailItems().add(fail);
					continue;
				}
				resp.getSuccessIds().add(assetId);
				if (result.dramaId != null) {
					dramaIdsToSync.add(result.dramaId);
				}
			}
			for (Integer dramaId : dramaIdsToSync) {
				dramaAuditService.syncDramaShelfByEpisodes(dramaId);
			}
			resp.setSuccessCount(resp.getSuccessIds().size());
			resp.setFailCount(resp.getFailItems().size());
			log.info("creator drama asset batchShelf creatorId={} shelfStatus={} success={} fail={}",
					creatorId, query.getShelfStatus(), resp.getSuccessCount(), resp.getFailCount());
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("creator drama asset batchShelf failed creatorId={} shelfStatus={}",
					creatorId, query.getShelfStatus(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase analytics(@RequestBody(required = false) CreatorDramaAnalyticsQuery query,
			HttpServletRequest request) {
		Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
		if (creatorId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query == null) {
			query = new CreatorDramaAnalyticsQuery();
		}
		if (StringUtils.isNotEmpty(query.getDramaTitle())) {
			query.setDramaTitle(query.getDramaTitle().trim());
		}
		Integer sortType = query.getSortType();
		if (sortType == null || (sortType != CreatorConstants.ANALYTICS_SORT_HOT
				&& sortType != CreatorConstants.ANALYTICS_SORT_TIME)) {
			query.setSortType(CreatorConstants.ANALYTICS_SORT_HOT);
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<DramaAnalyticsRespEntity> list = dramaDao.analyticsList(query, creatorId);
		if (list == null) {
			list = Collections.emptyList();
		}
		for (DramaAnalyticsRespEntity item : list) {
			item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
			item.setCompleteRate(calcCompleteRate(item.getComplete(), item.getExposure()));
			if (item.getIncomeCoin() == null) {
				item.setIncomeCoin(0L);
			}
			if (item.getScoreNum() != null) {
				item.setScoreNum(item.getScoreNum().setScale(1, RoundingMode.HALF_UP));
			}
		}
		log.info("creator drama analytics creatorId={} size={}", creatorId, list.size());
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	/** 完播率 = 完播量 / 曝光量 * 100，曝光为 0 则 0 */
	private static BigDecimal calcCompleteRate(Long complete, Long exposure) {
		long exp = exposure == null ? 0L : exposure;
		long cmp = complete == null ? 0L : complete;
		if (exp <= 0L) {
			return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
		}
		return BigDecimal.valueOf(cmp)
				.multiply(BigDecimal.valueOf(100))
				.divide(BigDecimal.valueOf(exp), 1, RoundingMode.HALF_UP);
	}

	/** 单集上架/下架并立刻 sync 整剧（单接口用） */
	private String shelfOne(Integer assetId, Integer creatorId, boolean shelfOn) {
		ShelfOneResult result = shelfOneWithDrama(assetId, creatorId, shelfOn);
		if (result.errorMsg != null) {
			return result.errorMsg;
		}
		if (result.dramaId != null) {
			dramaAuditService.syncDramaShelfByEpisodes(result.dramaId);
		}
		return null;
	}

	/**
	 * 单集上下架核心逻辑；成功时返回 dramaId 供批量去重 sync，失败返回 errorMsg。
	 */
	private ShelfOneResult shelfOneWithDrama(Integer assetId, Integer creatorId, boolean shelfOn) {
		ShelfOneResult result = new ShelfOneResult();
		if (assetId == null) {
			result.errorMsg = I18nUtil.getMessage("base_error");
			return result;
		}
		DramaAssetEntity asset = dramaAssetDao.selectById(assetId);
		if (asset == null || DeleteStateEnum.DELETE.getIndex().equals(asset.getDeleteState())) {
			result.errorMsg = I18nUtil.getMessage("base_data_null");
			return result;
		}
		DramaEntity drama = dramaDao.selectById(asset.getDramaId());
		if (drama == null || DeleteStateEnum.DELETE.getIndex().equals(drama.getDeleteState())) {
			result.errorMsg = I18nUtil.getMessage("drama_null");
			return result;
		}
		// 仅本人作品下的集可操作
		if (drama.getBelongUser() == null || !drama.getBelongUser().equals(creatorId)) {
			result.errorMsg = I18nUtil.getMessage("purview_error_null");
			return result;
		}
		Date now = new Date();
		if (shelfOn) {
			if (!isApproved(asset.getAuditStatus()) || !isApproved(drama.getAuditStatus())) {
				result.errorMsg = I18nUtil.getMessage("base_error");
				return result;
			}
			// 已上架：幂等成功
			if (asset.getShelfStatus() == null
					|| !asset.getShelfStatus().equals(DramaAssetShelfStatusEnums.ON.getCode())) {
				asset.setShelfStatus(DramaAssetShelfStatusEnums.ON.getCode());
				asset.setVideoStatus(PublicEnums.ONE.getIndex());
				asset.setShelfTime(now);
				asset.setGmtModified(now);
				dramaAssetDao.updateById(asset);
			}
		} else {
			// 已下架：幂等成功，仍需 sync
			if (asset.getShelfStatus() != null
					&& !asset.getShelfStatus().equals(DramaAssetShelfStatusEnums.OFF.getCode())) {
				asset.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
				asset.setVideoStatus(PublicEnums.ZERO.getIndex());
				asset.setShelfTime(null);
				asset.setGmtModified(now);
				dramaAssetDao.updateById(asset);
			}
		}
		result.dramaId = drama.getId();
		return result;
	}

	/** 去重并去掉 null */
	private static List<Integer> normalizeAssetIds(List<Integer> raw) {
		if (raw == null || raw.isEmpty()) {
			return new ArrayList<>();
		}
		Set<Integer> uniq = new LinkedHashSet<>();
		for (Integer id : raw) {
			if (id != null) {
				uniq.add(id);
			}
		}
		return new ArrayList<>(uniq);
	}

	private static boolean isApproved(Integer auditStatus) {
		return auditStatus != null && auditStatus.equals(DramaAssetAuditStatusEnums.APPROVED.getCode());
	}

	private static class ShelfOneResult {
		private Integer dramaId;
		private String errorMsg;
	}

	private CreatorDramaInfoRespEntity toInfoResp(DramaEntity drama, String language) {
		CreatorDramaInfoRespEntity resp = new CreatorDramaInfoRespEntity();
		resp.setId(drama.getId());
		resp.setDramaTitle(drama.getDramaTitle());
		resp.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
		resp.setProducerFirm(drama.getProducerFirm());
		resp.setDescriptionInfo(drama.getDescriptionInfo());
		resp.setTotalEpisodes(drama.getTotalEpisodes());
		resp.setFinishedState(drama.getFinishedState());
		resp.setVideoType(drama.getVideoType());
		resp.setAuditStatus(drama.getAuditStatus());
		resp.setShelfStatus(drama.getShelfStatus());
		resp.setAuditRejectReason(drama.getAuditRejectReason());
		resp.setAppealStatus(drama.getAppealStatus());
		resp.setAppealReason(drama.getAppealReason());
		resp.setAppealTime(drama.getAppealTime());
		resp.setSetTime(drama.getSetTime());
		resp.setGmtModified(drama.getGmtModified());
		resp.setTagList(tagDao.findGroupLang(language, drama.getId()));
		List<DramaAssetEntity> assets = dramaAssetDao.findNotDeletedByDramaId(drama.getId());
		List<CreatorDramaAssetRespEntity> assetList = new ArrayList<>();
		if (assets != null) {
			for (DramaAssetEntity asset : assets) {
				assetList.add(toAssetResp(asset));
			}
		}
		resp.setAssetList(assetList);
		resp.setUploadSetNum(assetList.size());
		return resp;
	}

	private CreatorDramaAssetRespEntity toAssetResp(DramaAssetEntity asset) {
		CreatorDramaAssetRespEntity resp = new CreatorDramaAssetRespEntity();
		resp.setId(asset.getId());
		resp.setDramaId(asset.getDramaId());
		resp.setSetNum(asset.getSetNum());
		resp.setVideoName(asset.getVideoName());
		resp.setVideoUrl(mediaUrlService.signVideo(asset.getVideoUrl()));
		resp.setAuditStatus(asset.getAuditStatus());
		resp.setAuditStatusName(DramaAssetAuditStatusEnums.getLabel(asset.getAuditStatus()));
		resp.setShelfStatus(asset.getShelfStatus());
		resp.setAuditRejectReason(asset.getAuditRejectReason());
		resp.setSetTime(asset.getSetTime());
		resp.setDurationSeconds(asset.getDurationSeconds());
		resp.setDurationText(formatDuration(asset.getDurationSeconds()));
		long exposure = asset.getExposureCount() == null ? 0L : asset.getExposureCount();
		long complete = asset.getCompleteCount() == null ? 0L : asset.getCompleteCount();
		resp.setExposureCount(exposure);
		resp.setCompleteCount(complete);
		// 完播率 = 完播 / 曝光 * 100，保留 1 位小数
		if (exposure <= 0) {
			resp.setCompleteRate(0.0);
		} else {
			resp.setCompleteRate(Math.round(complete * 1000.0 / exposure) / 10.0);
		}
		return resp;
	}

	/** 秒数转 7'55"；空则不展示 */
	private static String formatDuration(Integer seconds) {
		if (seconds == null || seconds < 0) {
			return null;
		}
		int minute = seconds / 60;
		int second = seconds % 60;
		return minute + "'" + String.format("%02d", second) + "\"";
	}
}
