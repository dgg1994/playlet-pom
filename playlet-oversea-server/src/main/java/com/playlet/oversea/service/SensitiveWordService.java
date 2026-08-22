package com.playlet.oversea.service;

import com.playlet.oversea.base.SensitiveCheckResult;
import com.playlet.oversea.config.SensitiveWordProperties;
import com.playlet.oversea.security.sensitive.SensitiveAction;
import com.playlet.oversea.security.sensitive.SensitiveDecision;
import com.playlet.oversea.security.sensitive.SensitiveMatch;
import com.playlet.oversea.security.sensitive.SensitiveNode;
import com.playlet.oversea.security.sensitive.SensitiveWordFilter;
import com.playlet.oversea.security.sensitive.SensitiveWordLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 敏感词检测与分级处置。
 */
@Slf4j
@Service
public class SensitiveWordService {

	@Resource
	private SensitiveWordLoader loader;

	@Resource
	private SensitiveWordFilter filter;

	@Resource
	private SensitiveWordProperties properties;

	@PostConstruct
	public void init() {
		reload();
	}

	/**
	 * 定时热刷新词库（默认 30 分钟）。
	 */
	@Scheduled(fixedDelayString = "${sensitive.word.refresh-interval-ms:1800000}")
	public void scheduledReload() {
		reload();
	}

	public synchronized void reload() {
		try {
			SensitiveNode root = loader.load();
			filter.init(root);
		} catch (Exception e) {
			log.error("reload sensitive words failed", e);
		}
	}

	/**
	 * 原始检测（不做处置）。
	 */
	public SensitiveCheckResult check(String content) {
		SensitiveCheckResult result = new SensitiveCheckResult();
		if (!properties.isEnabled()) {
			result.setPass(true);
			result.setLevel(0);
			result.setMatches(Collections.emptyList());
			return result;
		}
		List<SensitiveMatch> matches = filter.findWords(content);
		result.setMatches(matches);
		if (matches == null || matches.isEmpty()) {
			result.setPass(true);
			result.setLevel(0);
			return result;
		}
		result.setPass(false);
		int maxLevel = 0;
		for (SensitiveMatch match : matches) {
			if (match.getLevel() != null && match.getLevel() > maxLevel) {
				maxLevel = match.getLevel();
			}
		}
		result.setLevel(maxLevel);
		return result;
	}

	/**
	 * 先检后处置：
	 * level≥3 REJECT；level=2 HIDE；level=1 MASK（可打码）；通过 PASS。
	 */
	public SensitiveDecision decide(String content) {
		String text = content == null ? "" : content;
		SensitiveCheckResult check = check(text);
		SensitiveDecision decision = new SensitiveDecision();
		decision.setCheck(check);
		decision.setContent(text);
		if (Boolean.TRUE.equals(check.getPass())) {
			decision.setAction(SensitiveAction.PASS);
			return decision;
		}
		int level = check.getLevel() == null ? 1 : check.getLevel();
		if (level >= 3) {
			decision.setAction(SensitiveAction.REJECT);
			return decision;
		}
		if (level == 2) {
			decision.setAction(SensitiveAction.HIDE);
			return decision;
		}
		decision.setAction(SensitiveAction.MASK);
		if (properties.isReplaceEnabled()) {
			decision.setContent(filter.replace(text, check.getMatches(), properties.getReplaceChar()));
		}
		return decision;
	}
}
