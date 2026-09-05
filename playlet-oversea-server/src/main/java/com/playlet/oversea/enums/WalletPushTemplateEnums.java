package com.playlet.oversea.enums;

import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.Map;

/**
 * 钱包推送文案模板（多语言）。
 * 语言顺序：zh-cn / en / zh-hk / tr-tr / ko-kr / ja-jp / bn-bd / pt-br。
 */
public enum WalletPushTemplateEnums {

	COIN_TO_WALLET_SUCCESS_TITLE(
			t("金币提现到账", "Coin Withdraw Credited", "金幣提現到帳", "Coin çekimi yattı",
					"코인 출금 입금", "コイン出金入金", "কয়েন উত্তোলন জমা", "Saque em moedas creditado")),
	COIN_TO_WALLET_SUCCESS_BODY(
			t("已将 {0} 金币兑换并入账钱包 {1} {2}",
					"{0} coins converted and credited {1} {2}",
					"已將 {0} 金幣兌換並入賬錢包 {1} {2}",
					"{0} coin {1} {2} olarak cüzdana yatırıldı",
					"{0} 코인이 환전되어 지갑에 {1} {2} 입금되었습니다",
					"{0} コインを換金しウォレットに {1} {2} 入金しました",
					"{0} কয়েন রূপান্তর করে ওয়ালেটে {1} {2} জমা হয়েছে",
					"{0} moedas convertidas e creditadas {1} {2}")),

	USDT_TOPIN_SUCCESS_TITLE(
			t("USDT 充值到账", "USDT Deposit Credited", "USDT 儲值到帳", "USDT yatırımı yattı",
					"USDT 입금 완료", "USDT入金完了", "USDT জমা হয়েছে", "Depósito USDT creditado")),
	USDT_TOPIN_SUCCESS_BODY(
			t("钱包已入账 {0} USDT", "Wallet credited {0} USDT", "錢包已入賬 {0} USDT",
					"Cüzdana {0} USDT yatırıldı", "지갑에 {0} USDT가 입금되었습니다",
					"ウォレットに {0} USDT が入金されました", "ওয়ালেটে {0} USDT জমা হয়েছে",
					"Carteira creditada com {0} USDT")),

	CARD_RECHARGE_SUCCESS_TITLE(
			t("卡充值成功", "Card Top-up Success", "卡儲值成功", "Kart yükleme başarılı",
					"카드 충전 성공", "カードチャージ成功", "কার্ড টপ-আপ সফল", "Recarga do cartão concluída")),
	CARD_RECHARGE_SUCCESS_BODY(
			t("银行卡充值 {0} 已成功", "Card top-up of {0} succeeded", "銀行卡儲值 {0} 已成功",
					"{0} kart yüklemesi başarılı", "카드 충전 {0} 성공", "カードチャージ {0} 成功",
					"কার্ড টপ-আপ {0} সফল", "Recarga de {0} concluída")),
	CARD_RECHARGE_FAIL_TITLE(
			t("卡充值失败", "Card Top-up Failed", "卡儲值失敗", "Kart yükleme başarısız",
					"카드 충전 실패", "カードチャージ失敗", "কার্ড টপ-আপ ব্যর্থ", "Falha na recarga do cartão")),
	CARD_RECHARGE_FAIL_BODY(
			t("银行卡充值失败，金额已退回钱包", "Card top-up failed; amount refunded to wallet",
					"銀行卡儲值失敗，金額已退回錢包", "Kart yükleme başarısız; tutar cüzdana iade edildi",
					"카드 충전 실패, 금액이 지갑으로 환불되었습니다", "カードチャージ失敗、金額はウォレットに戻しました",
					"কার্ড টপ-আপ ব্যর্থ, অর্থ ওয়ালেটে ফেরত", "Recarga falhou; valor estornado à carteira")),

	TRANSFER_OUT_SUCCESS_TITLE(
			t("转账成功", "Transfer Sent", "轉賬成功", "Transfer gönderildi",
					"송금 완료", "送金完了", "ট্রান্সফার পাঠানো হয়েছে", "Transferência enviada")),
	TRANSFER_OUT_SUCCESS_BODY(
			t("已向 {0} 转出 {1}", "Sent {1} to {0}", "已向 {0} 轉出 {1}",
					"{0} hesabına {1} gönderildi", "{0}에게 {1} 송금했습니다",
					"{0} へ {1} を送金しました", "{0}-এ {1} পাঠানো হয়েছে", "Enviado {1} para {0}")),
	TRANSFER_IN_SUCCESS_TITLE(
			t("收到转账", "Transfer Received", "收到轉賬", "Transfer alındı",
					"입금 완료", "入金完了", "ট্রান্সফার প্রাপ্ত", "Transferência recebida")),
	TRANSFER_IN_SUCCESS_BODY(
			t("收到来自 {0} 的 {1}", "Received {1} from {0}", "收到來自 {0} 的 {1}",
					"{0} hesabından {1} alındı", "{0}로부터 {1} 입금",
					"{0} から {1} を受け取りました", "{0} থেকে {1} পেয়েছেন", "Recebido {1} de {0}")),

