package com.playlet.internal.service.creator.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.CallCardRespEntity;
import com.playlet.internal.api.response.CallDetailRespEntity;
import com.playlet.internal.api.response.CallPublishRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.CallSquareConstants;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.creator.CreatorCallDao;
import com.playlet.internal.dao.creator.CreatorCallJoinDao;
import com.playlet.internal.entity.creator.CreatorCallEntity;
import com.playlet.internal.entity.creator.CreatorCallJoinEntity;
import com.playlet.internal.query.creator.CallSquareListQuery;
import com.playlet.internal.service.creator.CallSquareService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 征稿广场实现：列表 + 详情 + 参与。
 */
@Slf4j
@RestController
@CrossOrigin
public class CallSquareServiceImpl extends BaseApiService implements CallSquareService {

	private static final String DATE_PATTERN = "yyyy-MM-dd";

	@Resource
	private CreatorCallDao creatorCallDao;
	@Resource
	private CreatorCallJoinDao creatorCallJoinDao;

	@Override
	public ResponseBase list(CallSquareListQuery query, HttpServletRequest request) {
		try {
			if (query == null) {
				query = new CallSquareListQuery();
			}
			normalizeListQuery(query);
			Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
			Set<Integer> joinedIds = loadJoinedIds(creatorId);

			PageHelper.startPage(query.getPageNumber(), query.getPageSize());
			List<CreatorCallEntity> list = creatorCallDao.findSquareList(query);
			List<CallCardRespEntity> cards = new ArrayList<>();
			if (list != null) {
				for (CreatorCallEntity entity : list) {
					cards.add(toCard(entity, joinedIds));
				}
			}
			PageInfo<CreatorCallEntity> pageInfo = new PageInfo<>(list);
			PageInfo<CallCardRespEntity> respPage = new PageInfo<>();
			respPage.setTotal(pageInfo.getTotal());
			respPage.setPageNum(pageInfo.getPageNum());
			respPage.setPageSize(pageInfo.getPageSize());
			respPage.setPages(pageInfo.getPages());
			respPage.setList(cards);
			return setResultSuccess(respPage);
		} catch (Exception e) {
			log.error("call square list error", e);
			return setResultError(Constants.HTTP_RES_CODE_500, I18nUtil.getMessage("base_error"));
		}
	}

	@Override
	public ResponseBase detail(Integer id, HttpServletRequest request) {
		try {
			CreatorCallEntity entity = creatorCallDao.findVisibleById(id);
			if (entity == null) {
				return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
			}
			Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
			CallDetailRespEntity resp = toDetail(entity);
			resp.setJoined(creatorId != null && creatorCallJoinDao.countJoin(id, creatorId) > 0);
			resp.setRelated(buildRelated(entity));
			return setResultSuccess(resp);
		} catch (Exception e) {
			log.error("call square detail error id={}", id, e);
			return setResultError(Constants.HTTP_RES_CODE_500, I18nUtil.getMessage("base_error"));
		}
	}

	@Override
	public ResponseBase join(Integer id, HttpServletRequest request) {
		try {
			Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
			if (creatorId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
			}
			CreatorCallEntity entity = creatorCallDao.findVisibleById(id);
			if (entity == null) {
				return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
			}
			if (statusOf(entity) == CallSquareConstants.STATUS_ENDED) {
				return setResultError(Constants.HTTP_RES_CODE_500, CallSquareConstants.ERROR_ENDED_JOIN);
			}
			// 重复参与幂等处理，不重复累计人数
			if (creatorCallJoinDao.countJoin(id, creatorId) > 0) {
				return setResultSuccess(true);
			}
			CreatorCallJoinEntity join = new CreatorCallJoinEntity();
			join.setCallId(id);
			join.setCreatorId(creatorId);
			creatorCallJoinDao.insert(join);
			creatorCallDao.update(null, Wrappers.<CreatorCallEntity>lambdaUpdate()
					.eq(CreatorCallEntity::getId, id)
					.setSql("participants = participants + 1"));
			return setResultSuccess(true);
		} catch (Exception e) {
			log.error("call square join error id={}", id, e);
			return setResultError(Constants.HTTP_RES_CODE_500, I18nUtil.getMessage("base_error"));
		}
	}

