package com.playlet.internal.service.support.pay;

import com.playlet.internal.api.response.PayContractResult;
import com.playlet.internal.api.response.PayDeductResult;
import com.playlet.internal.api.response.PayPrepayResult;
import com.playlet.internal.enums.MemberPayChannelEnums;
import com.playlet.internal.enums.PaySceneEnums;
import com.playlet.internal.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 三方支付统一入口：按 payChannel 路由到微信/支付宝。
 */
@Slf4j
@Component
public class PaySupport {

	@Autowired
	private WechatPaySupport wechatPaySupport;
	@Autowired
	private AlipaySupport alipaySupport;
	@Autowired
	private WechatPapaySupport wechatPapaySupport;

	/** 该渠道是否已配置且启用（未配置时调用方应走本地兜底逻辑） */
	public boolean available(String channel) {
		MemberPayChannelEnums ch = MemberPayChannelEnums.ofCode(channel);
		if (ch == null) {
			return false;
		}
		switch (ch) {
			case WECHAT:
				return wechatPaySupport.enabled();
			case ALIPAY:
				return alipaySupport.enabled();
			default:
				return false;
		}
	}

	/**
	 * 下单。
	 *
	 * @param channel    WECHAT/ALIPAY
	 * @param scene      支付场景，空则按 PC 处理
	 * @param subject    商品标题
	 * @param openId     微信 JSAPI 场景的 openid，可空
	 * @return 渠道不可用返回 null，由调用方决定是否降级
	 */
	public PayPrepayResult prepay(String channel, PaySceneEnums scene, String orderNo,
			BigDecimal amountYuan, String subject, String openId) {
		MemberPayChannelEnums ch = requireChannel(channel);
		try {
			PaySceneEnums target = scene == null ? PaySceneEnums.PC : scene;
			if (ch == MemberPayChannelEnums.ALIPAY) {
				// 支付宝下单
				return alipaySupport.prepay(target, orderNo, amountYuan, subject, buildAttach(orderNo));
			}
			// 微信下单
			return wechatPaySupport.prepay(target, orderNo, amountYuan, subject, openId);
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("pay prepay failed channel={} orderNo={}", channel, orderNo, e);
			throw new BaseException("支付下单失败", e);
		}
	}

	/**
	 * 申请退款。
	 *
	 * @param refundNo 商户退款单号（幂等键）
	 * @return 结果为 false 时应保留原订单状态由人工复核
	 */
	public PayRefundResult refund(String channel, String orderNo, String thirdTradeNo, String refundNo,
			BigDecimal payAmountYuan, BigDecimal refundAmountYuan, String reason) {
		MemberPayChannelEnums ch = requireChannel(channel);
		try {
			if (ch == MemberPayChannelEnums.ALIPAY) {
				return alipaySupport.refund(orderNo, thirdTradeNo, refundNo, refundAmountYuan, reason);
			}
			return wechatPaySupport.refund(orderNo, refundNo, payAmountYuan, refundAmountYuan, reason);
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("pay refund failed channel={} orderNo={} refundNo={}", channel, orderNo, refundNo, e);
			throw new BaseException("渠道退款失败", e);
		}
	}

	/** 渠道查单 */
	public PayOrderResult query(String channel, String orderNo, String thirdTradeNo) {
		MemberPayChannelEnums ch = requireChannel(channel);
		try {
			if (ch == MemberPayChannelEnums.ALIPAY) {
				return alipaySupport.queryOrder(orderNo, thirdTradeNo);
			}
			if (thirdTradeNo != null && !thirdTradeNo.trim().isEmpty()) {
				return wechatPaySupport.queryOrderByTradeNo(thirdTradeNo.trim());
			}
			return wechatPaySupport.queryOrder(orderNo);
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("pay query failed channel={} orderNo={}", channel, orderNo, e);
			throw new BaseException("渠道查单失败", e);
		}
	}

	/** 关闭渠道未支付订单；失败仅告警 */
	public void close(String channel, String orderNo) {
		MemberPayChannelEnums ch = MemberPayChannelEnums.ofCode(channel);
		if (ch == null) {
			return;
		}
		try {
			if (ch == MemberPayChannelEnums.ALIPAY) {
				alipaySupport.closeOrder(orderNo);
			} else if (ch == MemberPayChannelEnums.WECHAT && wechatPaySupport.enabled()) {
				wechatPaySupport.closeOrder(orderNo);
			}
		} catch (Exception e) {
			log.warn("pay close failed channel={} orderNo={} msg={}", channel, orderNo, e.getMessage());
		}
	}

