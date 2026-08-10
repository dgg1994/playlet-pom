package com.playlet.oversea.service.impl;

import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.dao.system.SysInfoDao;
import com.playlet.oversea.entity.system.SysInfoEntity;
import com.playlet.oversea.enums.NoticeStateEnums;
import com.playlet.oversea.service.SysInfoApiService;
import com.playlet.oversea.utils.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 类描述：配置接口实现
 *
 */
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class SysInfoApiServiceImpl implements SysInfoApiService {

    @Autowired
    private SysInfoDao SysInfoDao;

    @Override
    @SysLogAnnotation(module = "配置接口", type = "get", remark = "查询配置（1：用户协议；2：隐私协议；3：关于我们；4：联系我们；5：客服；7：帮助中心）")
    public ResponseBase findConfigInfo(Integer configType) {
        String language = LanguageContext.getLanguage();
        SysInfoEntity entity = SysInfoDao.findContent(configType, NoticeStateEnums.NORMAL.getIndex(),language);
        return setResultSuccess(entity, I18nUtil.getMessage("base_success"));
    }

}