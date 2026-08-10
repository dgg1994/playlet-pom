package com.playlet.oversea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.dao.drama.DramaDao;
import com.playlet.oversea.dao.drama.DramaTagRelDao;
import com.playlet.oversea.dao.drama.TagDao;
import com.playlet.oversea.entity.drama.DramaEntity;
import com.playlet.oversea.entity.drama.DramaTagRelEntity;
import com.playlet.oversea.entity.drama.TagEntity;
import com.playlet.oversea.service.TagService;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class TagServiceImpl extends BaseApiService implements TagService {

	@Autowired
	private TagDao tagDao;

    @Autowired
    private DramaTagRelDao dramaTagRelDao;

    @Autowired
    private DramaDao dramaDao;

	@Override
	@SysLogAnnotation(module = "短剧标签", type = "POST", remark = "标签列表")
	public ResponseBase findList(@RequestBody TagEntity entity) {
		if (entity == null) {
			entity = new TagEntity();
		}
		if (StringUtils.isEmpty(entity.getLangue())) {
			entity.setLangue(LanguageContext.getLanguage());
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<TagEntity> list = tagDao.findAppList(entity);
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

    @Override
    @SysLogAnnotation(module = "根据标签id查询绑定的短剧", type = "GET", remark = "根据标签id查询绑定的短剧")
    public ResponseBase findDramas(@RequestParam("tagId") Integer tagId) {
        List<DramaTagRelEntity> entitys = dramaTagRelDao.selectByTagId(tagId);
        if (entitys == null){
            entitys = new ArrayList<>();
            return setResultSuccess(entitys, I18nUtil.getMessage("base_success"));
        }
        for (DramaTagRelEntity entity : entitys) {
            entity.setDrama(dramaDao.selectOne(new QueryWrapper<DramaEntity>().eq("id", entity.getDramaId())));
        }
        return setResultSuccess(entitys, I18nUtil.getMessage("base_success"));
    }

}
