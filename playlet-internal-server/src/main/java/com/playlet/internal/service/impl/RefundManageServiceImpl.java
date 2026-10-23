package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.RefundApplyCreateQuery;
import com.playlet.internal.api.request.RefundApplyHandleQuery;
import com.playlet.internal.api.request.RefundApplyQuery;
import com.playlet.internal.api.response.RefundApplyItemEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.member.RefundApplyDao;
import com.playlet.internal.entity.member.RefundApplyEntity;
import com.playlet.internal.enums.RefundApplyStatusEnums;
import com.playlet.internal.enums.RechargeProductTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.RefundManageService;
import com.playlet.internal.service.support.OrderRefundSupport;
import com.playlet.internal.service.support.RefundApplySupport;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.SysUserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端退款管理：列表、同意、拒绝、代提申请。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class RefundManageServiceImpl implements RefundManageService {

	@Autowired
	private RefundApplyDao refundApplyDao;
	@Autowired
	private OrderRefundSupport orderRefundSupport;
	@Autowired
	private RefundApplySupport refundApplySupport;

	@Override
	@SysLogAnnotation(module = "退款管理", type = "POST", remark = "列表")
	public ResponseBase findList(@RequestBody(required = false) RefundApplyQuery query) {
		if (query == null) {
			query = new RefundApplyQuery();
		}
		if (!StringUtils.isEmpty(query.getKeyword())) {
			query.setKeyword(query.getKeyword().trim());
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<RefundApplyItemEntity> list = refundApplyDao.findAdminList(query);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "退款管理", type = "POST", remark = "同意退款")
	public ResponseBase agree(HttpServletRequest request, @RequestBody RefundApplyHandleQuery query) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			if (query == null || query.getId() == null) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			RefundApplyEntity apply = refundApplyDao.selectById(query.getId());
			if (apply == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (!RefundApplyStatusEnums.isPending(apply.getStatus())) {
				return setResultError("仅待处理申请可同意");
			}
			RechargeProductTypeEnums type = RechargeProductTypeEnums.ofCode(apply.getProductType());
			if (type == null) {
				return setResultError("productType 无效");
			}
			String remark = StringUtils.isEmpty(query.getRemark()) ? apply.getRefundReason() : query.getRemark().trim();
			// 同意：标记原单已退款（暂不调渠道）
			orderRefundSupport.markRefunded(apply.getOrderNo(), type, remark);
			RefundApplyEntity upd = new RefundApplyEntity();
			upd.setId(apply.getId());
			upd.setStatus(RefundApplyStatusEnums.REFUNDED.getIndex());
			upd.setHandleAdminId(adminId);
			upd.setHandleTime(new Date());
			upd.setHandleRemark(remark);
			upd.setGmtModified(new Date());
			refundApplyDao.updateById(upd);
			log.info("refund apply agreed id={} orderNo={} adminId={}", apply.getId(), apply.getOrderNo(), adminId);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("refund agree biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("refund agree failed", e);
			throw new BaseException("同意退款失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "退款管理", type = "POST", remark = "拒绝退款")
	public ResponseBase reject(HttpServletRequest request, @RequestBody RefundApplyHandleQuery query) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			if (query == null || query.getId() == null) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			RefundApplyEntity apply = refundApplyDao.selectById(query.getId());
			if (apply == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (!RefundApplyStatusEnums.isPending(apply.getStatus())) {
				return setResultError("仅待处理申请可拒绝");
			}
			RefundApplyEntity upd = new RefundApplyEntity();
			upd.setId(apply.getId());
			upd.setStatus(RefundApplyStatusEnums.REJECTED.getIndex());
			upd.setHandleAdminId(adminId);
			upd.setHandleTime(new Date());
			if (!StringUtils.isEmpty(query.getRemark())) {
				upd.setHandleRemark(query.getRemark().trim());
			}
			upd.setGmtModified(new Date());
			refundApplyDao.updateById(upd);
			log.info("refund apply rejected id={} orderNo={} adminId={}", apply.getId(), apply.getOrderNo(), adminId);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("refund reject biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("refund reject failed", e);
			throw new BaseException("拒绝退款失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "退款管理", type = "POST", remark = "代提申请")
	public ResponseBase create(HttpServletRequest request, @RequestBody RefundApplyCreateQuery query) {
		try {
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			if (adminId == null) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			RefundApplyEntity apply = refundApplySupport.buildApplyFromOrder(query, null);
			GenericityUtil.setDate(apply);
			refundApplyDao.insert(apply);
			log.info("refund apply created by admin id={} orderNo={} adminId={}",
					apply.getId(), apply.getOrderNo(), adminId);
			return setResultSuccess(apply, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("refund create biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("refund create failed", e);
			throw new BaseException("创建退款申请失败", e);
		}
	}
}
