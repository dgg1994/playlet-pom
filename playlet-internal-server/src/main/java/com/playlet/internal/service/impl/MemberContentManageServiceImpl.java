package com.playlet.internal.service.impl;

import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.EpisodeUnlockConfigQuery;
import com.playlet.internal.api.request.MemberDramaConfigQuery;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.enums.EpisodeUnlockTypeEnums;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.MemberContentManageService;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端会员短剧标记与剧集解锁配置。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class MemberContentManageServiceImpl implements MemberContentManageService {

	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;

	@Override
	@SysLogAnnotation(module = "会员内容", type = "POST", remark = "设置会员短剧")
	public ResponseBase setMemberDrama(@RequestBody MemberDramaConfigQuery query) {
		try {
			if (query == null || query.getDramaId() == null || query.getIsMemberDrama() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			boolean validFlag = PublicEnums.ZERO.getIndex().equals(query.getIsMemberDrama())
					|| PublicEnums.ONE.getIndex().equals(query.getIsMemberDrama());
			if (!validFlag) {
				return setResultError("isMemberDrama 仅支持 0/1");
			}
			DramaEntity drama = dramaDao.findByDramaId(query.getDramaId());
			if (drama == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			int rows = dramaDao.updateMemberDrama(query.getDramaId(), query.getIsMemberDrama());
			log.info("setMemberDrama dramaId={} isMemberDrama={} rows={}",
					query.getDramaId(), query.getIsMemberDrama(), rows);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("setMemberDrama biz error dramaId={}", query == null ? null : query.getDramaId(), e);
			throw e;
		} catch (Exception e) {
			log.error("setMemberDrama failed dramaId={}", query == null ? null : query.getDramaId(), e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "会员内容", type = "POST", remark = "单集解锁配置")
	public ResponseBase updateUnlock(@RequestBody EpisodeUnlockConfigQuery query) {
		try {
			String err = validateUnlock(query, true);
			if (err != null) {
				return setResultError(err);
			}
			DramaAssetEntity asset = dramaAssetDao.selectById(query.getAssetId());
			if (asset == null || (asset.getDeleteState() != null && asset.getDeleteState() == 1)) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			Integer priceCoin = resolvePriceCoin(query);
			BigDecimal priceYuan = resolvePriceYuan(query);
			int rows = dramaAssetDao.updateUnlockConfig(query.getAssetId(),
					query.getUnlockType(), priceCoin, priceYuan);
			log.info("updateUnlock assetId={} unlockType={} rows={}",
					query.getAssetId(), query.getUnlockType(), rows);
			Map<String, Object> data = new HashMap<>(2);
			data.put("affected", rows);
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("updateUnlock biz error assetId={}", query == null ? null : query.getAssetId(), e);
			throw e;
		} catch (Exception e) {
			log.error("updateUnlock failed assetId={}", query == null ? null : query.getAssetId(), e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "会员内容", type = "POST", remark = "批量解锁配置")
	public ResponseBase batchUnlockFromSetNum(@RequestBody EpisodeUnlockConfigQuery query) {
		try {
			String err = validateUnlock(query, false);
			if (err != null) {
				return setResultError(err);
			}
			if (query.getDramaId() == null || query.getFromSetNum() == null) {
				return setResultError("dramaId、fromSetNum 不能为空");
			}
			if (query.getFromSetNum() < 1) {
				return setResultError("fromSetNum 须 >= 1");
			}
			DramaEntity drama = dramaDao.findByDramaId(query.getDramaId());
			if (drama == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			Integer priceCoin = resolvePriceCoin(query);
			BigDecimal priceYuan = resolvePriceYuan(query);
			int rows = dramaAssetDao.batchUpdateUnlockFromSetNum(query.getDramaId(),
					query.getFromSetNum(), query.getUnlockType(), priceCoin, priceYuan);
			log.info("batchUnlock dramaId={} fromSetNum={} unlockType={} rows={}",
					query.getDramaId(), query.getFromSetNum(), query.getUnlockType(), rows);
			Map<String, Object> data = new HashMap<>(2);
			data.put("affected", rows);
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("batchUnlock biz error dramaId={}", query == null ? null : query.getDramaId(), e);
			throw e;
		} catch (Exception e) {
			log.error("batchUnlock failed dramaId={}", query == null ? null : query.getDramaId(), e);
			throw new BaseException("操作失败", e);
		}
	}

	private String validateUnlock(EpisodeUnlockConfigQuery query, boolean single) {
		if (query == null || query.getUnlockType() == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (single && query.getAssetId() == null) {
			return "assetId 不能为空";
		}
		boolean knownType = EpisodeUnlockTypeEnums.isFree(query.getUnlockType())
				|| EpisodeUnlockTypeEnums.isAd(query.getUnlockType())
				|| EpisodeUnlockTypeEnums.isPay(query.getUnlockType());
		if (!knownType) {
			return "unlockType 仅支持 0/1/2";
		}
		if (EpisodeUnlockTypeEnums.isPay(query.getUnlockType())) {
			boolean hasCoin = query.getPriceCoin() != null && query.getPriceCoin() > 0;
			boolean hasYuan = query.getPriceYuan() != null
					&& query.getPriceYuan().compareTo(BigDecimal.ZERO) > 0;
			if (!hasCoin && !hasYuan) {
				return "付费解锁须配置 priceCoin 或 priceYuan";
			}
		}
		if (query.getPriceCoin() != null && query.getPriceCoin() < 0) {
			return "priceCoin 不能为负";
		}
		if (query.getPriceYuan() != null && query.getPriceYuan().compareTo(BigDecimal.ZERO) < 0) {
			return "priceYuan 不能为负";
		}
		return null;
	}

	/** 非付费集价格清零，避免残留 */
	private Integer resolvePriceCoin(EpisodeUnlockConfigQuery query) {
		if (EpisodeUnlockTypeEnums.isPay(query.getUnlockType())) {
			return query.getPriceCoin() == null ? 0 : query.getPriceCoin();
		}
		return 0;
	}

	private BigDecimal resolvePriceYuan(EpisodeUnlockConfigQuery query) {
		if (EpisodeUnlockTypeEnums.isPay(query.getUnlockType())) {
			return query.getPriceYuan() == null ? BigDecimal.ZERO : query.getPriceYuan();
		}
		return BigDecimal.ZERO;
	}
}