	/**
	 * 发布征稿：需登录；必填字段校验；JSON 字段二次序列化；写 publisher 与当前日期。
	 * 新发布默认下架（shelf_status=0），等审核通过后再上架，避免脏数据污染列表。
	 */
	@Override
	public ResponseBase publish(CreatorCallEntity body, HttpServletRequest request) {
		try {
			Integer creatorId = CreatorTokenUtil.resolveCreatorId(request);
			if (creatorId == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
			}
			if (body == null) {
				return setResultError(Constants.HTTP_RES_CODE_400, "请求体不能为空");
			}

			List<String> missing = new ArrayList<>();
			if (isBlank(body.getTitle())) {
				missing.add("标题");
			}
			if (isBlank(body.getTopic())) {
				missing.add("题材");
			}
			if (body.getAmount() == null || body.getAmount() <= 0) {
				missing.add("总奖金");
			}
			if (body.getNeedCount() == null || body.getNeedCount() <= 0) {
				missing.add("需求稿件数");
			}
			if (body.getDeadline() == null) {
				missing.add("截止日期");
			}
			if (body.getExclusive() == null) {
				missing.add("授权方式");
			}
			if (isBlank(body.getIntro())) {
				missing.add("卡片摘要");
			}
			if (!missing.isEmpty()) {
				return setResultError(Constants.HTTP_RES_CODE_400, "请填写：" + String.join("、", missing));
			}

			CreatorCallEntity entity = new CreatorCallEntity();
			entity.setPublisherId(creatorId);
			// publisher 优先使用请求体传入值（前端可传"笔名/工作室名"），否则兜底为默认昵称
			entity.setPublisher(isBlank(body.getPublisher())
					? CallSquareConstants.DEFAULT_PUBLISHER : body.getPublisher().trim());
			entity.setTitle(body.getTitle().trim());
			entity.setTopic(body.getTopic().trim());
			entity.setExclusive(body.getExclusive() == 1 ? 1 : 0);
			entity.setAmount(body.getAmount());
			entity.setNeedCount(body.getNeedCount());
			entity.setDeadline(body.getDeadline());
			entity.setPublishAt(new java.util.Date());
			entity.setTags(trimToJson(body.getTags()));
			entity.setIntro(body.getIntro().trim());
			entity.setBenchmark(safeTrim(body.getBenchmark()));
			entity.setDurationPerEpisode(body.getDurationPerEpisode());
			entity.setEpisodes(body.getEpisodes());
			entity.setContentRequirement(safeTrim(body.getContentRequirement()));
			entity.setLeadCast(safeTrim(body.getLeadCast()));
			entity.setSupportCast(safeTrim(body.getSupportCast()));
			entity.setBenchmarkNote(safeTrim(body.getBenchmarkNote()));
			entity.setExtraNote(safeTrim(body.getExtraNote()));
			entity.setSelectionDays(safeTrim(body.getSelectionDays()));
			entity.setSettle(safeTrim(body.getSettle()));
			entity.setAttachments(trimToJson(body.getAttachments()));
			entity.setSubmissions(CallSquareConstants.EMPTY_JSON_ARRAY);
			entity.setSortWeight(CallSquareConstants.DEFAULT_SORT_WEIGHT);
			// participants / shelf_status / delete_state 未显式赋值，依赖 DB DEFAULT 0
			creatorCallDao.insert(entity);

			CallPublishRespEntity resp = new CallPublishRespEntity();
			resp.setId(entity.getId());
			resp.setMsg(CallSquareConstants.PUBLISH_SUCCESS_MSG);
			return setResultSuccess(resp);
		} catch (Exception e) {
			log.error("call square publish error", e);
			return setResultError(Constants.HTTP_RES_CODE_500, I18nUtil.getMessage("base_error"));
		}
	}

	/** 入参规范化：空串/全部 视为不筛，默认排序兜底 */
	private void normalizeListQuery(CallSquareListQuery query) {
		query.setKeyword(CallSquareConstants.normalizeText(query.getKeyword()));
		query.setTopic(CallSquareConstants.normalizeText(query.getTopic()));
		query.setDeadlineStart(CallSquareConstants.normalizeText(query.getDeadlineStart()));
		query.setDeadlineEnd(CallSquareConstants.normalizeText(query.getDeadlineEnd()));
		if (query.getStatus() == null || query.getStatus() < CallSquareConstants.STATUS_OPEN
				|| query.getStatus() > CallSquareConstants.STATUS_ENDED) {
			query.setStatus(0);
		}
		if (query.getSortType() == null || query.getSortType() < CallSquareConstants.SORT_COMPREHENSIVE
				|| query.getSortType() > CallSquareConstants.SORT_DEADLINE) {
			query.setSortType(CallSquareConstants.SORT_COMPREHENSIVE);
		}
	}

