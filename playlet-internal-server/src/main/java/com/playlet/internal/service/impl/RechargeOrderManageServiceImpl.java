package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.DramaEpisodePayQuery;
import com.playlet.internal.api.request.DramaPaymentDetailQuery;
import com.playlet.internal.api.request.DramaPaymentPurchaseQuery;
import com.playlet.internal.api.request.DramaPaymentQuery;
import com.playlet.internal.api.request.RechargeOrderQuery;
import com.playlet.internal.api.request.RechargeOrderRefundQuery;
import com.playlet.internal.api.response.DramaEpisodePayItemEntity;
import com.playlet.internal.api.response.DramaPaymentDetailEntity;
import com.playlet.internal.api.response.DramaPaymentRespEntity;
import com.playlet.internal.api.response.DramaPurchaseItemEntity;
import com.playlet.internal.api.response.RechargeOrderItemEntity;
import com.playlet.internal.api.response.RechargeOrderSummaryEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.member.RechargeOrderDao;
import com.playlet.internal.enums.RechargeProductTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.RechargeOrderManageService;
import com.playlet.internal.service.support.OrderRefundSupport;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端充值订单：统一列表、统计、详情、标记退款。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class RechargeOrderManageServiceImpl implements RechargeOrderManageService {

	@Autowired
	private RechargeOrderDao rechargeOrderDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private MediaUrlService mediaUrlService;
	@Autowired
	private OrderRefundSupport orderRefundSupport;

	@Override
	@SysLogAnnotation(module = "充值订单", type = "POST", remark = "统计")
	public ResponseBase summary() {
		RechargeOrderSummaryEntity data = rechargeOrderDao.summary();
		if (data == null) {
			data = new RechargeOrderSummaryEntity();
			data.setTodayAmount(BigDecimal.ZERO);
			data.setTodayOrderCount(0L);
			data.setRefundRate(BigDecimal.ZERO);
			data.setPendingSettleAmount(BigDecimal.ZERO);
		}
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "充值订单", type = "POST", remark = "列表")
	public ResponseBase findList(@RequestBody(required = false) RechargeOrderQuery query) {
		if (query == null) {
			query = new RechargeOrderQuery();
		}
		if (!StringUtils.isEmpty(query.getProductType())
				&& !RechargeProductTypeEnums.isValid(query.getProductType())) {
			return setResultError("productType 须为 MEMBER / EPISODE");
		}
		if (!StringUtils.isEmpty(query.getKeyword())) {
			query.setKeyword(query.getKeyword().trim());
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<RechargeOrderItemEntity> list = rechargeOrderDao.findAdminList(query);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "充值订单", type = "POST", remark = "详情")
	public ResponseBase detail(@RequestBody RechargeOrderRefundQuery query) {
		if (query == null || StringUtils.isEmpty(query.getOrderNo())) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		String orderNo = query.getOrderNo().trim();
		String productType = StringUtils.isEmpty(query.getProductType()) ? null : query.getProductType().trim();
		RechargeOrderItemEntity row = rechargeOrderDao.findDetail(orderNo, productType);
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "充值订单", type = "POST", remark = "退款")
	public ResponseBase refund(@RequestBody RechargeOrderRefundQuery query) {
		try {
			if (query == null || StringUtils.isEmpty(query.getOrderNo())
					|| StringUtils.isEmpty(query.getProductType())) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			RechargeProductTypeEnums type = RechargeProductTypeEnums.ofCode(query.getProductType());
			if (type == null) {
				return setResultError("productType 须为 MEMBER / EPISODE");
			}
			String orderNo = query.getOrderNo().trim();
			orderRefundSupport.markRefunded(orderNo, type, query.getRemark());
			log.info("recharge order refund ok orderNo={} productType={}", orderNo, type.getCode());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("recharge order refund biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("recharge order refund failed", e);
			throw new BaseException("退款失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "充值订单", type = "POST", remark = "短剧付费")
	public ResponseBase dramaInfo(@RequestBody(required = false) DramaPaymentQuery query) {
		if (query == null) {
			query = new DramaPaymentQuery();
		}
		if (!StringUtils.isEmpty(query.getDramaName())) {
			query.setDramaName(query.getDramaName().trim());
		}
		if (!StringUtils.isEmpty(query.getDramaTag())) {
			query.setDramaTag(query.getDramaTag().trim());
		}
		if (!StringUtils.isEmpty(query.getPayType())) {
			String payType = query.getPayType().trim().toUpperCase();
			if (!"MEMBER".equals(payType) && !"PAY".equals(payType) && !"AD".equals(payType)) {
				return setResultError("payType 须为 MEMBER / PAY / AD");
			}
			query.setPayType(payType);
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<DramaPaymentRespEntity> list = dramaDao.selectPayDrama(query);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "充值订单", type = "POST", remark = "短剧付费详情")
	public ResponseBase dramaDetail(@RequestBody DramaPaymentDetailQuery query) {
		if (query == null || query.getDramaId() == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		Integer dramaId = query.getDramaId();
		DramaPaymentDetailEntity detail = dramaDao.selectPayDramaDetail(dramaId);
		if (detail == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		// 封面签名，便于管理端直接展示
		detail.setCoverUrl(mediaUrlService.sign(detail.getCoverUrl()));
		detail.setRevenueMomRate(calcRevenueMomRate(dramaId));

		// 详情摘要默认只展示 5 条（对齐管理端原型）
		int limit = query.getEpisodeLimit() == null || query.getEpisodeLimit() <= 0
				? 5 : query.getEpisodeLimit();
		List<DramaEpisodePayItemEntity> episodes = dramaDao.selectEpisodePayList(dramaId, limit);
		if (episodes == null) {
			episodes = new ArrayList<>();
		}
		fillEpisodeRevenueRate(episodes, detail.getTotalRevenue());
		detail.setEpisodePayList(episodes);
		detail.setEpisodePayTotal(detail.getPaidEpisodes() == null ? episodes.size() : detail.getPaidEpisodes());
		return setResultSuccess(detail, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "充值订单", type = "POST", remark = "剧集付费明细")
	public ResponseBase dramaEpisodePayList(@RequestBody DramaEpisodePayQuery query) {
		if (query == null || query.getDramaId() == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		Integer dramaId = query.getDramaId();
		DramaPaymentDetailEntity base = dramaDao.selectPayDramaDetail(dramaId);
		if (base == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<DramaEpisodePayItemEntity> list = dramaDao.selectEpisodePayList(dramaId, null);
		if (list == null) {
			list = new ArrayList<>();
		}
		fillEpisodeRevenueRate(list, base.getTotalRevenue());
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "充值订单", type = "POST", remark = "短剧购买记录")
	public ResponseBase dramaPurchaseList(@RequestBody DramaPaymentPurchaseQuery query) {
		if (query == null || query.getDramaId() == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		if (!StringUtils.isEmpty(query.getKeyword())) {
			query.setKeyword(query.getKeyword().trim());
		}
		if (!StringUtils.isEmpty(query.getPayChannel())) {
			query.setPayChannel(query.getPayChannel().trim().toUpperCase());
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<DramaPurchaseItemEntity> list = dramaDao.selectDramaPurchaseList(query);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "充值订单", type = "POST", remark = "短剧付费集下拉")
	public ResponseBase dramaPaidEpisodeOptions(@RequestBody DramaPaymentDetailQuery query) {
		if (query == null || query.getDramaId() == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		List<DramaEpisodePayItemEntity> list = dramaDao.selectPaidEpisodeOptions(query.getDramaId());
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	/** 收入环比：本月 vs 上月 */
	private BigDecimal calcRevenueMomRate(Integer dramaId) {
		Map<String, Object> mom = dramaDao.selectPayDramaMom(dramaId);
		if (mom == null || mom.isEmpty()) {
			return null;
		}
		BigDecimal thisMonth = mapDecimal(mom, "thisMonth");
		BigDecimal lastMonth = mapDecimal(mom, "lastMonth");
		if (lastMonth.compareTo(BigDecimal.ZERO) <= 0) {
			return null;
		}
		return thisMonth.subtract(lastMonth)
				.multiply(BigDecimal.valueOf(100))
				.divide(lastMonth, 1, RoundingMode.HALF_UP);
	}

	/** MyBatis Map 键大小写不统一时兼容取值 */
	private static BigDecimal mapDecimal(Map<String, ?> map, String key) {
		Object val = map.get(key);
		if (val == null) {
			val = map.get(key.toLowerCase());
		}
		if (val == null) {
			val = map.get(key.toUpperCase());
		}
		return toDecimal(val);
	}

	/** 按剧总收入回填各集收入占比 */
	private void fillEpisodeRevenueRate(List<DramaEpisodePayItemEntity> episodes, BigDecimal totalRevenue) {
		BigDecimal total = totalRevenue == null ? BigDecimal.ZERO : totalRevenue;
		for (DramaEpisodePayItemEntity ep : episodes) {
			BigDecimal revenue = ep.getRevenue() == null ? BigDecimal.ZERO : ep.getRevenue();
			if (total.compareTo(BigDecimal.ZERO) <= 0) {
				ep.setRevenueRate(BigDecimal.ZERO);
			} else {
				ep.setRevenueRate(revenue.multiply(BigDecimal.valueOf(100))
						.divide(total, 1, RoundingMode.HALF_UP));
			}
		}
	}

	private static BigDecimal toDecimal(Object val) {
		if (val == null) {
			return BigDecimal.ZERO;
		}
		if (val instanceof BigDecimal) {
			return (BigDecimal) val;
		}
		return new BigDecimal(String.valueOf(val));
	}

}
