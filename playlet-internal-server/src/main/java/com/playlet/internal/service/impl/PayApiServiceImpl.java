package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.PayPrepayReqEntity;
import com.playlet.internal.api.request.PayQueryReqEntity;
import com.playlet.internal.api.response.PayPrepayResult;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.member.EpisodeUnlockOrderDao;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.dao.member.UserPayAgreementDao;
import com.playlet.internal.entity.member.EpisodeUnlockOrderEntity;
import com.playlet.internal.entity.member.MemberOrderEntity;
import com.playlet.internal.entity.member.UserPayAgreementEntity;
import com.playlet.internal.enums.MemberPayStatusEnums;
import com.playlet.internal.enums.PaySceneEnums;
import com.playlet.internal.enums.RechargeProductTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.PayApiService;
import com.playlet.internal.service.support.OrderPaidSupport;
import com.playlet.internal.service.support.OrderRefundSupport;
import com.playlet.internal.service.support.member.MemberAutoRenewSupport;
import com.playlet.internal.service.support.pay.AlipaySupport;
import com.playlet.internal.service.support.pay.PayOrderResult;
import com.playlet.internal.service.support.pay.PaySupport;
import com.playlet.internal.service.support.pay.WechatPaySupport;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 支付：拉起支付参数、查单补单，以及微信/支付宝异步回调。
 */
@Slf4j
@RestController
@CrossOrigin
public class PayApiServiceImpl implements PayApiService {

	private static final String SUBJECT_MEMBER = "会员套餐购买";
	private static final String SUBJECT_EPISODE = "剧集解锁";

	@Autowired
	private MemberOrderDao memberOrderDao;
	@Autowired
	private EpisodeUnlockOrderDao episodeUnlockOrderDao;
	@Autowired
	private PaySupport paySupport;
	@Autowired
	private WechatPaySupport wechatPaySupport;
	@Autowired
	private AlipaySupport alipaySupport;
	@Autowired
	private OrderPaidSupport orderPaidSupport;
	@Autowired
	private OrderRefundSupport orderRefundSupport;
	@Autowired
	private MemberAutoRenewSupport memberAutoRenewSupport;
	@Autowired
	private UserPayAgreementDao userPayAgreementDao;

	@Override
	public ResponseBase prepay(HttpServletRequest request, @RequestBody PayPrepayReqEntity req) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (req == null || StringUtils.isEmpty(req.getOrderNo())) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		String orderNo = req.getOrderNo().trim();
		try {
			// 取待支付现金订单的渠道/金额/商品类型，并校验归属
			String[] order = loadWaitCashOrder(orderNo, uid);
			String channel = order[0];
			BigDecimal amount = new BigDecimal(order[1]);
			// 该渠道是否已配置且启用
			if (!paySupport.available(channel)) {
				return setResultError("该支付渠道暂不可用，请联系客服");
			}
			PaySceneEnums scene = PaySceneEnums.ofCode(req.getPayScene()) == null
					? PaySceneEnums.PC : PaySceneEnums.ofCode(req.getPayScene());
			String subject = RechargeProductTypeEnums.MEMBER.getCode().equals(order[2])
					? SUBJECT_MEMBER : SUBJECT_EPISODE;
			// 下单
			PayPrepayResult result = paySupport.prepay(channel, scene, orderNo, amount, subject, req.getOpenId());
			return setResultSuccess(result, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("pay prepay failed uid={} orderNo={}", uid, orderNo, e);
			throw new BaseException("获取支付参数失败", e);
		}
	}