	/** 当前创作者已参与的征稿 id 集合 */
	private Set<Integer> loadJoinedIds(Integer creatorId) {
		Set<Integer> joinedIds = new HashSet<>();
		if (creatorId == null) {
			return joinedIds;
		}
		List<Integer> ids = creatorCallJoinDao.findJoinedCallIds(creatorId);
		if (ids != null) {
			joinedIds.addAll(ids);
		}
		return joinedIds;
	}

	private CallCardRespEntity toCard(CreatorCallEntity entity, Set<Integer> joinedIds) {
		CallCardRespEntity card = new CallCardRespEntity();
		card.setId(entity.getId());
		card.setTitle(entity.getTitle());
		card.setTopic(entity.getTopic());
		card.setExclusive(entity.getExclusive() != null && entity.getExclusive() == 1);
		card.setAmount(entity.getAmount());
		card.setParticipants(entity.getParticipants());
		card.setNeed(entity.getNeedCount());
		card.setDeadline(formatDate(entity.getDeadline()));
		card.setCreatedAt(formatDate(entity.getPublishAt()));
		card.setTags(parseStrArray(entity.getTags()));
		card.setDesc(entity.getIntro());
		card.setBenchmark(entity.getBenchmark());
		card.setDurationPerEpisode(entity.getDurationPerEpisode());
		card.setEpisodes(entity.getEpisodes());
		card.setStatus(CallSquareConstants.statusLabelOf(statusOf(entity)));
		card.setJoined(joinedIds.contains(entity.getId()));
		return card;
	}

	private CallDetailRespEntity toDetail(CreatorCallEntity entity) {
		CallDetailRespEntity resp = new CallDetailRespEntity();
		int status = statusOf(entity);
		resp.setId(entity.getId());
		resp.setTitle(entity.getTitle());
		resp.setTopic(entity.getTopic());
		resp.setExclusive(entity.getExclusive() != null && entity.getExclusive() == 1);
		resp.setAmount(entity.getAmount());
		resp.setParticipants(entity.getParticipants());
		resp.setNeed(entity.getNeedCount());
		resp.setDeadline(formatDate(entity.getDeadline()));
		resp.setCreatedAt(formatDate(entity.getPublishAt()));
		resp.setTags(parseStrArray(entity.getTags()));
		resp.setDesc(entity.getIntro());
		resp.setBenchmark(entity.getBenchmark());
		resp.setDurationPerEpisode(entity.getDurationPerEpisode());
		resp.setEpisodes(entity.getEpisodes());
		resp.setStatus(CallSquareConstants.statusLabelOf(status));
		resp.setDaysLeft((int) Math.max(0, daysLeftOf(entity)));

		resp.setPublisher(entity.getPublisher());
		resp.setPublishAt(formatDate(entity.getPublishAt()));
		resp.setSelectionDays(entity.getSelectionDays());
		resp.setSettle(entity.getSettle());
		resp.setLicenseMode(CallSquareConstants.licenseModeOf(entity.getExclusive()));
		resp.setLicenseNote(CallSquareConstants.licenseNoteOf(entity.getExclusive()));
		resp.setWordCount(buildWordCount(entity));
		resp.setEpisodeFormat(buildEpisodeFormat(entity));
		resp.setContentRequirement(entity.getContentRequirement());
		resp.setLeadCast(entity.getLeadCast());
		resp.setSupportCast(entity.getSupportCast());
		resp.setBenchmarkNote(entity.getBenchmarkNote());
		resp.setExtraNote(entity.getExtraNote());
		resp.setAttachments(parseMapArray(entity.getAttachments()));
		resp.setSubmissions(parseMapArray(entity.getSubmissions()));
		return resp;
	}

