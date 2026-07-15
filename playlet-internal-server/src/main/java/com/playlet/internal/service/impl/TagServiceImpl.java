package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.TagDao;
import com.playlet.internal.entity.drama.TagEntity;
import com.playlet.internal.service.TagService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@Transactional
public class TagServiceImpl extends BaseApiService implements TagService {

	@Autowired
	private TagDao tagDao;

	@Override
	@SysLogAnnotation(module = "短剧标签", type = "POST", remark = "标签列表")
	public ResponseBase findList(@RequestBody TagEntity entity) {
		if (entity == null) {
			entity = new TagEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<TagEntity> list = tagDao.findAdminList(entity);
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "短剧标签", type = "GET", remark = "标签详情")
	public ResponseBase detail(@RequestParam String tagId) {
		TagEntity tag = tagDao.selectById(tagId);
		if (tag == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		return setResultSuccess(tag, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "短剧标签", type = "POST", remark = "新增标签")
	public ResponseBase save(@RequestBody TagEntity entity) {
        try {
            if (entity == null || StringUtils.isEmpty(entity.getTagName())) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            if (tagDao.findByTagName(entity.getTagName().trim()) != null) {
                return setResultError(I18nUtil.getMessage("base_info_exist"));
            }
            Date now = new Date();
            entity.setTagName(entity.getTagName().trim());
            entity.setStatus(entity.getStatus() == null ? 1 : entity.getStatus());
            entity.setSortWeight(entity.getSortWeight() == null ? 0 : entity.getSortWeight());
            entity.setSetTime(now);
            entity.setGmtModified(now);
            tagDao.insert(entity);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	@SysLogAnnotation(module = "短剧标签", type = "POST", remark = "编辑标签")
	public ResponseBase update(@RequestBody TagEntity entity) {
        try {
            if (entity == null || entity.getId() == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            TagEntity exist = tagDao.selectById(entity.getId());
            if (exist == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            if (StringUtils.isNotEmpty(entity.getTagName())) {
                TagEntity byName = tagDao.findByTagName(entity.getTagName().trim());
                if (byName != null && !byName.getId().equals(exist.getId())) {
                    return setResultError(I18nUtil.getMessage("base_info_exist"));
                }
                exist.setTagName(entity.getTagName().trim());
            }
            if (entity.getSortWeight() != null) {
                exist.setSortWeight(entity.getSortWeight());
            }

            exist.setGmtModified(new Date());
            tagDao.updateById(exist);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	@SysLogAnnotation(module = "短剧标签", type = "POST", remark = "变更标签状态")
	public ResponseBase changeStatus(@RequestBody TagEntity entity) {
        try {
            if (entity == null || entity.getId() == null || entity.getStatus() == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            if (entity.getStatus() != 0 && entity.getStatus() != 1) {
                return setResultError("状态仅支持 0停用 / 1启用");
            }
            TagEntity exist = tagDao.selectById(entity.getId());
            if (exist == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            exist.setStatus(entity.getStatus());
            exist.setGmtModified(new Date());
            tagDao.updateById(exist);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
