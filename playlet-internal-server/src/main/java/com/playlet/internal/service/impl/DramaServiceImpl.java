package com.playlet.internal.service.impl;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.DramaTagRelDao;
import com.playlet.internal.dao.drama.TagDao;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaTagRelEntity;
import com.playlet.internal.entity.drama.TagEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.VerifyStateEnums;
import com.playlet.internal.query.drama.AddDramaQuery;
import com.playlet.internal.query.drama.QueryDramaQuery;
import com.playlet.internal.query.drama.UpdateDramaQuery;
import com.playlet.internal.response.drama.DramaAssetRes;
import com.playlet.internal.service.DramaService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.QiniuUploadUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Transactional
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
	
	
	@Override
	public ResponseBase addDrama(AddDramaQuery createPay, MultipartFile file) {
		try {
			//上传图片
			if(file == null) {
				return setResultError(I18nUtil.getMessage("cover_not_null"));
			}
			DramaEntity entity = new DramaEntity();
			BeanUtils.copyProperties(createPay, entity);
			//新增短剧基础信息
			entity.setVerifyStatus(VerifyStateEnums.DRAFT.getIndex());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaDao.insert(entity);
			//添加短剧标签关联
			if(createPay.getTagGroupIdList() != null && createPay.getTagGroupIdList().size() > 0) {
				for (int i = 0; i < createPay.getTagGroupIdList().size(); i++) {
					DramaTagRelEntity dramaTagRelEntity = new DramaTagRelEntity();
					dramaTagRelEntity.setDramaId(entity.getId());
					dramaTagRelEntity.setTagGroupId(createPay.getTagGroupIdList().get(i));;
					GenericityUtil.setDate(dramaTagRelEntity);
					dramaTagRelDao.insert(dramaTagRelEntity);
				}
			}
			String path = String.format(Constants.FILE_UPLOAD_SITE, entity.getId());
			String url = QiniuUploadUtils.uploadFile(file,path);
			entity.setCoverUrl(url);
			dramaDao.updateById(entity);
			return setResultSuccess(entity,I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}


	@Override
	public ResponseBase update(UpdateDramaQuery createPay, MultipartFile file) {
		try {
			DramaEntity entity = dramaDao.selectById(createPay.getId());
			if(entity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			if(file != null) {
				String path = String.format(Constants.FILE_UPLOAD_SITE, entity.getId());
				String url = QiniuUploadUtils.uploadFile(file,path);
				entity.setCoverUrl(url);
			}
			if(createPay.getTagGroupIdList() != null && createPay.getTagGroupIdList().size() > 0) {
				dramaTagRelDao.deleteByDramaId(entity.getId());
				for (int i = 0; i < createPay.getTagGroupIdList().size(); i++) {
					DramaTagRelEntity dramaTagRelEntity = new DramaTagRelEntity();
					dramaTagRelEntity.setDramaId(entity.getId());
					dramaTagRelEntity.setTagGroupId(createPay.getTagGroupIdList().get(i));;
					GenericityUtil.setDate(dramaTagRelEntity);
					dramaTagRelDao.insert(dramaTagRelEntity);
				}
			}
			dramaDao.updateById(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}


	@Override
	public ResponseBase findList(@RequestBody QueryDramaQuery entity) {
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
				}
			}
			PageInfo<DramaEntity> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
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
			e.printStackTrace();
			throw new RuntimeException();
		}
	}


	@Override
	public ResponseBase verifyStatus(Integer id, Integer verifyStatus) {
		try {
			DramaEntity entity = dramaDao.selectById(id);
			if(entity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			entity.setVerifyStatus(verifyStatus);
			dramaDao.updateById(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}


	@Override
	public ResponseBase findVideo(Integer id) {
		try {
			DramaEntity entity = dramaDao.selectById(id);
			if(entity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			List<DramaAssetRes> list = dramaAssetDao.findByDramaId(id);
			return setResultSuccess(list, I18nUtil.getMessage("base_success")); 
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	

}
