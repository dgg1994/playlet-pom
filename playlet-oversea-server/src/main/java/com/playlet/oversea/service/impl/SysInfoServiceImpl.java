package com.playlet.oversea.service.impl;


import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.api.request.ContentItemEntity;
import com.playlet.oversea.api.request.SysInfoAddEntity;
import com.playlet.oversea.api.response.SysInfoGroupEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.system.SysInfoDao;
import com.playlet.oversea.entity.system.SysInfoEntity;
import com.playlet.oversea.enums.LanguageEnums;
import com.playlet.oversea.enums.NoticeStateEnums;
import com.playlet.oversea.enums.SysConfigTypeEnums;
import com.playlet.oversea.constants.WalletSysConfigConstants;
import com.playlet.oversea.service.SysInfoService;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.HtmlSanitizeUtils;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.QiniuUploadUtils;
import com.playlet.oversea.utils.StringUtils;
import com.playlet.oversea.utils.SysConfigHtmlUtils;
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
				SysInfoEntity sysInfoEntity = buildSysInfoRow(entity.getConfigType(), contentItem,
						NoticeStateEnums.NORMAL.getIndex());
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
				SysInfoEntity row = buildSysInfoRow(entity.getConfigType(), contentItem, status);
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

	/**
	 * 组装 sys_info 行：钱包充值介绍自动生成 HTML 上传七牛（对齐 onetoken），供 countInfo/countUrl。
	 */
	private SysInfoEntity buildSysInfoRow(Integer configType, ContentItemEntity contentItem, Integer status) {
		SysInfoEntity row = new SysInfoEntity();
		row.setConfigType(configType);
		row.setConfigTypeName(SysConfigTypeEnums.getName(configType));
		row.setConfigLable(SysConfigTypeEnums.getLable(configType));
		row.setStatus(status);
		String configName = contentItem.getConfigName();
		if (StringUtils.isEmpty(configName)) {
			configName = SysConfigTypeEnums.getLanguageName(configType, contentItem.getLanguage());
		}
		row.setConfigName(HtmlSanitizeUtils.plain(configName));
		String sanitized = HtmlSanitizeUtils.rich(contentItem.getConfigContent());
		row.setConfigContent(sanitized);
		row.setLanguage(contentItem.getLanguage());
		// 钱包充值介绍：包装完整 HTML 上传七牛，供 topinUsdtAddress 的 countInfo/countUrl（对齐 onetoken）
		boolean walletCenter = WalletSysConfigConstants.WALLET_CENTER_CONFIG_TYPE == configType;
		if (walletCenter && !StringUtils.isEmpty(sanitized)) {
			String typeName = SysConfigTypeEnums.getType(configType);
			String lang = contentItem.getLanguage() == null ? "" : contentItem.getLanguage();
			String fileName = typeName + "_" + lang + ".html";
			byte[] htmlBytes = SysConfigHtmlUtils.toFullHtmlBytes(sanitized);
			String key = QiniuUploadUtils.uploadConfigHtml(htmlBytes, fileName);
			row.setConfigUrl(key);
			log.info("sys config html uploaded configType={} language={} key={}", configType, lang, key);
		} else {
			row.setConfigUrl(contentItem.getConfigUrl());
		}
		return row;
	}

}