	@Override
	public ResponseBase query(HttpServletRequest request, @RequestBody PayQueryReqEntity req) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (req == null || StringUtils.isEmpty(req.getOrderNo())) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		String orderNo = req.getOrderNo().trim();
		try {
			MemberOrderEntity memberOrder = memberOrderDao.findByOrderNo(orderNo);
			EpisodeUnlockOrderEntity episodeOrder =
					memberOrder == null ? episodeUnlockOrderDao.findByOrderNo(orderNo) : null;
			if (memberOrder == null && episodeOrder == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String channel = memberOrder != null ? memberOrder.getPayChannel() : episodeOrder.getPayChannel();
			String thirdTradeNo = memberOrder != null ? memberOrder.getThirdTradeNo()
					: episodeOrder.getThirdTradeNo();
			Integer payStatus = memberOrder != null ? memberOrder.getPayStatus() : episodeOrder.getPayStatus();
			Integer ownerUid = memberOrder != null ? memberOrder.getUid() : episodeOrder.getUid();
			if (ownerUid == null || !ownerUid.equals(uid)) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}

			Map<String, Object> data = new HashMap<>(6);
			data.put("orderNo", orderNo);
			if (MemberPayStatusEnums.isPaid(payStatus)) {
				data.put("paid", true);
				data.put("payStatus", payStatus);
				return setResultSuccess(data, I18nUtil.getMessage("base_success"));
			}
			if (!paySupport.available(channel)) {
				data.put("paid", false);
				data.put("payStatus", payStatus);
				data.put("tradeState", "CHANNEL_UNAVAILABLE");
				return setResultSuccess(data, I18nUtil.getMessage("base_success"));
			}
			PayOrderResult result = paySupport.query(channel, orderNo, thirdTradeNo);
			data.put("tradeState", result.getTradeState());
			if (result.isPaid()) {
				orderPaidSupport.markPaid(orderNo, result.getThirdTradeNo());
				data.put("paid", true);
				data.put("payStatus", MemberPayStatusEnums.PAID.getIndex());
			} else {
				data.put("paid", false);
				data.put("payStatus", payStatus);
			}
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("pay query failed uid={} orderNo={}", uid, orderNo, e);
			throw new BaseException("查询支付状态失败", e);
		}
	}

	@Override
	public void wechatPayNotify(HttpServletRequest request, HttpServletResponse response) {
		String body = null;
		try {
			body = readBody(request);
			String serial = request.getHeader("Wechatpay-Serial");
			String signature = request.getHeader("Wechatpay-Signature");
			String timestamp = request.getHeader("Wechatpay-Timestamp");
			String nonce = request.getHeader("Wechatpay-Nonce");
			if (StringUtils.isEmpty(serial) || StringUtils.isEmpty(signature)) {
				writeWechatNotify(response, false, "验签信息缺失");
				return;
			}
			Transaction tx = wechatPaySupport.parsePayNotify(serial, timestamp, nonce, signature, body);
			if (tx == null || tx.getOutTradeNo() == null) {
				writeWechatNotify(response, false, "报文解析失败");
				return;
			}
			String tradeState = tx.getTradeState() == null ? null : tx.getTradeState().name();
			if (!"SUCCESS".equals(tradeState)) {
				log.info("wechat pay notify not success orderNo={} tradeState={}", tx.getOutTradeNo(), tradeState);
				// 非成功状态仍需回 200，避免平台持续重投
				writeWechatNotify(response, true, null);
				return;
			}
			orderPaidSupport.markPaid(tx.getOutTradeNo(), tx.getTransactionId());
			writeWechatNotify(response, true, null);
		} catch (Exception e) {
			log.error("wechat pay notify failed body={}", body, e);
			writeWechatNotify(response, false, "处理失败");
		}
	}

	@Override
	public void wechatRefundNotify(HttpServletRequest request, HttpServletResponse response) {
		String body = null;
		try {
			body = readBody(request);
			String serial = request.getHeader("Wechatpay-Serial");
			String signature = request.getHeader("Wechatpay-Signature");
			String timestamp = request.getHeader("Wechatpay-Timestamp");
			String nonce = request.getHeader("Wechatpay-Nonce");
			RefundNotification notice = wechatPaySupport.parseRefundNotify(serial, timestamp, nonce, signature, body);
			if (notice == null || notice.getOutTradeNo() == null) {
				writeWechatNotify(response, false, "报文解析失败");
				return;
			}
			String status = notice.getRefundStatus() == null ? null : notice.getRefundStatus().name();
			log.info("wechat refund notify orderNo={} refundNo={} status={}",
					notice.getOutTradeNo(), notice.getOutRefundNo(), status);
			if ("SUCCESS".equals(status)) {
				RechargeProductTypeEnums type = orderRefundSupport.resolveType(notice.getOutTradeNo());
				if (type != null) {
					// 仅补齐本地状态，避免再次调用渠道退款
					orderRefundSupport.markRefundedLocal(notice.getOutTradeNo(), type, "渠道退款成功通知");
				}
			}
			writeWechatNotify(response, true, null);
		} catch (Exception e) {
			log.error("wechat refund notify failed body={}", body, e);
			writeWechatNotify(response, false, "处理失败");
		}
	}

	@Override
	public void alipayPayNotify(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			log.warn("alipay notify set encoding error", e);
		}
		try {
			Map<String, String> params = toParamMap(request.getParameterMap());
			if (params.isEmpty() || !alipaySupport.verifyNotify(params)) {
				writeText(response, "failure");
				return;
			}
			String tradeStatus = params.get("trade_status");
			String orderNo = params.get("out_trade_no");
			String tradeNo = params.get("trade_no");
			if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
				log.info("alipay notify not success orderNo={} tradeStatus={}", orderNo, tradeStatus);
				writeText(response, "success");
				return;
			}
			orderPaidSupport.markPaid(orderNo, tradeNo);
			writeText(response, "success");
		} catch (Exception e) {
			log.error("alipay notify failed", e);
			writeText(response, "failure");
		}
	}

	/**
	 * 取待支付现金订单的渠道/金额/商品类型，并校验归属。
	 *
	 * @return [channel, amount, productType]
	 */
	private String[] loadWaitCashOrder(String orderNo, Integer uid) {
		MemberOrderEntity memberOrder = memberOrderDao.findByOrderNo(orderNo);
		if (memberOrder != null) {
			if (!uid.equals(memberOrder.getUid())) {
				throw new BaseException(I18nUtil.getMessage("base_data_null"));
			}
			if (!MemberPayStatusEnums.WAIT.getIndex().equals(memberOrder.getPayStatus())) {
				throw new BaseException("订单非待支付状态");
			}
			return new String[] {
					memberOrder.getPayChannel(),
					memberOrder.getPayAmount() == null ? "0" : memberOrder.getPayAmount().toPlainString(),
					RechargeProductTypeEnums.MEMBER.getCode()
			};
		}
		EpisodeUnlockOrderEntity episodeOrder = episodeUnlockOrderDao.findByOrderNo(orderNo);
		if (episodeOrder != null) {
			if (!uid.equals(episodeOrder.getUid())) {
				throw new BaseException(I18nUtil.getMessage("base_data_null"));
			}
			if (!MemberPayStatusEnums.WAIT.getIndex().equals(episodeOrder.getPayStatus())) {
				throw new BaseException("订单非待支付状态");
			}
			return new String[] {
					episodeOrder.getPayChannel(),
					episodeOrder.getPayAmount() == null ? "0" : episodeOrder.getPayAmount().toPlainString(),
					RechargeProductTypeEnums.EPISODE.getCode()
			};
		}
		throw new BaseException(I18nUtil.getMessage("base_data_null"));
	}

	@Override
	public void alipayContractNotify(HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> params = alipaySupport.wrapParams(request.getParameterMap());
			String externalNo = params.get("external_agreement_no");
			if (!alipaySupport.verifyNotify(params)) {
				log.warn("alipay contract notify verify failed externalNo={}", externalNo);
				writeText(response, "failure");
				return;
			}
			String status = params.get("status");
			String agreementNo = params.get("agreement_no");
			String alipayUserId = params.get("alipay_user_id");
			log.info("alipay contract notify externalNo={} agreementNo={} status={}",
					externalNo, agreementNo, status);
			if ("NORMAL".equals(status)) {
				memberAutoRenewSupport.markSigned(externalNo, agreementNo, alipayUserId);
			} else if ("UNSIGN".equals(status) || "STOP_SIGN".equals(status)) {
				UserPayAgreementEntity agreement = userPayAgreementDao.findByExternalAgreementNo(externalNo);
				memberAutoRenewSupport.markUnsigned(agreement, "支付宝解约通知 status=" + status);
			} else {
				log.info("alipay contract notify ignored externalNo={} status={}", externalNo, status);
			}
			writeText(response, "success");
		} catch (Exception e) {
			log.error("alipay contract notify failed", e);
			writeText(response, "failure");
		}
	}

	@Override
	public void wechatContractNotify(HttpServletRequest request, HttpServletResponse response) {
		try {
			String body = readBody(request);
			Map<String, Object> notify = wechatPaySupport.parseNotifyMap(
					request.getHeader("Wechatpay-Serial"),
					request.getHeader("Wechatpay-Timestamp"),
					request.getHeader("Wechatpay-Nonce"),
					request.getHeader("Wechatpay-Signature"),
					body);
			String status = notify.get("contract_status") == null
					? null : String.valueOf(notify.get("contract_status"));
			String externalNo = notify.get("out_contract_code") == null
					? null : String.valueOf(notify.get("out_contract_code"));
			String contractId = notify.get("contract_id") == null
					? null : String.valueOf(notify.get("contract_id"));
			log.info("wechat contract notify externalNo={} contractId={} status={}", externalNo, contractId, status);
			if ("SIGNED".equals(status)) {
				memberAutoRenewSupport.markSigned(externalNo, contractId, null);
			} else if ("TERMINATED".equals(status) || "EXPIRED".equals(status)) {
				UserPayAgreementEntity agreement = userPayAgreementDao.findByExternalAgreementNo(externalNo);
				memberAutoRenewSupport.markUnsigned(agreement, "微信解约通知 status=" + status);
			} else {
				log.info("wechat contract notify ignored externalNo={} status={}", externalNo, status);
			}
			writeWechatNotify(response, true, null);
		} catch (Exception e) {
			log.error("wechat contract notify failed", e);
			writeWechatNotify(response, false, "处理失败");
		}
	}

	private String readBody(HttpServletRequest request) throws Exception {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		}
		return sb.toString();
	}

	private Map<String, String> toParamMap(Map<String, String[]> rawParams) {
		Map<String, String> params = new HashMap<>(rawParams.size());
		for (Map.Entry<String, String[]> entry : rawParams.entrySet()) {
			String[] values = entry.getValue();
			params.put(entry.getKey(), values == null || values.length == 0 ? null : values[0]);
		}
		return params;
	}

	/** 微信回调应答：成功回 200 空报文，失败回 5xx + FAIL */
	private void writeWechatNotify(HttpServletResponse response, boolean success, String message) {
		response.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.setContentType("application/json;charset=UTF-8");
		try {
			if (success) {
				response.getWriter().write("{\"code\":\"SUCCESS\"}");
			} else {
				response.getWriter().write("{\"code\":\"FAIL\",\"message\":\""
						+ (message == null ? "失败" : message) + "\"}");
			}
		} catch (Exception e) {
			log.warn("write wechat notify response error", e);
		}
	}

	private void writeText(HttpServletResponse response, String text) {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain;charset=UTF-8");
		try {
			response.getWriter().write(text);
		} catch (Exception e) {
			log.warn("write notify response error", e);
		}
	}
}
