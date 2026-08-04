package com.playlet.internal.enums;

import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.Map;

/**
 * 极光推送模板（多语言）。
 * <p>
 * 语言码与 {@link LanguageEnums} 一致：zh-cn / en / zh-hk / tr-tr / ko-kr / ja-jp / bn-bd / pt-br。
 * 占位符使用 {@link MessageFormat} 风格：{0}、{1}…
 */
public enum PushTemplateEnums {

	INTERACT_TITLE(
			t("互动消息", "Interaction", "互動訊息", "Etkileşim", "알림", "インタラクション", "ইন্টারঅ্যাকশন", "Interação")),
	SOMEONE(
			t("有人", "Someone", "有人", "Birisi", "누군가", "誰か", "কেউ", "Alguém")),
	LIKE(
			t("{0} 赞了你", "{0} liked you", "{0} 讚了你", "{0} seni beğendi",
					"{0}님이 회원님을 좋아합니다", "{0}さんがあなたにいいねしました",
					"{0} আপনাকে লাইক করেছেন", "{0} curtiu você")),
	COMMENT(
			t("{0} 评论了你", "{0} commented on you", "{0} 評論了你", "{0} sana yorum yaptı",
					"{0}님이 회원님에게 댓글을 남겼습니다", "{0}さんがあなたにコメントしました",
					"{0} আপনাকে মন্তব্য করেছেন", "{0} comentou em você")),
	REPLY(
			t("{0} 回复了你", "{0} replied to you", "{0} 回覆了你", "{0} sana yanıt verdi",
					"{0}님이 회원님에게 답글을 남겼습니다", "{0}さんがあなたに返信しました",
					"{0} আপনাকে উত্তর দিয়েছেন", "{0} respondeu você")),
	INTERACT_DEFAULT(
			t("{0} 与你互动了", "{0} interacted with you", "{0} 與你互動了", "{0} seninle etkileşime geçti",
					"{0}님이 회원님과 상호작용했습니다", "{0}さんがあなたと交流しました",
					"{0} আপনার সাথে ইন্টারঅ্যাক্ট করেছেন", "{0} interagiu com você")),
	MEDAL_TITLE(
			t("勋章解锁", "Medal Unlocked", "勳章解鎖", "Madalya Açıldı",
					"메달 해제", "メダル解除", "পদক আনলক", "Medalha Desbloqueada")),
	MEDAL_UNLOCK(
			t("恭喜解锁勋章：{0}", "Unlocked medal: {0}", "恭喜解鎖勳章：{0}", "Madalya açıldı: {0}",
					"메달을 해제했습니다: {0}", "メダルを解除しました：{0}",
					"পদক আনলক হয়েছে: {0}", "Medalha desbloqueada: {0}"));

	private final Map<LanguageEnums, String> texts;

	PushTemplateEnums(Map<LanguageEnums, String> texts) {
		this.texts = texts;
	}

	/**
	 * 按语言取模板并格式化。未知语言回退 zh-cn。
	 */
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

	/** 仅取模板原文（不格式化） */
	public String text(String langue) {
		LanguageEnums lang = LanguageEnums.of(langue);
		String v = texts.get(lang);
		if (v != null) {
			return v;
		}
		v = texts.get(LanguageEnums.ZH_CN);
		return v == null ? name() : v;
	}

	/**
	 * 按 LanguageEnums 声明顺序填充：zh-cn, en, zh-hk, tr-tr, ko-kr, ja-jp, bn-bd, pt-br
	 */
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
