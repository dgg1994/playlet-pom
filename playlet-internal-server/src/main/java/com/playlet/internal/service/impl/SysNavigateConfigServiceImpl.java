package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.AppVersionContext;
import com.playlet.internal.config.heard.DeviceTypeContext;
import com.playlet.internal.dao.system.SysNavigateConfigDao;
import com.playlet.internal.entity.system.SysNavigateConfigEntity;
import com.playlet.internal.service.SysNavigateConfigService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@Transactional
public class SysNavigateConfigServiceImpl extends BaseApiService implements SysNavigateConfigService {
	
	@Autowired
	private SysNavigateConfigDao sysNavigateConfigDao;

	

	@Override
	public ResponseBase update(@RequestBody SysNavigateConfigEntity configEntity) {
		try {
			SysNavigateConfigEntity entity = sysNavigateConfigDao.selectById(configEntity.getId());
			if(entity != null) {
				sysNavigateConfigDao.updateById(configEntity);
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			return setResultError(I18nUtil.getMessage("base_data_null"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase find() {
		String appVersion = AppVersionContext.getAppVersion();
		String deviceType = DeviceTypeContext.getDeviceType();
		SysNavigateConfigEntity configEntity = sysNavigateConfigDao.findOne(appVersion,deviceType);
		if(configEntity != null) {
			return setResultSuccess(configEntity,I18nUtil.getMessage("base_success"));			
		}else {
			SysNavigateConfigEntity entity = new SysNavigateConfigEntity();
			entity.setWalletState(false);
			entity.setWelfareState(false);
			entity.setPayPasswordState(false);
			entity.setSiteState(false);
			return setResultSuccess(entity,I18nUtil.getMessage("base_success"));	
		}
	}

	@Override
	public ResponseBase findList(@RequestBody SysNavigateConfigEntity configEntity) {
		PageHelper.startPage(configEntity.getPageNumber(), configEntity.getPageSize());
		List<SysNavigateConfigEntity> list = sysNavigateConfigDao.findList(configEntity);
		PageInfo<SysNavigateConfigEntity> info = new PageInfo<>(list);
		return setResultSuccess(info, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase add(@RequestBody SysNavigateConfigEntity configEntity) {
		try {
			SysNavigateConfigEntity entity = sysNavigateConfigDao.findOne(configEntity.getAppVersion(),configEntity.getDeviceType());
			if(entity != null) {
				return setResultError(I18nUtil.getMessage("base_info_exist"));
			}
			GenericityUtil.setDate(configEntity);
			sysNavigateConfigDao.insert(configEntity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase delete(Integer id) {
		try {
			SysNavigateConfigEntity entity = sysNavigateConfigDao.selectById(id);
			if(entity != null) {
				sysNavigateConfigDao.deleteById(id);
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}else {
				return setResultError(I18nUtil.getMessage("base_info_exist"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
