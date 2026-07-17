package com.playlet.internal.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.VerifyStateEnums;
import com.playlet.internal.query.drama.RecommendDramaQuery;
import com.playlet.internal.response.drama.RecommendDramaRes;
import com.playlet.internal.response.drama.RecommendVidoeRes;
import com.playlet.internal.service.DramaApiService;
import com.playlet.internal.utils.I18nUtil;

@RestController
@Transactional
@CrossOrigin
public class DramaApiServiceImpl extends BaseApiService implements DramaApiService{
	
	@Autowired
	private DramaDao dramaDao;
	
	@Autowired
	private DramaAssetDao dramaAssetDao;

	@Override
	public ResponseBase recommend(@RequestBody RecommendDramaQuery entity) {
		try {
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			entity.setVerifyStatus(VerifyStateEnums.AVAILABLE_NOW.getIndex());
			List<RecommendDramaRes> list = dramaDao.recommendList(entity);
			if(list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					RecommendVidoeRes vidoeRes = dramaAssetDao.findDramaIdOne(list.get(i).getId(),DeleteStateEnum.NORMAL.getIndex());
					vidoeRes.setCollectScore(list.get(i).getCollectScore());
					vidoeRes.setShareScore(list.get(i).getShareScore());
					list.get(i).setVidoeRes(vidoeRes);
					System.out.println(JSON.toJSON(vidoeRes));
					System.out.println(JSON.toJSON(list.get(i)));
				}
			}
			 // 👇 在创建 PageInfo 之前打印
	        System.out.println("创建 PageInfo 之前: " + JSON.toJSONString(list));
	        
	        PageInfo<RecommendDramaRes> info = new PageInfo<>(list);
	        
	        // 👇 在创建 PageInfo 之后打印
	        System.out.println("创建 PageInfo 之后: " + JSON.toJSONString(info.getList()));
//			PageInfo<RecommendDramaRes> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase playVideo(Integer id) {
		try {
			DramaEntity dramaEntity = dramaDao.findByVideoId(id);
			if(dramaEntity != null) {
				dramaEntity.setHotScore(dramaEntity.getHotScore() + 1);
				dramaDao.updateById(dramaEntity);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
