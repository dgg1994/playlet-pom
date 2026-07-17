package com.playlet.internal.service.impl;

import java.util.List;

import com.playlet.internal.enums.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.system.DicEntity;
import com.playlet.internal.service.DicService;
import com.playlet.internal.utils.I18nUtil;

@RestController
@Transactional
@CrossOrigin
public class DicServiceImpl extends BaseApiService implements DicService{
		
	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询语言列表")
	public ResponseBase getLanguage() {
		List<DicEntity> list = LanguageEnums.getLableList();
		return setResultSuccess(list,I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询用户状态")
	public ResponseBase findUserState() {
		List<DicEntity> list = UserStateEnums.getList();
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}
	
	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询设备类型")
	public ResponseBase findDeviceType() {
		List<DicEntity> list = DeviceTypeEnums.getList();
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase findOrderState() {
		List<DicEntity> list = OrderStatusEnum.getList();
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase findVerifyStatus() {
		List<DicEntity> list = VerifyStateEnums.getList();
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase findWelfareActionType() {
		List<DicEntity> list = WelfareActionTypeEnums.getList();
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

}

