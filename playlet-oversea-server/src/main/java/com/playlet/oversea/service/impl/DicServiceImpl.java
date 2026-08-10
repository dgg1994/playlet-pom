package com.playlet.oversea.service.impl;

import java.util.List;

import com.playlet.oversea.enums.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.system.DicEntity;
import com.playlet.oversea.service.DicService;
import com.playlet.oversea.utils.I18nUtil;

@RestController
@Transactional(rollbackFor = Exception.class)
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

	@Override
	public ResponseBase findWelfareCycleType() {
		List<DicEntity> list = WelfareCycleTypeEnums.getList();
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询协议类型")
	public ResponseBase findProtocolType() {
		List<DicEntity> list = SysConfigTypeEnums.getProtocolType();
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

}

