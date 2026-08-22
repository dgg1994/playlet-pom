package com.playlet.oversea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.api.response.DramaAssetRespEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.constants.TheaterConstants;
import com.playlet.oversea.dao.drama.DramaAssetDao;
import com.playlet.oversea.dao.drama.DramaDao;
import com.playlet.oversea.dao.drama.DramaTagRelDao;
import com.playlet.oversea.dao.drama.TagDao;
import com.playlet.oversea.entity.drama.DramaEntity;
import com.playlet.oversea.entity.drama.DramaTagRelEntity;
import com.playlet.oversea.entity.drama.TagEntity;
import com.playlet.oversea.enums.*;
import com.playlet.oversea.query.drama.AddDramaQuery;
import com.playlet.oversea.query.drama.QueryDramaQuery;
import com.playlet.oversea.query.drama.UpdateDramaQuery;
import com.playlet.oversea.service.DramaAuditService;
import com.playlet.oversea.service.DramaService;
import com.playlet.oversea.service.MediaUrlService;
import com.playlet.oversea.service.RankAlgoService;
import com.playlet.oversea.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

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
	private RankAlgoService rankAlgoService;

	@Autowired
	private TheaterHomeCacheHelper theaterHomeCacheHelper;

	@Autowired
	private DramaAuditService dramaAuditService;

	@Override
	public ResponseBase addDrama(@Valid AddDramaQuery createPay, MultipartFile file) {
		try {
			//上传图片
			if(file == null) {
				return setResultError(I18nUtil.getMessage("cover_not_null"));
			}
			DramaEntity entity = new DramaEntity();
			BeanUtils.copyProperties(createPay, entity);
			//新增短剧基础信息
			entity.setRecommendedCarousel(RecommendedCarouselEnums.NOT_RECOMMENDED.getIndex());
			entity.setVerifyStatus(VerifyStateEnums.REMOVED_SHELVES.getIndex());
			entity.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
			entity.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaDao.insert(entity);
			//添加短剧标签关联
			if(createPay.getTagGroupIdList() != null && createPay.getTagGroupIdList().size() > 0) {
				for (int i = 0; i < createPay.getTagGroupIdList().size(); i++) {
					DramaTagRelEntity dramaTagRelEntity = new DramaTagRelEntity();
					dramaTagRelEntity.setDramaId(entity.getId());
					dramaTagRelEntity.setTagGroupId(createPay.getTagGroupIdList().get(i));
					GenericityUtil.setDate(dramaTagRelEntity);
					dramaTagRelDao.insert(dramaTagRelEntity);
				}
			}
			String path = String.format(Constants.FILE_UPLOAD_SITE, entity.getId());
			String url = QiniuUploadUtils.uploadFile(file,path);
			entity.setCoverUrl(url);
			dramaDao.updateById(entity);
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
			DramaEntity entity = dramaDao.selectById(createPay.getId());
			if(entity == null) {
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
			if(list != null && list.size() > 0) {
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
			if(entity == null) {
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
			if(entity == null) {
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
			if(entity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			List<TagEntity> tagList = tagDao.findGroupLang(language,entity.getId());
			entity.setTagList(tagList);
			Integer uploadSetNum = dramaAssetDao.findByDramaIdNum(entity.getId());
			entity.setUploadSetNum(uploadSetNum);
			List<DramaAssetRespEntity> list = dramaAssetDao.findByDramaId(id);
			entity.setVoideList(list);
			entity.setCoverUrl(mediaUrlService.sign(entity.getCoverUrl()));
			return setResultSuccess(entity, I18nUtil.getMessage("base_success")); 
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

}
