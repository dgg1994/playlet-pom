package com.playlet.oversea.service.impl;

import com.playlet.oversea.api.request.SensitiveRecordEntity;
import com.playlet.oversea.base.SensitiveCheckResult;
import com.playlet.oversea.dao.security.IllegalCommentRecordDao;
import com.playlet.oversea.entity.security.IllegalCommentRecordEntity;
import com.playlet.oversea.security.sensitive.SensitiveMatch;
import com.playlet.oversea.service.SensitiveRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * 违规评论记录落库。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SensitiveRecordServiceImpl implements SensitiveRecordService {

	@Resource
	private IllegalCommentRecordDao illegalCommentRecordDao;

	@Override
	public void saveRecord(SensitiveRecordEntity dto, SensitiveCheckResult result) {
		if (dto == null || result == null || Boolean.TRUE.equals(result.getPass())) {
			return;
		}
		try {
			IllegalCommentRecordEntity entity = new IllegalCommentRecordEntity();
			entity.setCommentId(dto.getCommentId());
			entity.setUserId(dto.getUserId());
			entity.setDramaId(dto.getDramaId());
			entity.setEpisodeId(dto.getEpisodeId());
			entity.setContent(dto.getContent());
			String words = result.getMatches() == null ? "" : result.getMatches().stream()
					.map(SensitiveMatch::getWord)
					.filter(w -> w != null && !w.isEmpty())
					.collect(Collectors.joining(","));
			entity.setSensitiveWords(words);
			entity.setRiskLevel(result.getLevel());
			entity.setStatus(0);
			entity.setSourceType(dto.getSourceType());
			illegalCommentRecordDao.insert(entity);
		} catch (Exception e) {
			log.warn("save illegal comment record failed: {}", e.getMessage());
		}
	}
}
