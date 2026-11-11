package com.playlet.internal.service.drama.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.DramaAssetRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.TheaterConstants;
import com.playlet.internal.dao.drama.*;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaRecordMaterialEntity;
import com.playlet.internal.entity.drama.DramaTagRelEntity;
import com.playlet.internal.entity.drama.TagEntity;
import com.playlet.internal.enums.*;
import com.playlet.internal.query.drama.AddDramaQuery;
import com.playlet.internal.query.drama.DramaMaterialItemQuery;
import com.playlet.internal.query.drama.QueryDramaQuery;
import com.playlet.internal.query.drama.UpdateDramaQuery;
import com.playlet.internal.service.drama.DramaAuditService;
import com.playlet.internal.service.drama.DramaService;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.QiniuUploadUtils;
import com.playlet.internal.utils.TheaterHomeCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
public class DramaServiceImpl extends BaseApiService implements DramaService{
	
	@Autowired
	private DramaDao dramaDao;
	
	@Autowired
	private DramaTagRelDao dramaTagRelDao;
	
	@Autowired
	private DramaAssetDao dramaAssetDao;
	
	@Autowired
	private TagDao tagDao;

	@Autowired
	private MediaUrlService mediaUrlService;

	@Autowired
	private TheaterHomeCacheHelper theaterHomeCacheHelper;

	@Autowired
	private DramaAuditService dramaAuditService;

	@Autowired
	private DramaRecordMaterialDao dramaRecordMaterialDao;

