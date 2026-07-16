package com.playlet.internal.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.DramaTagRelDao;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.DramaTagRelEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.VerifyStateEnums;
import com.playlet.internal.query.drama.AddDramaQuery;
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
			entity.setVerifyStatus(VerifyStateEnums.AVAILABLE_NOW.getIndex());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			GenericityUtil.setDate(entity);
			dramaDao.insert(entity);
			//添加短剧标签关联
			System.out.println(JSON.toJSON(entity.getTagList()));
			if(createPay.getTagIdList() != null && createPay.getTagIdList().size() > 0) {
				for (int i = 0; i < createPay.getTagIdList().size(); i++) {
					DramaTagRelEntity dramaTagRelEntity = new DramaTagRelEntity();
					dramaTagRelEntity.setDramaId(entity.getId());
					dramaTagRelEntity.setTagId(createPay.getTagIdList().get(i));
					GenericityUtil.setDate(dramaTagRelEntity);
					dramaTagRelDao.insert(dramaTagRelEntity);
				}
			}
			String path = String.format(Constants.FILE_UPLOAD_SITE, entity.getId());
			String url = QiniuUploadUtils.uploadFile(file,path);
			entity.setCoverUrl(url);
			dramaDao.updateById(entity);
			
			return setResultSuccess();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	

}
