package com.playlet.oversea.service.support;

import com.playlet.oversea.api.request.JpushReqEntity;
import com.playlet.oversea.constants.PushConstants;
import com.playlet.oversea.constants.WalletNotifyConstants;
import com.playlet.oversea.dao.account.AppAccountDao;
import com.playlet.oversea.entity.account.AppAccountEntity;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.enums.LanguageEnums;
import com.playlet.oversea.enums.SystemMessageTypeEnums;
import com.playlet.oversea.enums.WalletNotifyEventEnums;
import com.playlet.oversea.enums.WalletPushTemplateEnums;
import com.playlet.oversea.enums.WithdrawUserTypeEnums;
import com.playlet.oversea.service.CreatorSystemMessageSendService;
import com.playlet.oversea.service.SystemMessageSendService;
import com.playlet.oversea.utils.JPushUtils;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 钱包通知：落系统消息并极光推送。
 */
@Slf4j
@Service
public class WalletNotifyServiceImpl implements WalletNotifyService {

	@Autowired
	private SystemMessageSendService systemMessageSendService;
	@Autowired
	private CreatorSystemMessageSendService creatorSystemMessageSendService;
	@Autowired
	private AppAccountDao appAccountDao;

	@Override
	public void notify(WalletUserEntity user, WalletNotifyEventEnums event,
			String bizId, String jumpType, String jumpParam, Object... contentArgs) {
		if (user == null) {
			return;
		}
		notify(user.getUserType(), user.getLocalUid(), event, bizId, jumpType, jumpParam, contentArgs);
	}

	@Override
	public void notify(Integer userType, Integer localUid, WalletNotifyEventEnums event,
			String bizId, String jumpType, String jumpParam, Object... contentArgs) {
		if (localUid == null || event == null || StringUtils.isEmpty(bizId)) {
			return;
		}
		try {
			String langue = resolveLangue(userType, localUid);
			String title = resolveTitle(event, langue);
			String content = resolveContent(event, langue, contentArgs);
			String jump = StringUtils.isEmpty(jumpType) ? event.getDefaultJumpType() : jumpType;
			WithdrawUserTypeEnums type = WithdrawUserTypeEnums.fromCode(userType);
			if (WithdrawUserTypeEnums.CREATOR.equals(type)) {
				sendCreator(localUid, langue, title, content, bizId, jump, jumpParam);
			} else {
				sendApp(localUid, langue, title, content, bizId, jump, jumpParam);
			}
		} catch (Exception e) {
			// 通知失败不影响主业务
			log.error("wallet notify failed userType={} localUid={} event={} bizId={}",
					userType, localUid, event, bizId, e);
		}
	}

	private void sendApp(Integer uid, String langue, String title, String content,
			String bizId, String jumpType, String jumpParam) {
		boolean ok = systemMessageSendService.sendToUser(uid, SystemMessageTypeEnums.WALLET.getCode(),
				langue, title, content, null, null, bizId, jumpType, jumpParam, true);
		log.info("wallet notify app uid={} bizId={} ok={}", uid, bizId, ok);
	}

	private void sendCreator(Integer creatorId, String langue, String title, String content,
			String bizId, String jumpType, String jumpParam) {
		boolean ok = creatorSystemMessageSendService.sendToCreator(creatorId,
				WalletNotifyConstants.MESSAGE_TYPE, langue, title, content, null, null, null,
				bizId, jumpType, jumpParam);
		log.info("wallet notify creator creatorId={} bizId={} ok={}", creatorId, bizId, ok);
		// 作家站内信落库后尽力极光（alias=creatorId；未绑定则静默失败）
		if (ok) {
			pushCreatorBestEffort(creatorId, title, content, jumpType, jumpParam);
		}
	}

	private void pushCreatorBestEffort(Integer creatorId, String title, String content,
			String jumpType, String jumpParam) {
		try {
			Map<String, Object> extras = new HashMap<>();
			extras.put("bizType", PushConstants.BIZ_SYSTEM);
			extras.put("messageType", WalletNotifyConstants.MESSAGE_TYPE);
			if (!StringUtils.isEmpty(jumpType)) {
				extras.put("jumpType", jumpType);
			}
			if (!StringUtils.isEmpty(jumpParam)) {
				extras.put("jumpParam", jumpParam);
			}
			String body = content == null ? "" : content.trim();
			if (body.length() > 120) {
				body = body.substring(0, 120) + "...";
			}
			JpushReqEntity pushVo = new JpushReqEntity();
			pushVo.setTitle(title == null ? "" : title);
			pushVo.setMsg(body);
			pushVo.setBroadcasting(false);
			pushVo.setAliasList(Collections.singletonList(String.valueOf(creatorId)));
			pushVo.setExtrasMap(extras);
			JPushUtils.sendAsync(pushVo);
		} catch (Exception e) {
			log.warn("wallet creator jpush best-effort failed creatorId={}: {}", creatorId, e.getMessage());
		}
	}

	private String resolveLangue(Integer userType, Integer localUid) {
		if (WithdrawUserTypeEnums.APP.equals(WithdrawUserTypeEnums.fromCode(userType))) {
			AppAccountEntity account = appAccountDao.findByUid(localUid);
			if (account != null && !StringUtils.isEmpty(account.getPushLangue())) {
				return LanguageEnums.of(account.getPushLangue()).getName();
			}
		}
		return LanguageEnums.DEFAULT_LANGUE;
	}

