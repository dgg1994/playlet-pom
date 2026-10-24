package com.playlet.internal.service.support;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.playlet.internal.constants.WatchPreferConstants;
import com.playlet.internal.dao.prefer.UserWatchPreferenceDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户看剧喜好 → 剧标签 groupId 解析（搜索/首页/推荐共用）。
 */
@Slf4j
@Component
public class WatchPreferMatchSupport {

	@Autowired
	private UserWatchPreferenceDao userWatchPreferenceDao;

	/**
	 * 解析用户偏好对应的剧标签 groupId；无登录/无偏好/无映射返回 null。
	 * 注意：勿返回 Collections.emptyList()——MyBatis OGNL 在 JDK16+ 反射调 size() 会 InaccessibleObjectException。
	 */
	public List<String> resolveDramaTagGroupIds(Integer uid) {
		if (uid == null || uid <= 0) {
			return null;
		}
		try {
			List<String> ids = userWatchPreferenceDao.findDramaTagGroupIdsByUid(uid,
					WatchPreferConstants.STATUS_ENABLED);
			if (CollectionUtils.isEmpty(ids)) {
				return null;
			}
			List<String> result = ids.stream()
					.filter(id -> id != null && !id.trim().isEmpty())
					.map(String::trim)
					.distinct()
					.collect(Collectors.toList());
			return result.isEmpty() ? null : result;
		} catch (Exception e) {
			log.warn("resolveDramaTagGroupIds failed uid={}: {}", uid, e.getMessage());
			return null;
		}
	}

	/**
	 * 是否有可用于加权的偏好标签。
	 */
	public boolean hasPreferTags(Integer uid) {
		return CollectionUtils.isNotEmpty(resolveDramaTagGroupIds(uid));
	}
}
