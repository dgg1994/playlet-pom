package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.system.SysInfoDao;
import com.playlet.internal.entity.system.SysInfoEntity;
import com.playlet.internal.enums.FilePathEnums;
import com.playlet.internal.enums.LanguageEnums;
import com.playlet.internal.enums.NoticeStateEnums;
import com.playlet.internal.service.SysInfoService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@CrossOrigin
@Transactional
public class SysInfoServiceImpl extends BaseApiService implements SysInfoService {
	
	@Autowired
	private SysInfoDao SysInfoDao;
	

	@Override
	@SysLogAnnotation(module = "配置接口", type = "POST", remark = "查询所有配置")
	public ResponseBase findAll(@RequestBody SysInfoEntity entity) {
		try {
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			List<SysInfoEntity> list = SysInfoDao.findList(entity);
			if(list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setLanguageName(LanguageEnums.getLable(list.get(i).getLanguage()));
				}
			}
			PageInfo<SysInfoEntity> info = new PageInfo<>(list);
			return setResultSuccess(info, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	@SysLogAnnotation(module = "配置接口", type = "GET", remark = "查询配置")
	public ResponseBase findById(Integer id) {
		SysInfoEntity entity = SysInfoDao.selectById(id);
		return setResultSuccess(entity,I18nUtil.getMessage("base_success"));
	}

   

    @Override
    @SysLogAnnotation(module = "配置接口", type = "POST", remark = "新增配置")
    public ResponseBase add(@RequestBody SysInfoEntity entity) {
        try {
            GenericityUtil.setDate(entity);
            SysInfoDao.insert(entity);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    @SysLogAnnotation(module = "配置接口", type = "POST", remark = "编辑配置信息")
    public ResponseBase update(@RequestBody SysInfoEntity entity) {
        try {
            if(entity.getId() == null){
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            SysInfoEntity infoEntity = SysInfoDao.selectById(entity.getId());
            if(infoEntity != null) {

                SysInfoDao.updateById(entity);
                return setResultSuccess(I18nUtil.getMessage("base_success"));
            }else {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


	@Override
	@SysLogAnnotation(module = "配置接口", type = "get", remark = "查询配置（1：用户协议；2：隐私协议；3：关于我们；4：联系我们；5：客服；7：帮助中心）")
	public ResponseBase findConfigInfo(Integer configType) {
		String language = LanguageContext.getLanguage();
		SysInfoEntity entity = SysInfoDao.findContent(configType, NoticeStateEnums.NORMAL.getIndex(),language);
		return setResultSuccess(entity, I18nUtil.getMessage("base_success"));
	}

	
	
}