	private static String resolveTitle(WalletNotifyEventEnums event, String langue) {
		WalletPushTemplateEnums tpl = titleTemplate(event);
		return tpl == null ? event.name() : tpl.format(langue);
	}

	private static String resolveContent(WalletNotifyEventEnums event, String langue, Object... args) {
		WalletPushTemplateEnums tpl = bodyTemplate(event);
		return tpl == null ? event.name() : tpl.format(langue, args == null ? new Object[0] : args);
	}

	private static WalletPushTemplateEnums titleTemplate(WalletNotifyEventEnums event) {
		switch (event) {
			case COIN_TO_WALLET_SUCCESS:
				return WalletPushTemplateEnums.COIN_TO_WALLET_SUCCESS_TITLE;
			case USDT_TOPIN_SUCCESS:
				return WalletPushTemplateEnums.USDT_TOPIN_SUCCESS_TITLE;
			case CARD_RECHARGE_SUCCESS:
				return WalletPushTemplateEnums.CARD_RECHARGE_SUCCESS_TITLE;
			case CARD_RECHARGE_FAIL:
				return WalletPushTemplateEnums.CARD_RECHARGE_FAIL_TITLE;
			case TRANSFER_OUT_SUCCESS:
				return WalletPushTemplateEnums.TRANSFER_OUT_SUCCESS_TITLE;
			case TRANSFER_IN_SUCCESS:
				return WalletPushTemplateEnums.TRANSFER_IN_SUCCESS_TITLE;
			case KYC_PASS:
				return WalletPushTemplateEnums.KYC_PASS_TITLE;
			case KYC_REJECT:
				return WalletPushTemplateEnums.KYC_REJECT_TITLE;
			case CARD_OPEN_SUCCESS:
				return WalletPushTemplateEnums.CARD_OPEN_SUCCESS_TITLE;
			case CARD_OPEN_FAIL:
				return WalletPushTemplateEnums.CARD_OPEN_FAIL_TITLE;
			case CARD_FREEZE:
				return WalletPushTemplateEnums.CARD_FREEZE_TITLE;
			case CARD_UNFREEZE:
				return WalletPushTemplateEnums.CARD_UNFREEZE_TITLE;
			case CARD_CLOSE:
				return WalletPushTemplateEnums.CARD_CLOSE_TITLE;
			case CARD_TXN:
				return WalletPushTemplateEnums.CARD_TXN_TITLE;
			case CARD_3DS:
				return WalletPushTemplateEnums.CARD_3DS_TITLE;
			case CARD_SHIPPING:
				return WalletPushTemplateEnums.CARD_SHIPPING_TITLE;
			case PAY_PASSWORD_BOUND:
				return WalletPushTemplateEnums.PAY_PASSWORD_BOUND_TITLE;
			default:
				return null;
		}
	}

	private static WalletPushTemplateEnums bodyTemplate(WalletNotifyEventEnums event) {
		switch (event) {
			case COIN_TO_WALLET_SUCCESS:
				return WalletPushTemplateEnums.COIN_TO_WALLET_SUCCESS_BODY;
			case USDT_TOPIN_SUCCESS:
				return WalletPushTemplateEnums.USDT_TOPIN_SUCCESS_BODY;
			case CARD_RECHARGE_SUCCESS:
				return WalletPushTemplateEnums.CARD_RECHARGE_SUCCESS_BODY;
			case CARD_RECHARGE_FAIL:
				return WalletPushTemplateEnums.CARD_RECHARGE_FAIL_BODY;
			case TRANSFER_OUT_SUCCESS:
				return WalletPushTemplateEnums.TRANSFER_OUT_SUCCESS_BODY;
			case TRANSFER_IN_SUCCESS:
				return WalletPushTemplateEnums.TRANSFER_IN_SUCCESS_BODY;
			case KYC_PASS:
				return WalletPushTemplateEnums.KYC_PASS_BODY;
			case KYC_REJECT:
				return WalletPushTemplateEnums.KYC_REJECT_BODY;
			case CARD_OPEN_SUCCESS:
				return WalletPushTemplateEnums.CARD_OPEN_SUCCESS_BODY;
			case CARD_OPEN_FAIL:
				return WalletPushTemplateEnums.CARD_OPEN_FAIL_BODY;
			case CARD_FREEZE:
				return WalletPushTemplateEnums.CARD_FREEZE_BODY;
			case CARD_UNFREEZE:
				return WalletPushTemplateEnums.CARD_UNFREEZE_BODY;
			case CARD_CLOSE:
				return WalletPushTemplateEnums.CARD_CLOSE_BODY;
			case CARD_TXN:
				return WalletPushTemplateEnums.CARD_TXN_BODY;
			case CARD_3DS:
				return WalletPushTemplateEnums.CARD_3DS_BODY;
			case CARD_SHIPPING:
				return WalletPushTemplateEnums.CARD_SHIPPING_BODY;
			case PAY_PASSWORD_BOUND:
				return WalletPushTemplateEnums.PAY_PASSWORD_BOUND_BODY;
			default:
				return null;
		}
	}
}
