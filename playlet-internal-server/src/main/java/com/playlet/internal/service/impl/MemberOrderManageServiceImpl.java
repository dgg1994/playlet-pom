package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.entity.member.MemberOrderEntity;
import com.playlet.internal.service.MemberOrderManageService;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端会员订单查询。
 */
@Slf4j
@RestController
@CrossOrigin
public class MemberOrderManageServiceImpl implements MemberOrderManageService {

	@Autowired
	private MemberOrderDao memberOrderDao;

	@Override
	@SysLogAnnotation(module = "会员订单", type = "POST", remark = "订单列表")
	public ResponseBase findList(@RequestBody(required = false) MemberOrderEntity entity) {
		if (entity == null) {
			entity = new MemberOrderEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<MemberOrderEntity> list = memberOrderDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}
}
