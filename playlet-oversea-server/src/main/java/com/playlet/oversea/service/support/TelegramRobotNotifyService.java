package com.playlet.oversea.service.support;

import com.playlet.oversea.dao.template.EmailTemplateDao;
import com.playlet.oversea.entity.template.EmailTemplateEntity;
import com.playlet.oversea.enums.LanguageEnums;
import com.playlet.oversea.enums.MessageEnums;
import com.playlet.oversea.utils.MessageFormatUtils;
import com.playlet.oversea.utils.StringUtils;
import com.playlet.oversea.utils.TelegramNotificationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 飞机消息模板发送（对齐 worldpay MessageTemplateServiceImpl.robotSend）。
 */
@Slf4j
@Service
public class TelegramRobotNotifyService {

	@Autowired
	private EmailTemplateDao emailTemplateDao;
	@Autowired
	private TelegramNotificationUtil telegramNotificationUtil;

	/**
	 * 异步发送飞机消息。
	 *
	 * @param code      模板编号（MessageEnums index）
	 * @param language  语言码，空则 zh-cn
	 * @param robotType 角色标记（暂不分流群组）
	 * @param arg       模板占位参数
	 */
	@Async("asyncExecutor")
	public void robotSendAsync(Integer code, String language, Integer robotType, Object... arg) {
		try {
			robotSend(code, language, robotType, arg);
		} catch (Exception e) {
			log.error("telegram robotSendAsync failed code={} robotType={}", code, robotType, e);
		}
	}

	/**
	 * 同步发送：查 email_template，缺省回退 MessageEnums 文案。
	 */
	public void robotSend(Integer code, String language, Integer robotType, Object... arg) {
		if (code == null) {
			log.warn("telegram robotSend skip empty code");
			return;
		}
		String lang = StringUtils.isEmpty(language) ? LanguageEnums.ZH_CN.getName() : language;
		try {
			String templateContent = resolveTemplateContent(code, lang);
			if (StringUtils.isEmpty(templateContent)) {
				log.warn("telegram robotSend template missing code={} language={}", code, lang);
				return;
			}
			String htmlContent = MessageFormatUtils.format(templateContent, arg);
			String html = MessageFormatUtils.saveTelegramHtml(htmlContent);
			telegramNotificationUtil.sendTelegramBindingMsg(html);
			log.info("telegram robotSend ok code={} robotType={}", code, robotType);
		} catch (Exception e) {
			log.error("telegram robotSend failed code={} language={}", code, lang, e);
		}
	}

	/** 优先 DB 模板，否则枚举默认文案 */
	private String resolveTemplateContent(Integer code, String language) {
		EmailTemplateEntity templateEntity = emailTemplateDao.findByNum(code, language);
		if (templateEntity != null && !StringUtils.isEmpty(templateEntity.getTemplateContent())) {
			return templateEntity.getTemplateContent();
		}
		// 兼容历史 zh / zh-cn
		if (LanguageEnums.ZH_CN.getName().equals(language)) {
			EmailTemplateEntity zh = emailTemplateDao.findByNum(code, "zh");
			if (zh != null && !StringUtils.isEmpty(zh.getTemplateContent())) {
				return zh.getTemplateContent();
			}
		}
		for (MessageEnums item : MessageEnums.values()) {
			if (code.equals(item.getIndex()) && matchesLang(item.getLanguage(), language)) {
				return item.getMsg();
			}
		}
		for (MessageEnums item : MessageEnums.values()) {
			if (code.equals(item.getIndex())) {
				return item.getMsg();
			}
		}
		return null;
	}

	private static boolean matchesLang(String enumLang, String language) {
		if (StringUtils.isEmpty(enumLang) || StringUtils.isEmpty(language)) {
			return false;
		}
		if (enumLang.equalsIgnoreCase(language)) {
			return true;
		}
		// MessageEnums 用 zh，配置/调用用 zh-cn
		boolean enumZh = "zh".equalsIgnoreCase(enumLang) || LanguageEnums.ZH_CN.getName().equalsIgnoreCase(enumLang);
		boolean callZh = LanguageEnums.ZH_CN.getName().equalsIgnoreCase(language) || "zh".equalsIgnoreCase(language);
		return enumZh && callZh;
	}
}