	// ------------------------------------------------------------ 连续包月（代扣）

	/** 渠道是否支持签约代扣（微信需额外配置 plan_id） */
	public boolean contractAvailable(String channel) {
		MemberPayChannelEnums ch = MemberPayChannelEnums.ofCode(channel);
		if (ch == null) {
			return false;
		}
		if (ch == MemberPayChannelEnums.ALIPAY) {
			return alipaySupport.enabled();
		}
		if (ch == MemberPayChannelEnums.WECHAT) {
			return wechatPapaySupport.enabled();
		}
		return false;
	}

	/**
	 * 发起签约（连续包月）。
	 *
	 * @param scene      签约场景 PC/H5/APP/MINI
	 * @param externalNo 商户侧签约号（幂等键）
	 * @param openId     微信 MINI/JSAPI 场景必填
	 */
	public PayContractResult contractSign(String channel, PaySceneEnums scene, String externalNo,
			String productName, BigDecimal amountYuan, String openId) {
		MemberPayChannelEnums ch = requireChannel(channel);
		try {
			if (ch == MemberPayChannelEnums.ALIPAY) {
				return alipaySupport.agreementSign(scene, externalNo, productName, amountYuan);
			}
			return wechatPapaySupport.contractSign(scene, externalNo, productName, openId);
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("pay contract sign failed channel={} externalNo={}", channel, externalNo, e);
			throw new BaseException("签约失败", e);
		}
	}

	/** 查询签约状态；未查到返回 null */
	public String contractQuery(String channel, String externalNo, String agreementNo) {
		MemberPayChannelEnums ch = requireChannel(channel);
		try {
			if (ch == MemberPayChannelEnums.ALIPAY) {
				com.alibaba.fastjson.JSONObject json =
						alipaySupport.agreementQuery(externalNo, agreementNo);
				return json == null ? null : json.getString("status");
			}
			return wechatPapaySupport.contractQuery(null, externalNo, agreementNo);
		} catch (Exception e) {
			log.error("pay contract query failed channel={} externalNo={}", channel, externalNo, e);
			return null;
		}
	}

	/** 解约；失败返回 false */
	public boolean contractUnsign(String channel, String externalNo, String agreementNo) {
		MemberPayChannelEnums ch = MemberPayChannelEnums.ofCode(channel);
		if (ch == null) {
			return false;
		}
		try {
			if (ch == MemberPayChannelEnums.ALIPAY) {
				return alipaySupport.agreementUnsign(externalNo, agreementNo);
			}
			return wechatPapaySupport.contractUnsign(null, externalNo, agreementNo, "用户主动解约");
		} catch (Exception e) {
			log.error("pay contract unsign failed channel={} externalNo={}", channel, externalNo, e);
			return false;
		}
	}

	/** 按协议扣款（每期自动续费） */
	public PayDeductResult contractPay(String channel, String agreementNo, String orderNo,
			BigDecimal amountYuan, String subject) {
		MemberPayChannelEnums ch = requireChannel(channel);
		try {
			if (ch == MemberPayChannelEnums.ALIPAY) {
				return alipaySupport.agreementPay(agreementNo, null, orderNo, amountYuan, subject);
			}
			return wechatPapaySupport.contractPay(orderNo, agreementNo, amountYuan, subject);
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("pay contract deduct failed channel={} orderNo={}", channel, orderNo, e);
			throw new BaseException("代扣失败", e);
		}
	}

	// 选择支付渠道
	private MemberPayChannelEnums requireChannel(String channel) {
		MemberPayChannelEnums ch = MemberPayChannelEnums.ofCode(channel);
		if (ch == null) {
			throw new BaseException("支付渠道无效");
		}
		if (ch == MemberPayChannelEnums.BANK) {
			throw new BaseException("银行卡渠道不支持在线退款/下单，请走线下");
		}
		return ch;
	}

	private String buildAttach(String orderNo) {
		return "ORDERNO:" + orderNo;
	}
}
