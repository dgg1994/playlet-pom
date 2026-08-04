package com.playlet.internal.service.impl;


import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.ContentItemEntity;
import com.playlet.internal.api.request.SysInfoAddEntity;
import com.playlet.internal.api.response.SysInfoGroupEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.system.SysInfoDao;
import com.playlet.internal.entity.system.SysInfoEntity;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.enums.NoticeStateEnums;
import com.playlet.internal.enums.SysConfigTypeEnums;
import com.playlet.internal.service.SysInfoService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.HtmlSanitizeUtils;
import com.playlet.internal.utils.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class SysInfoServiceImpl extends BaseApiService implements SysInfoService {
	
	@Autowired
	private SysInfoDao sysInfoDao;
	

	@Override
	@SysLogAnnotation(module = "配置接口", type = "POST", remark = "查询所有配置")
	public ResponseBase findAll(@RequestBody SysInfoEntity entity) {
		try {
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			List<SysInfoEntity> typeList = sysInfoDao.findDistinctConfigTypes(entity);
			PageInfo<SysInfoEntity> typePage = new PageInfo<>(typeList);
			List<SysInfoGroupEntity> groups = new ArrayList<>();
			if (typeList != null && !typeList.isEmpty()) {
				List<Integer> configTypes = typeList.stream()
						.map(SysInfoEntity::getConfigType)
						.collect(Collectors.toList());
				List<SysInfoEntity> rows = sysInfoDao.findByConfigTypes(configTypes);
				Map<Integer, SysInfoGroupEntity> groupMap = new LinkedHashMap<>();
				for (SysInfoEntity typeRow : typeList) {
					SysInfoGroupEntity group = new SysInfoGroupEntity();
					group.setConfigType(typeRow.getConfigType());
					group.setConfigTypeName(typeRow.getConfigTypeName());
					group.setConfigLable(typeRow.getConfigLable());
					group.setStatus(typeRow.getStatus());
					group.setConfigContent(new ArrayList<>());
					groupMap.put(typeRow.getConfigType(), group);
				}
				if (rows != null) {
					for (SysInfoEntity row : rows) {
						SysInfoGroupEntity group = groupMap.get(row.getConfigType());
						if (group == null) {
							continue;
						}
						ContentItemEntity item = new ContentItemEntity();
						item.setId(row.getId());
						item.setLanguage(row.getLanguage());
						item.setLanguageName(LanguageEnums.getLable(row.getLanguage()));
						item.setConfigName(row.getConfigName());
						item.setConfigContent(row.getConfigContent());
						item.setConfigUrl(row.getConfigUrl());
						group.getConfigContent().add(item);
					}
				}
				groups.addAll(groupMap.values());
			}
			PageInfo<SysInfoGroupEntity> info = new PageInfo<>();
			info.setList(groups);
			info.setPageNum(typePage.getPageNum());
			info.setPageSize(typePage.getPageSize());
			info.setTotal(typePage.getTotal());
			info.setPages(typePage.getPages());
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "配置接口", type = "GET", remark = "查询配置")
	public ResponseBase findById(Integer id) {
		SysInfoEntity entity = sysInfoDao.selectById(id);
		return setResultSuccess(entity,I18nUtil.getMessage("base_success"));
	}

   

    @Override
    @SysLogAnnotation(module = "配置接口", type = "POST", remark = "新增配置")
    public ResponseBase add(@RequestBody SysInfoAddEntity entity) {
        try {
			if (entity == null || entity.getConfigType() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			List<ContentItemEntity> configContent = entity.getConfigContent();
			if (configContent == null || configContent.isEmpty()) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			Integer count = sysInfoDao.selectCount(new QueryWrapper<SysInfoEntity>().eq("config_type", entity.getConfigType()));
			if (count > 0){
				return setResultError(I18nUtil.getMessage("config_type_isExist"));
			}
			for (ContentItemEntity contentItem : configContent) {
				SysInfoEntity sysInfoEntity = new SysInfoEntity();
				sysInfoEntity.setConfigType(entity.getConfigType());
				sysInfoEntity.setConfigTypeName(SysConfigTypeEnums.getName(entity.getConfigType()));
				sysInfoEntity.setConfigLable(SysConfigTypeEnums.getLable(entity.getConfigType()));
				sysInfoEntity.setStatus(NoticeStateEnums.NORMAL.getIndex());
				sysInfoEntity.setConfigName(HtmlSanitizeUtils.plain(contentItem.getConfigName()));
				sysInfoEntity.setConfigContent(HtmlSanitizeUtils.rich(contentItem.getConfigContent()));
				sysInfoEntity.setConfigUrl(contentItem.getConfigUrl());
				sysInfoEntity.setLanguage(contentItem.getLanguage());
				GenericityUtil.setDate(sysInfoEntity);
				sysInfoDao.insert(sysInfoEntity);
			}
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @SysLogAnnotation(module = "配置接口", type = "POST", remark = "编辑配置信息")
    public ResponseBase update(@RequestBody SysInfoAddEntity entity) {
        try {
			if (entity == null || entity.getConfigType() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			List<ContentItemEntity> configContent = entity.getConfigContent();
			if (configContent == null || configContent.isEmpty()) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			List<SysInfoEntity> oldRows = sysInfoDao.selectList(
					new QueryWrapper<SysInfoEntity>().eq("config_type", entity.getConfigType()));
			Integer status = (oldRows != null && !oldRows.isEmpty() && oldRows.get(0).getStatus() != null)
					? oldRows.get(0).getStatus()
					: NoticeStateEnums.NORMAL.getIndex();
			sysInfoDao.deleteByConfigType(entity.getConfigType());
			for (ContentItemEntity contentItem : configContent) {
				SysInfoEntity row = new SysInfoEntity();
				row.setConfigType(entity.getConfigType());
				row.setConfigTypeName(SysConfigTypeEnums.getName(entity.getConfigType()));
				row.setConfigLable(SysConfigTypeEnums.getLable(entity.getConfigType()));
				row.setStatus(status);
				row.setConfigName(HtmlSanitizeUtils.plain(contentItem.getConfigName()));
				row.setConfigContent(HtmlSanitizeUtils.rich(contentItem.getConfigContent()));
				row.setConfigUrl(contentItem.getConfigUrl());
				row.setLanguage(contentItem.getLanguage());
				GenericityUtil.setDate(row);
				sysInfoDao.insert(row);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

	@Override
	@SysLogAnnotation(module = "配置接口", type = "POST", remark = "启停配置")
	public ResponseBase changeStatus(@RequestBody SysInfoEntity entity) {
		try {
			if (entity == null || entity.getConfigType() == null || entity.getStatus() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (!NoticeStateEnums.NORMAL.getIndex().equals(entity.getStatus())
					&& !NoticeStateEnums.DEACTIVATE.getIndex().equals(entity.getStatus())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			sysInfoDao.updateByConfigType(entity.getConfigType(),entity.getStatus());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

}