	KYC_PASS_TITLE(
			t("KYC 审核通过", "KYC Approved", "KYC 審核通過", "KYC onaylandı",
					"KYC 승인", "KYC承認", "KYC অনুমোদিত", "KYC aprovado")),
	KYC_PASS_BODY(
			t("身份认证已通过，可继续使用钱包服务", "Identity verified. You can continue using wallet services.",
					"身份認證已通過，可繼續使用錢包服務", "Kimlik doğrulandı. Cüzdan hizmetlerini kullanmaya devam edebilirsiniz.",
					"본인인증이 완료되었습니다. 지갑 서비스를 계속 이용하세요.", "本人確認が完了しました。ウォレットをご利用ください。",
					"পরিচয় যাচাই হয়েছে। ওয়ালেট ব্যবহার চালিয়ে যান।", "Identidade verificada. Continue usando a carteira.")),
	KYC_REJECT_TITLE(
			t("KYC 审核未通过", "KYC Rejected", "KYC 審核未通過", "KYC reddedildi",
					"KYC 거절", "KYC却下", "KYC প্রত্যাখ্যাত", "KYC rejeitado")),
	KYC_REJECT_BODY(
			t("身份认证未通过：{0}", "Identity verification failed: {0}", "身份認證未通過：{0}",
					"Kimlik doğrulama başarısız: {0}", "본인인증 실패: {0}", "本人確認に失敗しました：{0}",
					"পরিচয় যাচাই ব্যর্থ: {0}", "Falha na verificação: {0}")),

	CARD_OPEN_SUCCESS_TITLE(
			t("开卡成功", "Card Activated", "開卡成功", "Kart aktif",
					"카드 활성화", "カード有効化", "কার্ড সক্রিয়", "Cartão ativado")),
	CARD_OPEN_SUCCESS_BODY(
			t("您的银行卡已激活，可开始使用", "Your bank card is activated and ready to use.",
					"您的銀行卡已激活，可開始使用", "Bankanız aktif ve kullanıma hazır.",
					"카드가 활성화되어 사용할 수 있습니다.", "カードが有効化され、利用できます。",
					"আপনার কার্ড সক্রিয় এবং ব্যবহারযোগ্য।", "Seu cartão está ativado e pronto.")),
	CARD_OPEN_FAIL_TITLE(
			t("开卡失败", "Card Application Failed", "開卡失敗", "Kart başvurusu başarısız",
					"카드 신청 실패", "カード申請失敗", "কার্ড আবেদন ব্যর্থ", "Falha na solicitação do cartão")),
	CARD_OPEN_FAIL_BODY(
			t("开卡未成功，冻结费用已退回钱包", "Card application failed; frozen fees were refunded to wallet.",
					"開卡未成功，凍結費用已退回錢包", "Kart başvurusu başarısız; dondurulan ücret cüzdana iade edildi.",
					"카드 신청 실패, 동결 비용이 지갑으로 환불되었습니다.", "カード申請失敗、凍結費用はウォレットに戻しました。",
					"কার্ড আবেদন ব্যর্থ, জমা খরচ ওয়ালেটে ফেরত।", "Solicitação falhou; taxas congeladas estornadas.")),

	CARD_FREEZE_TITLE(
			t("卡片已冻结", "Card Frozen", "卡片已凍結", "Kart donduruldu",
					"카드 동결", "カード凍結", "কার্ড হিমায়িত", "Cartão congelado")),
	CARD_FREEZE_BODY(
			t("您的银行卡已冻结{0}", "Your bank card has been frozen{0}", "您的銀行卡已凍結{0}",
					"Bankanız donduruldu{0}", "카드가 동결되었습니다{0}", "カードが凍結されました{0}",
					"আপনার কার্ড হিমায়িত হয়েছে{0}", "Seu cartão foi congelado{0}")),
	CARD_UNFREEZE_TITLE(
			t("卡片已解冻", "Card Unfrozen", "卡片已解凍", "Kart çözüldü",
					"카드 해동", "カード解除", "কার্ড আনফ্রোজেন", "Cartão descongelado")),
	CARD_UNFREEZE_BODY(
			t("您的银行卡已解冻，可继续使用", "Your bank card has been unfrozen.",
					"您的銀行卡已解凍，可繼續使用", "Bankanızın dondurması kaldırıldı.",
					"카드 동결이 해제되었습니다.", "カードの凍結が解除されました。",
					"কার্ড আনফ্রোজেন হয়েছে।", "Seu cartão foi descongelado.")),
	CARD_CLOSE_TITLE(
			t("卡片已注销", "Card Closed", "卡片已註銷", "Kart kapatıldı",
					"카드 해지", "カード解約", "কার্ড বন্ধ", "Cartão encerrado")),
	CARD_CLOSE_BODY(
			t("您的银行卡已注销", "Your bank card has been closed.",
					"您的銀行卡已註銷", "Bankanız kapatıldı.",
					"카드가 해지되었습니다.", "カードが解約されました。",
					"আপনার কার্ড বন্ধ হয়েছে।", "Seu cartão foi encerrado.")),

