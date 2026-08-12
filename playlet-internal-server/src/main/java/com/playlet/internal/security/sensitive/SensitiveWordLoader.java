package com.playlet.internal.security.sensitive;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.playlet.internal.dao.security.SensitiveWordDao;
import com.playlet.internal.entity.security.SensitiveWordEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 从数据库加载敏感词，构建 DFA Trie。
 */
@Slf4j
@Component
public class SensitiveWordLoader {

	@Resource
	private SensitiveWordDao sensitiveWordDao;

	public SensitiveNode load() {
		SensitiveNode root = new SensitiveNode();
		List<SensitiveWordEntity> list = sensitiveWordDao.selectList(new QueryWrapper<>());
		int count = 0;
		if (list != null) {
			for (SensitiveWordEntity entity : list) {
				if (entity == null || !StringUtils.hasText(entity.getWord())) {
					continue;
				}
				insert(root, entity.getWord().trim());
				count++;
			}
		}
		log.info("sensitive word DFA loaded, size={}", count);
		return root;
	}

	private void insert(SensitiveNode root, String word) {
		SensitiveNode node = root;
		for (char c : word.toCharArray()) {
			node.getChildren().putIfAbsent(c, new SensitiveNode());
			node = node.getChildren().get(c);
		}
		node.setEnd(true);
		node.setWord(word);
		node.setLevel(1);
	}
}
