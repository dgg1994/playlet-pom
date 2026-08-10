package com.playlet.oversea.utils;

import com.alibaba.fastjson.JSON;
import com.playlet.oversea.api.response.TheaterHomeRespEntity;
import com.playlet.oversea.constants.RedisKeyConstants;
import com.playlet.oversea.enums.LanguageEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 剧场首页整页缓存：按语言存最终响应；失败降级不影响主流程。
 */
@Slf4j
@Component
public class TheaterHomeCacheHelper {

	@Autowired
	private RedisUtil redisUtil;

	public TheaterHomeRespEntity get(String langue) {
		try {
			Object raw = redisUtil.get(cacheKey(langue));
			if (raw == null) {
				return null;
			}
			if (raw instanceof TheaterHomeRespEntity) {
				return (TheaterHomeRespEntity) raw;
			}
			String json = raw instanceof String ? (String) raw : JSON.toJSONString(raw);
			return JSON.parseObject(json, TheaterHomeRespEntity.class);
		} catch (Exception e) {
			log.warn("theater home cache get failed langue={}: {}", langue, e.getMessage());
			return null;
		}
	}

	public void put(String langue, TheaterHomeRespEntity resp) {
		if (resp == null) {
			return;
		}
		try {
			redisUtil.set(cacheKey(langue), JSON.toJSONString(resp), RedisKeyConstants.THEATER_HOME_TTL_SEC);
		} catch (Exception e) {
			log.warn("theater home cache put failed langue={}: {}", langue, e.getMessage());
		}
	}

	/** 清掉所有语言的首页缓存 */
	public void invalidateAll() {
		try {
			List<String> langues = LanguageEnums.getAll();
			if (langues == null || langues.isEmpty()) {
				return;
			}
			String[] keys = new String[langues.size()];
			for (int i = 0; i < langues.size(); i++) {
				keys[i] = cacheKey(langues.get(i));
			}
			redisUtil.del(keys);
		} catch (Exception e) {
			log.warn("theater home cache invalidate failed: {}", e.getMessage());
		}
	}

	private static String cacheKey(String langue) {
		String lang = StringUtils.isEmpty(langue) ? LanguageEnums.DEFAULT_LANGUE : langue.trim();
		return RedisKeyConstants.THEATER_HOME_KEY + lang;
	}
}
