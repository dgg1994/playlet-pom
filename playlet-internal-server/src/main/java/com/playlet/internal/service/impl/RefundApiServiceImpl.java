package com.playlet.internal.service.impl;

import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.RefundApplyCreateQuery;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.member.RefundApplyDao;
import com.playlet.internal.entity.member.RefundApplyEntity;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.RefundApiService;
import com.playlet.internal.service.support.RefundApplySupport;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * C端退款申请提交。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class RefundApiServiceImpl implements RefundApiService {

	@Autowired
	private RefundApplyDao refundApplyDao;
	@Autowired
	private RefundApplySupport refundApplySupport;

	@Override
	@SysLogAnnotation(module = "退款申请", type = "POST", remark = "用户申请")
	public ResponseBase apply(HttpServletRequest request, @RequestBody RefundApplyCreateQuery query) {
		try {
			Integer uid = AppTokenUtil.resolveUid(request);
			if (uid == null) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			RefundApplyEntity apply = refundApplySupport.buildApplyFromOrder(query, uid);
			GenericityUtil.setDate(apply);
			refundApplyDao.insert(apply);
			log.info("refund apply submitted applyNo={} orderNo={} uid={}",
					apply.getApplyNo(), apply.getOrderNo(), uid);
			return setResultSuccess(apply, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("refund apply biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("refund apply failed", e);
			throw new BaseException("提交退款申请失败", e);
		}
	}
}
