package com.playlet.internal.service.impl;

import java.util.List;

import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.system.SysFaCountryDao;
import com.playlet.internal.dao.system.SyssCountryCodeDao;
import com.playlet.internal.entity.system.SysFaCountryEntity;
import com.playlet.internal.entity.system.SyssCountryCodeEntity;
import com.playlet.internal.enums.*;
import org.springframework.beans.factory.annotation.Autowired;
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
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
public class DicServiceImpl extends BaseApiService implements DicService{

	@Autowired
	private SyssCountryCodeDao syssCountryCodeDao;

	@Autowired
	private SysFaCountryDao sysFaCountryDao;

	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询国家地区代码")
	public ResponseBase getCountryCode() {
		String language = LanguageContext.getLanguage();
		List<SyssCountryCodeEntity> list = syssCountryCodeDao.findLanguage(language);
		return setResultSuccess(list,I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "国家省市级联")
	public ResponseBase findCountry(Integer parentId) {
		String language = LanguageContext.getLanguage();
		if(parentId == null) {
			parentId = 0;
		}
		List<SysFaCountryEntity> list = sysFaCountryDao.findParent(parentId);
		if(list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				if(LanguageEnums.ZH_CN.getName().equals(language)) {
					list.get(i).setName(list.get(i).getCname());
				}else if(LanguageEnums.EN_US.getName().equals(language)){
					list.get(i).setName(list.get(i).getEname());
				}else if(LanguageEnums.ZH_TW.getName().equals(language)){
					list.get(i).setName(list.get(i).getTname());
				}
				list.get(i).setEname(null);
				list.get(i).setCname(null);
				list.get(i).setTname(null);
			}
		}
		return setResultSuccess(list,I18nUtil.getMessage("base_success"));
	}
		
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

	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询卡状态")
	public ResponseBase findCardState() {
		return setResultSuccess(WalletCardStateDicEnums.getList(), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询充值方式")
	public ResponseBase findPayType() {
		return setResultSuccess(WalletPayTypeEnums.getList(), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询KYC状态")
	public ResponseBase findKycState() {
		return setResultSuccess(WalletKycStateEnums.getList(), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询网络类型")
	public ResponseBase findNetwokList() {
		return setResultSuccess(WalletNetworkTypeDicEnums.getList(), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "字典管理", type = "get", remark = "查询审核状态")
	public ResponseBase findRecordState() {
		return setResultSuccess(WalletRecordStateEnums.getList(), I18nUtil.getMessage("base_success"));
	}

}

