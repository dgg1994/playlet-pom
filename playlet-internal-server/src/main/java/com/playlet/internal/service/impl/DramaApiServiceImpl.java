package com.playlet.internal.service.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.TagDao;
import com.playlet.internal.dao.drama.UserDramaCollectDao;
import com.playlet.internal.dao.drama.UserDramaLikeDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.TagEntity;
import com.playlet.internal.entity.drama.UserDramaCollectEntity;
import com.playlet.internal.entity.drama.UserDramaLikeEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.enums.VerifyStateEnums;
import com.playlet.internal.query.drama.RecommendDramaQuery;
import com.playlet.internal.response.drama.DramaAssetRes;
import com.playlet.internal.response.drama.RecommendDramaRes;
import com.playlet.internal.response.drama.RecommendVidoeRes;
import com.playlet.internal.service.DramaApiService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;

@RestController
@Transactional
@CrossOrigin
public class DramaApiServiceImpl extends BaseApiService implements DramaApiService{
	
	@Autowired
	private DramaDao dramaDao;
	
	@Autowired
	private DramaAssetDao dramaAssetDao;
	
	@Autowired
	private TagDao tagDao;
	
	@Autowired
	private UserDramaLikeDao userDramaLikeDao;
	
	@Autowired
	private UserDramaCollectDao userDramaCollectDao;

	@Override
	public ResponseBase recommend(@RequestBody RecommendDramaQuery entity, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
			entity.setVerifyStatus(VerifyStateEnums.AVAILABLE_NOW.getIndex());
			List<RecommendDramaRes> list = dramaDao.recommendList(entity);
			if(list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					RecommendVidoeRes vidoeRes = dramaAssetDao.findDramaIdOne(list.get(i).getId(),DeleteStateEnum.NORMAL.getIndex());
					vidoeRes.setCollectScore(list.get(i).getCollectScore());
					vidoeRes.setShareScore(list.get(i).getShareScore());
					vidoeRes.setVideoUrl(null);
					if(uid != null) {
						UserDramaLikeEntity dramaLikeEntity = userDramaLikeDao.findByVoideId(vidoeRes.getId());
						if(dramaLikeEntity != null) {
							vidoeRes.setIsLike(PublicEnums.ONE.getIndex());
						}
						UserDramaCollectEntity collectEntity = userDramaCollectDao.findByVoideId(vidoeRes.getId());
						if(collectEntity != null) {
							vidoeRes.setIsCollect(PublicEnums.ONE.getIndex());
						}
					}else {
						vidoeRes.setIsLike(PublicEnums.ZERO.getIndex());
						vidoeRes.setIsCollect(PublicEnums.ZERO.getIndex());
					}
					list.get(i).setVidoeRes(vidoeRes);
				}
			}
			PageInfo<RecommendDramaRes> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}


	@Override
	public ResponseBase playVideoReport(Integer id) {
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

	@Override
	public ResponseBase selections(Integer id, HttpServletRequest request) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			List<DramaAssetRes> list = dramaAssetDao.findByDramaId(id);
			if(list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					if(uid != null) {
						UserDramaLikeEntity dramaLikeEntity = userDramaLikeDao.findByVoideId(list.get(i).getId());
						if(dramaLikeEntity != null) {
							list.get(i).setIsLike(PublicEnums.ONE.getIndex());
						}
						UserDramaCollectEntity collectEntity = userDramaCollectDao.findByVoideId(list.get(i).getId());
						if(collectEntity != null) {
							list.get(i).setIsCollect(PublicEnums.ONE.getIndex());
						}
					}else {
						list.get(i).setIsLike(PublicEnums.ZERO.getIndex());
						list.get(i).setIsCollect(PublicEnums.ZERO.getIndex());
					}
				}
			}
			return setResultSuccess(list, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase getVideoUrl(Integer id) {
		try {
			String url = dramaAssetDao.findVideoUrl(id);
			return setResultSuccess(url, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase workInfo(Integer id) {
		try {
			String language = LanguageContext.getLanguage();
			DramaEntity entity = dramaDao.selectById(id);
			if(entity == null) {
				return  setResultError(I18nUtil.getMessage("base_error"));
			}
			if(VerifyStateEnums.REMOVED_SHELVES.getIndex().equals(entity.getVerifyStatus())) {
				return  setResultError(I18nUtil.getMessage("video_removed_shelves"));
			}
			if(DeleteStateEnum.DELETE.getIndex().equals(entity.getDeleteState())) {
				return  setResultError(I18nUtil.getMessage("video_delete"));
			}
			List<TagEntity> tagList = tagDao.findGroupLang(language,entity.getId());
			entity.setTagList(tagList);
			return setResultSuccess(entity, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase relatedWork(Integer id) {
		try {
			List<RecommendDramaRes> list = dramaDao.relatedWork(id,DeleteStateEnum.NORMAL.getIndex(),VerifyStateEnums.AVAILABLE_NOW.getIndex());
			return setResultSuccess(list, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase playVideo(Integer id) {
		try {
			RecommendDramaRes dramaRes = dramaDao.findById(id);
			if(dramaRes != null ) {
				RecommendVidoeRes vidoeRes = dramaAssetDao.findDramaIdOne(dramaRes.getId(),DeleteStateEnum.NORMAL.getIndex());
				vidoeRes.setCollectScore(dramaRes.getCollectScore());
				vidoeRes.setShareScore(dramaRes.getShareScore());
				dramaRes.setVidoeRes(vidoeRes);
			}
			return setResultSuccess(dramaRes, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}


	@Override
	public ResponseBase getVideoInfo(Integer id) {
		try {
			DramaAssetEntity vidoeRes = dramaAssetDao.selectById(id);
			if(vidoeRes != null) {
				DramaEntity dramaEntity = dramaDao.findByVideoId(id);				
				if(dramaEntity != null) {
					vidoeRes.setCollectScore(dramaEntity.getCollectScore());
					vidoeRes.setShareScore(dramaEntity.getShareScore());
					vidoeRes.setVideoUrl(null);					
				}
			}
			return setResultSuccess(vidoeRes, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