	CARD_TXN_TITLE(
			t("卡交易通知", "Card Transaction", "卡交易通知", "Kart işlemi",
					"카드 거래", "カード取引", "কার্ড লেনদেন", "Transação do cartão")),
	CARD_TXN_BODY(
			t("{0}", "{0}", "{0}", "{0}", "{0}", "{0}", "{0}", "{0}")),

	CARD_3DS_TITLE(
			t("3DS 验证提醒", "3DS Verification", "3DS 驗證提醒", "3DS doğrulama",
					"3DS 인증", "3DS認証", "3DS যাচাই", "Verificação 3DS")),
	CARD_3DS_BODY(
			t("您有一笔交易需要 3DS 验证，请尽快完成", "A transaction requires 3DS verification.",
					"您有一筆交易需要 3DS 驗證，請儘快完成", "Bir işlem için 3DS doğrulaması gerekiyor.",
					"3DS 인증이 필요한 거래가 있습니다.", "3DS認証が必要な取引があります。",
					"একটি লেনদেনের জন্য 3DS যাচাই প্রয়োজন।", "Uma transação exige verificação 3DS.")),

	CARD_SHIPPING_TITLE(
			t("实体卡已发货", "Physical Card Shipped", "實體卡已出貨", "Fiziksel kart kargoda",
					"실물카드 발송", "リアルカード発送", "ফিজিক্যাল কার্ড পাঠানো হয়েছে", "Cartão físico enviado")),
	CARD_SHIPPING_BODY(
			t("物流单号：{0}", "Tracking number: {0}", "物流單號：{0}", "Kargo no: {0}",
					"운송장 번호: {0}", "追跡番号：{0}", "ট্র্যাকিং নম্বর: {0}", "Código de rastreio: {0}")),

	PAY_PASSWORD_BOUND_TITLE(
			t("支付密码已设置", "Payment Password Set", "支付密碼已設置", "Ödeme şifresi ayarlandı",
					"결제 비밀번호 설정", "支払いパスワード設定", "পেমেন্ট পাসওয়ার্ড সেট", "Senha de pagamento definida")),
	PAY_PASSWORD_BOUND_BODY(
			t("您已成功设置钱包支付密码，请妥善保管", "Payment password set successfully. Keep it safe.",
					"您已成功設置錢包支付密碼，請妥善保管", "Ödeme şifreniz ayarlandı. Güvende tutun.",
					"결제 비밀번호가 설정되었습니다. 안전하게 보관하세요.", "支払いパスワードを設定しました。大切に保管してください。",
					"পেমেন্ট পাসওয়ার্ড সেট হয়েছে। নিরাপদে রাখুন।", "Senha definida com sucesso. Guarde-a com cuidado."));

	private final Map<LanguageEnums, String> texts;

	WalletPushTemplateEnums(Map<LanguageEnums, String> texts) {
		this.texts = texts;
	}

	public String format(String langue, Object... args) {
		String pattern = text(langue);
		if (args == null || args.length == 0) {
			return pattern;
		}
		try {
			return MessageFormat.format(pattern, args);
		} catch (Exception e) {
			return pattern;
		}
	}

	public String text(String langue) {
		LanguageEnums lang = LanguageEnums.of(langue);
		String v = texts.get(lang);
		if (v != null) {
			return v;
		}
		v = texts.get(LanguageEnums.ZH_CN);
		return v == null ? name() : v;
	}

	private static Map<LanguageEnums, String> t(String zhCn, String en, String zhHk, String tr,
			String ko, String ja, String bn, String ptBr) {
		Map<LanguageEnums, String> map = new EnumMap<>(LanguageEnums.class);
		map.put(LanguageEnums.ZH_CN, zhCn);
		map.put(LanguageEnums.EN_US, en);
		map.put(LanguageEnums.ZH_TW, zhHk);
		map.put(LanguageEnums.TR, tr);
		map.put(LanguageEnums.KO, ko);
		map.put(LanguageEnums.JA, ja);
		map.put(LanguageEnums.BN, bn);
		map.put(LanguageEnums.PT_BR, ptBr);
		return map;
	}
}
