package com.playlet.internal.security.sensitive;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 敏感词 DFA 匹配与打码。
 */
@Component
public class SensitiveWordFilter {

	private volatile SensitiveNode root;

	public void init(SensitiveNode root) {
		this.root = root;
	}

	public List<SensitiveMatch> findWords(String text) {
		List<SensitiveMatch> result = new ArrayList<>();
		SensitiveNode currentRoot = root;
		if (text == null || text.isEmpty() || currentRoot == null) {
			return result;
		}
		Set<String> exist = new HashSet<>();
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			SensitiveNode node = currentRoot;
			for (int j = i; j < chars.length; j++) {
				node = node.getChildren().get(chars[j]);
				if (node == null) {
					break;
				}
				if (node.isEnd() && node.getWord() != null && !exist.contains(node.getWord())) {
					int level = node.getLevel() == null ? 1 : node.getLevel();
					result.add(new SensitiveMatch(node.getWord(), level));
					exist.add(node.getWord());
				}
			}
		}
		return result;
	}

	/**
	 * 将命中词替换为等长打码字符；长词优先，减少短词破坏长词。
	 */
	public String replace(String text, List<SensitiveMatch> matches, String replaceChar) {
		if (text == null || text.isEmpty() || matches == null || matches.isEmpty()) {
			return text;
		}
		char rc = (replaceChar == null || replaceChar.isEmpty()) ? '*' : replaceChar.charAt(0);
		List<SensitiveMatch> ordered = new ArrayList<>(matches);
		ordered.sort(Comparator.comparingInt((SensitiveMatch m) ->
				m.getWord() == null ? 0 : m.getWord().length()).reversed());
		String result = text;
		for (SensitiveMatch match : ordered) {
			String word = match.getWord();
			if (word == null || word.isEmpty()) {
				continue;
			}
			StringBuilder stars = new StringBuilder(word.length());
			for (int i = 0; i < word.length(); i++) {
				stars.append(rc);
			}
			result = result.replace(word, stars.toString());
		}
		return result;
	}
}
