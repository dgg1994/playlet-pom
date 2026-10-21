package com.playlet.internal.security.sensitive;

import com.playlet.internal.dao.security.SensitiveWordDao;
import com.playlet.internal.entity.security.SensitiveWordEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 从数据库加载「已生效」敏感词，构建 DFA Trie。
 */
@Slf4j
@Component
public class SensitiveWordLoader {

	@Resource
	private SensitiveWordDao sensitiveWordDao;

	public SensitiveNode load() {
		SensitiveNode root = new SensitiveNode();
		// 仅 status=已生效 进生产词库，待审/驳回/禁用不参与过滤
		List<SensitiveWordEntity> list = sensitiveWordDao.findActiveList();
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
