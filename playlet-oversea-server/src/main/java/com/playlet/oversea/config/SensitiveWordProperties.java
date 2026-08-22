package com.playlet.oversea.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SensitiveWordProperties {

	@Value("${sensitive.word.enabled:true}")
	private boolean enabled;

	@Value("${sensitive.word.refresh-minutes:30}")
	private long refreshMinutes;

	@Value("${sensitive.word.replace-enabled:true}")
	private boolean replaceEnabled;

	@Value("${sensitive.word.replace-char:*}")
	private String replaceChar;
}