	/** 相关征稿：同题材优先，最多 3 条 */
	private List<Map<String, Object>> buildRelated(CreatorCallEntity entity) {
		List<Map<String, Object>> related = new ArrayList<>();
		List<CreatorCallEntity> list = creatorCallDao.findRelated(entity.getId(), entity.getTopic());
		if (list == null) {
			return related;
		}
		for (CreatorCallEntity item : list) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", item.getId());
			row.put("title", item.getTitle());
			row.put("need", item.getNeedCount());
			row.put("amount", item.getAmount());
			row.put("deadlineText", formatDeadlineText(item.getDeadline()));
			related.add(row);
		}
		return related;
	}

	/** 状态推导：已结束 / 即将截止（15 天内）/ 征稿中 */
	private int statusOf(CreatorCallEntity entity) {
		long days = daysLeftOf(entity);
		if (days < 0) {
			return CallSquareConstants.STATUS_ENDED;
		}
		return days <= CallSquareConstants.SOON_DAYS ? CallSquareConstants.STATUS_SOON : CallSquareConstants.STATUS_OPEN;
	}

	/** 剩余天数，已结束返回负数 */
	private long daysLeftOf(CreatorCallEntity entity) {
		if (entity.getDeadline() == null) {
			return 0;
		}
		LocalDate deadline = entity.getDeadline().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
	}

	/** 征稿字数说明：单集约 1200-1500 字 */
	private String buildWordCount(CreatorCallEntity entity) {
		int episodes = entity.getEpisodes() == null ? 0 : entity.getEpisodes();
		return "单集约 1200-1500 字，全本约 "
				+ String.format("%.1f", episodes * 1200 / 10000.0) + "-"
				+ String.format("%.1f", episodes * 1500 / 10000.0) + " 万字";
	}

	/** 单集时长与集数说明 */
	private String buildEpisodeFormat(CreatorCallEntity entity) {
		BigDecimal duration = entity.getDurationPerEpisode();
		int episodes = entity.getEpisodes() == null ? 0 : entity.getEpisodes();
		return "约 " + (duration == null ? "0" : duration.stripTrailingZeros().toPlainString())
				+ " 分钟/集，共 " + episodes + " 集";
	}

	private String formatDeadlineText(java.util.Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat monthDay = new SimpleDateFormat("MM月dd日");
		return monthDay.format(date);
	}

	private String formatDate(java.util.Date date) {
		if (date == null) {
			return "";
		}
		return new SimpleDateFormat(DATE_PATTERN).format(date);
	}

	/** 解析 JSON 字符串数组，异常/为空返回空列表 */
	private List<String> parseStrArray(String json) {
		List<String> result = new ArrayList<>();
		if (json == null || json.trim().isEmpty()) {
			return result;
		}
		try {
			List<String> parsed = JSON.parseArray(json, String.class);
			if (parsed != null) {
				result.addAll(parsed);
			}
		} catch (Exception e) {
			log.warn("parse json array fail: {}", json);
		}
		return result;
	}

	/** 解析 JSON 对象数组，异常/为空返回空列表 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> parseMapArray(String json) {
		List<Map<String, Object>> result = new ArrayList<>();
		if (json == null || json.trim().isEmpty()) {
			return result;
		}
		try {
			List<Map<String, Object>> parsed = (List<Map<String, Object>>) (List<?>) JSON.parseArray(json, Map.class);
			if (parsed != null) {
				result.addAll(parsed);
			}
		} catch (Exception e) {
			log.warn("parse json map array fail: {}", json);
		}
		return result;
	}

	/** 判空（null 或全空白） */
	private boolean isBlank(String text) {
		return text == null || text.trim().isEmpty();
	}

	/** 安全 trim，null 时返回 null */
	private String safeTrim(String text) {
		return text == null ? null : text.trim();
	}

	/**
	 * 兼容三种输入，统一序列化为 JSON 数组字符串：
	 *   - 前端传 JSON 字符串（旧方式）→ 校验合法性后原样保留，非法返回 EMPTY_JSON_ARRAY
	 *   - 前端传 List（Jackson 直接把数组字段反序列化成 List）→ 序列化为 JSON
	 *   - null / 空串 → EMPTY_JSON_ARRAY
	 */
	@SuppressWarnings("unchecked")
	private String trimToJson(Object raw) {
		if (raw == null) {
			return CallSquareConstants.EMPTY_JSON_ARRAY;
		}
		if (raw instanceof String) {
			String s = ((String) raw).trim();
			if (s.isEmpty()) {
				return CallSquareConstants.EMPTY_JSON_ARRAY;
			}
			try {
				JSON.parseArray(s);
				return s;
			} catch (Exception e) {
				return CallSquareConstants.EMPTY_JSON_ARRAY;
			}
		}
		if (raw instanceof List) {
			return JSON.toJSONString(raw);
		}
		return CallSquareConstants.EMPTY_JSON_ARRAY;
	}
}