	@Override
	public ResponseBase addDrama(@Valid AddDramaQuery createPay, MultipartFile file) {
		try {
			log.info("[addDrama] investmentAmount={}, filingCategory={}, defaultUnlockMethod={}, storySynopsis={}",
				createPay.getInvestmentAmount(), createPay.getFilingCategory(),
				createPay.getDefaultUnlockMethod(),
				createPay.getStorySynopsis() != null ? createPay.getStorySynopsis().substring(0, Math.min(30, createPay.getStorySynopsis().length())) : null);
			//上传图片
			if (file == null && (createPay.getCoverUrl() == null || createPay.getCoverUrl().trim().isEmpty())) {
				return setResultError(I18nUtil.getMessage("cover_not_null"));
			}
			DramaEntity entity = new DramaEntity();
			BeanUtils.copyProperties(createPay, entity);
			// 剧类型：显式 dramaType 优先，兼容老字段 isAi
			Integer dramaType = createPay.getDramaType() != null ? createPay.getDramaType()
					: (Integer.valueOf(1).equals(createPay.getIsAi()) ? 2 : 1);
			entity.setDramaType(dramaType);
			entity.setIsAi(Integer.valueOf(2).equals(dramaType) ? 1 : 0);
			// 备案分类以服务端为准，避免前端漏传/传错
			entity.setFilingCategory(computeFilingCategory(createPay.getInvestmentAmount(), dramaType,
					createPay.getSpecialTheme()));
			//新增短剧基础信息
			entity.setRecommendedCarousel(RecommendedCarouselEnums.NOT_RECOMMENDED.getIndex());
			entity.setVerifyStatus(VerifyStateEnums.REMOVED_SHELVES.getIndex());
			entity.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
			entity.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaDao.insert(entity);
			//添加短剧标签关联
			if (createPay.getTagGroupIdList() != null && createPay.getTagGroupIdList().size() > 0) {
				for (int i = 0; i < createPay.getTagGroupIdList().size(); i++) {
					DramaTagRelEntity dramaTagRelEntity = new DramaTagRelEntity();
					dramaTagRelEntity.setDramaId(entity.getId());
					dramaTagRelEntity.setTagGroupId(createPay.getTagGroupIdList().get(i));
					GenericityUtil.setDate(dramaTagRelEntity);
					dramaTagRelDao.insert(dramaTagRelEntity);
				}
			}
			if (file != null) {
				String path = String.format(Constants.FILE_UPLOAD_SITE, entity.getId());
				String url = QiniuUploadUtils.uploadFile(file, path);
				entity.setCoverUrl(url);
			}
			dramaDao.updateById(entity);
			// 保存创建向导提交的备案材料
			saveMaterials(entity.getId(), createPay.getMaterials());
			// 剧评审：封面/简介/标签进入 AI 默认通过 + A/B 待审
			dramaAuditService.initAuditSteps(entity.getId());
			return setResultSuccess(entity,I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}


	@Override
	public ResponseBase update(@Valid UpdateDramaQuery createPay, MultipartFile file) {
		try {
			log.info("[update] id={}, investmentAmount={}, filingCategory={}, defaultUnlockMethod={}",
				createPay.getId(), createPay.getInvestmentAmount(), createPay.getFilingCategory(), createPay.getDefaultUnlockMethod());
			DramaEntity entity = dramaDao.selectById(createPay.getId());
			if (entity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			// 仅剧名、简介、封面变更才触发重审；标签/集数等其它字段不影响审核状态
			boolean auditResetRequired = false;
			if (createPay.getDramaTitle() != null
					&& !createPay.getDramaTitle().equals(entity.getDramaTitle())) {
				auditResetRequired = true;
			}
			if (createPay.getDescriptionInfo() != null
					&& !createPay.getDescriptionInfo().equals(entity.getDescriptionInfo())) {
				auditResetRequired = true;
			}
			entity.setDramaTitle(createPay.getDramaTitle());
			entity.setProducerFirm(createPay.getProducerFirm());
			entity.setTotalEpisodes(createPay.getTotalEpisodes());
			entity.setFinishedState(createPay.getFinishedState());
			entity.setVideoType(createPay.getVideoType());
			entity.setDescriptionInfo(createPay.getDescriptionInfo());
			entity.setBelongUser(createPay.getBelongUser());
			entity.setIsAi(createPay.getIsAi());
			// 剧类型：显式 dramaType 优先，兼容老字段 isAi
			Integer dramaType = createPay.getDramaType() != null ? createPay.getDramaType()
					: (Integer.valueOf(1).equals(createPay.getIsAi()) ? 2 : 1);
			entity.setDramaType(dramaType);
			entity.setIsAi(Integer.valueOf(2).equals(dramaType) ? 1 : 0);
			entity.setSpecialTheme(createPay.getSpecialTheme());
			entity.setThemePartition(createPay.getThemePartition());
			entity.setOtherCharacters(createPay.getOtherCharacters());
			entity.setProjectShareRatio(createPay.getProjectShareRatio());
			// 备案分类以服务端为准
			entity.setFilingCategory(computeFilingCategory(createPay.getInvestmentAmount(), dramaType,
					createPay.getSpecialTheme()));
			// 创建向导扩展字段
			entity.setInvestmentAmount(createPay.getInvestmentAmount());
			entity.setDefaultUnlockMethod(createPay.getDefaultUnlockMethod());
			entity.setUnlockPrice(createPay.getUnlockPrice());
			entity.setStorySynopsis(createPay.getStorySynopsis());
			entity.setEpisodeOutline(createPay.getEpisodeOutline());
			entity.setFemaleLead(createPay.getFemaleLead());
			entity.setMaleLead(createPay.getMaleLead());
			if (file != null) {
				String path = String.format(Constants.FILE_UPLOAD_SITE, entity.getId());
				String url = QiniuUploadUtils.uploadFile(file, path);
				entity.setCoverUrl(url);
				auditResetRequired = true;
			}
			if (createPay.getTagGroupIdList() != null && createPay.getTagGroupIdList().size() > 0) {
				dramaTagRelDao.deleteByDramaId(entity.getId());
				for (int i = 0; i < createPay.getTagGroupIdList().size(); i++) {
					DramaTagRelEntity dramaTagRelEntity = new DramaTagRelEntity();
					dramaTagRelEntity.setDramaId(entity.getId());
					dramaTagRelEntity.setTagGroupId(createPay.getTagGroupIdList().get(i));
					GenericityUtil.setDate(dramaTagRelEntity);
					dramaTagRelDao.insert(dramaTagRelEntity);
				}
			}
			if (auditResetRequired) {
				entity.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
				entity.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
				entity.setVerifyStatus(VerifyStateEnums.REMOVED_SHELVES.getIndex());
				entity.setAuditRejectReason(null);
				entity.setAuditPassTime(null);
				entity.setShelfTime(null);
				// 元数据变更重审：清空申诉态
				entity.setAppealStatus(DramaAppealStatusEnums.NONE.getCode());
				entity.setAppealReason(null);
				entity.setAppealTime(null);
			}
			dramaDao.updateById(entity);
			// 创建向导提交的备案材料（全量替换）
			if (createPay.getMaterials() != null) {
				saveMaterials(entity.getId(), createPay.getMaterials());
			}
			if (auditResetRequired) {
				dramaAuditService.initAuditSteps(entity.getId());
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}


	@Override
	public ResponseBase findList(@Valid @RequestBody QueryDramaQuery entity) {
		try {
			String language = LanguageContext.getLanguage();
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			List<DramaEntity> list = dramaDao.findList(entity);
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					List<TagEntity> tagList = tagDao.findGroupLang(language,list.get(i).getId());
					list.get(i).setTagList(tagList);
					Integer uploadSetNum = dramaAssetDao.findByDramaIdNum(list.get(i).getId());
					list.get(i).setUploadSetNum(uploadSetNum);
					list.get(i).setCoverUrl(mediaUrlService.sign(list.get(i).getCoverUrl()));
				}
			}
			PageInfo<DramaEntity> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}


	@Override
	public ResponseBase delete(Integer id) {
		try {
			DramaEntity entity = dramaDao.selectById(id);
			if (entity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			entity.setDeleteState(DeleteStateEnum.DELETE.getIndex());
			dramaDao.updateById(entity);
			dramaAssetDao.updateDramaIdDeleteState(entity.getId(),DeleteStateEnum.DELETE.getIndex());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}


	@Override
	public ResponseBase verifyStatus(Integer id, Integer verifyStatus) {
		try {
			DramaEntity entity = dramaDao.selectById(id);
			if (entity == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (VerifyStateEnums.AVAILABLE_NOW.getIndex().equals(verifyStatus)) {
				// 整剧上架：剧须已过审，且至少一集已过审；批量上架已过审集后推导剧状态
				if (!DramaAssetAuditStatusEnums.isApproved(entity.getAuditStatus())) {
					return setResultError(I18nUtil.getMessage("creator.shelf.drama_not_approved"));
				}
				Integer approvedCount = dramaAssetDao.countApprovedByDramaId(id);
				if (approvedCount == null || approvedCount < 1) {
					return setResultError(I18nUtil.getMessage("video_not_release"));
				}
				dramaAuditService.forceShelfDramaAndApprovedEpisodes(id);
				log.info("drama verifyStatus shelf on dramaId={} approvedEpisodeCount={}", id, approvedCount);
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			// 下架：整剧 + 全部已上架集
			dramaAuditService.forceUnshelfDramaAndEpisodes(id);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase verifyRecommendedCarousel(Integer id, Integer status) {
		try {
			DramaEntity entity = dramaDao.selectOne(new QueryWrapper<DramaEntity>()
					.eq("id", id)
					.eq("verify_status", VerifyStateEnums.AVAILABLE_NOW.getIndex())
					.eq("delete_state", DeleteStateEnum.NORMAL.getIndex()));
			if (entity == null) {
				return setResultError(I18nUtil.getMessage("video_not_up"));
			}
			// 仅「设为推荐」时校验上限；取消推荐不受限
			if (RecommendedCarouselEnums.RECOMMENDED.getIndex().equals(status)
					&& !RecommendedCarouselEnums.RECOMMENDED.getIndex().equals(entity.getRecommendedCarousel())) {
				Integer count = dramaDao.selectCount(new QueryWrapper<DramaEntity>()
						.eq("recommended_carousel", RecommendedCarouselEnums.RECOMMENDED.getIndex())
						.eq("delete_state", DeleteStateEnum.NORMAL.getIndex()));
				if (count != null && count >= TheaterConstants.HOME_CAROUSEL_LIMIT) {
					return setResultError(I18nUtil.getMessage("video_recommended_carousel_limit"));
				}
			}
			entity.setRecommendedCarousel(status);
			dramaDao.updateById(entity);
			theaterHomeCacheHelper.invalidateAll();
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public ResponseBase findVideo(Integer id) {
		try {
			DramaEntity entity = dramaDao.selectById(id);
			if (entity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			List<DramaAssetRespEntity> list = dramaAssetDao.findByDramaId(id);
			return setResultSuccess(list, I18nUtil.getMessage("base_success")); 
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}


	@Override
	public ResponseBase findInfo(Integer id) {
		try {
			String language = LanguageContext.getLanguage();
			DramaEntity entity = dramaDao.selectById(id);
			if (entity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			List<TagEntity> tagList = tagDao.findGroupLang(language,entity.getId());
			entity.setTagList(tagList);
			Integer uploadSetNum = dramaAssetDao.findByDramaIdNum(entity.getId());
			entity.setUploadSetNum(uploadSetNum);
			List<DramaAssetRespEntity> list = dramaAssetDao.findByDramaId(id);
			entity.setVoideList(list);
			entity.setCoverUrl(mediaUrlService.sign(entity.getCoverUrl()));
			// 创建向导材料回显
			List<DramaRecordMaterialEntity> materials = dramaRecordMaterialDao.selectList(
					new QueryWrapper<DramaRecordMaterialEntity>()
							.eq("drama_id", entity.getId())
							.orderByAsc("material_type"));
			if (materials != null) {
				for (DramaRecordMaterialEntity m : materials) {
					m.setFileUrl(mediaUrlService.sign(m.getFileKey()));
				}
			}
			entity.setMaterialList(materials);
			return setResultSuccess(entity, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 备案分类判定（服务端权威计算）。
	 * 特殊题材命中一类；真人短剧：≥300万一类、100万(含)~300万二类、<100万三类；
	 * AI漫剧：≥80万一类、30万(含)~80万二类、<30万三类。
	 */
	private Integer computeFilingCategory(java.math.BigDecimal investmentAmount, Integer dramaType,
			String specialTheme) {
		if (specialTheme != null && !specialTheme.trim().isEmpty()) {
			return 1;
		}
		long amount = investmentAmount == null ? 0L : investmentAmount.longValue();
		if (Integer.valueOf(2).equals(dramaType)) {
			if (amount >= 800000L) {
				return 1;
			}
			if (amount >= 300000L) {
				return 2;
			}
			return 3;
		}
		if (amount >= 3000000L) {
			return 1;
		}
		if (amount >= 1000000L) {
			return 2;
		}
		return 3;
	}

	/** 保存创建向导提交的备案材料：先清空该短剧旧材料，再全量写入。 */
	private void saveMaterials(Integer dramaId, List<DramaMaterialItemQuery> materials) throws Exception {
		if (dramaId == null || materials == null) {
			return;
		}
		dramaRecordMaterialDao.delete(new QueryWrapper<DramaRecordMaterialEntity>().eq("drama_id", dramaId));
		// uk_drama_material 为 (drama_id, material_type)，同一类型仅保留首条
		Set<Integer> savedTypes = new HashSet<>();
		for (DramaMaterialItemQuery item : materials) {
			if (item.getMaterialType() == null || item.getFileKey() == null || item.getFileKey().isEmpty()) {
				continue;
			}
			if (!savedTypes.add(item.getMaterialType())) {
				continue;
			}
			DramaRecordMaterialEntity material = new DramaRecordMaterialEntity();
			material.setDramaId(dramaId);
			material.setMaterialType(item.getMaterialType());
			DramaRecordMaterialTypeEnums typeEnum = DramaRecordMaterialTypeEnums.fromCode(item.getMaterialType());
			material.setMaterialLabel(typeEnum == null ? "" : typeEnum.getLabel());
			material.setFileKey(item.getFileKey());
			material.setFileName(item.getFileName());
			material.setRequired(item.getRequired() == null ? 0 : item.getRequired());
			GenericityUtil.setDate(material);
			dramaRecordMaterialDao.insert(material);
		}
	}

}
