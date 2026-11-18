package com.playlet.internal.service.creator.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.TalentMarketCardRespEntity;
import com.playlet.internal.api.response.TalentMarketDetailRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.TalentConstants;
import com.playlet.internal.dao.creator.CreatorMarketInfoDao;
import com.playlet.internal.entity.creator.CreatorMarketInfoEntity;
import com.playlet.internal.query.creator.TalentMarketListQuery;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.creator.TalentMarketService;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 人才市场实现：列表 + 详情。
 */
@Slf4j
@RestController
@CrossOrigin
public class TalentMarketServiceImpl extends BaseApiService implements TalentMarketService {

	@Resource
	private CreatorMarketInfoDao creatorMarketInfoDao;
	@Resource
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase list(TalentMarketListQuery query, HttpServletRequest request) {
		if (query == null) {
			query = new TalentMarketListQuery();
		}
		normalizeListQuery(query);
		Integer qualType = TalentConstants.qualCodeOf(query.getQualification());
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<CreatorMarketInfoEntity> list = creatorMarketInfoDao.findMarketList(query, qualType);
		List<TalentMarketCardRespEntity> cards = new ArrayList<>();
		if (list != null) {
			for (CreatorMarketInfoEntity entity : list) {
				cards.add(toCard(entity));
			}
		}
		PageInfo<CreatorMarketInfoEntity> pageInfo = new PageInfo<>(list);
		PageInfo<TalentMarketCardRespEntity> respPage = new PageInfo<>();
		respPage.setTotal(pageInfo.getTotal());
		respPage.setPageNum(pageInfo.getPageNum());
		respPage.setPageSize(pageInfo.getPageSize());
		respPage.setPages(pageInfo.getPages());
		respPage.setList(cards);
		return setResultSuccess(respPage);
	}

	@Override
	public ResponseBase detail(Integer id, HttpServletRequest request) {
		CreatorMarketInfoEntity entity = creatorMarketInfoDao.findVisibleById(id);
		if (entity == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		return setResultSuccess(toDetail(entity));
	}

	/** 列表入参规范化：全部/空串 -> null，默认值兜底 */
	private void normalizeListQuery(TalentMarketListQuery query) {
		if (query.getRole() == null || (query.getRole() != TalentConstants.ROLE_WRITER
				&& query.getRole() != TalentConstants.ROLE_CREATOR)) {
			query.setRole(TalentConstants.ROLE_WRITER);
		}
		query.setKeyword(normalizeText(query.getKeyword()));
		query.setService(normalizeText(query.getService()));
		query.setProduction(normalizeText(query.getProduction()));
		query.setSkill(normalizeText(query.getSkill()));
		query.setStyle(normalizeText(query.getStyle()));
		query.setContract(normalizeText(query.getContract()));
		if (query.getSortType() == null || query.getSortType() < TalentConstants.SORT_COMPREHENSIVE
				|| query.getSortType() > TalentConstants.SORT_REVENUE) {
			query.setSortType(TalentConstants.SORT_COMPREHENSIVE);
		}
	}

	private String normalizeText(String text) {
		if (text == null) {
			return null;
		}
		String trimmed = text.trim();
		if (trimmed.isEmpty() || TalentConstants.FILTER_ALL.equals(trimmed)) {
			return null;
		}
		return trimmed;
	}

	/** 实体 -> 列表卡片 */
	private TalentMarketCardRespEntity toCard(CreatorMarketInfoEntity entity) {
		TalentMarketCardRespEntity card = new TalentMarketCardRespEntity();
		card.setId(entity.getId());
		card.setMarketRole(entity.getMarketRole());
		card.setName(entity.getName());
		card.setAvatarUrl(mediaUrlService.sign(entity.getAvatar()));
		card.setType(TalentConstants.qualLabelOf(entity.getQualType()));
		card.setBadge(entity.getBadge());
		card.setCertified(entity.getCertified() != null && entity.getCertified() == 1);
		card.setProduction(entity.getProduction());
		card.setSkills(parseStrArray(entity.getSkills()));
		card.setTags(parseStrArray(entity.getTags()));
		card.setServices(parseStrArray(entity.getServices()));
		card.setStyle(entity.getStyle());
		card.setScore(entity.getScore());
		card.setCity(entity.getCity());
		card.setWorks(entity.getWorks());
		card.setDeals(entity.getDeals());
		card.setRevenue(entity.getRevenue());
		card.setPrice(entity.getPrice());
		card.setPriceUnit(entity.getPriceUnit());
		card.setStatus(TalentConstants.acceptLabelOf(entity.getAcceptStatus()));
		return card;
	}

	/** 实体 -> 详情 */
	private TalentMarketDetailRespEntity toDetail(CreatorMarketInfoEntity entity) {
		boolean isCreator = entity.getMarketRole() != null
				&& entity.getMarketRole() == TalentConstants.ROLE_CREATOR;
		List<String> tags = parseStrArray(entity.getTags());
		List<String> services = parseStrArray(entity.getServices());

		TalentMarketDetailRespEntity resp = new TalentMarketDetailRespEntity();
		resp.setKind(isCreator ? "creator" : "writer");
		resp.setId(entity.getId());
		resp.setName(entity.getName());
		resp.setAvatarUrl(mediaUrlService.sign(entity.getAvatar()));
		resp.setType(TalentConstants.qualLabelOf(entity.getQualType()));
		resp.setCertLabel(isCreator ? "认证创作者" : "认证编剧");

		// 详情 chips：题材标签 + 承接服务，最多 5 个
		List<String> chips = new ArrayList<>(tags);
		for (String service : services) {
			if (!chips.contains(service)) {
				chips.add(service);
			}
		}
		resp.setTags(chips.size() > 5 ? chips.subList(0, 5) : chips);

		resp.setStats(buildStats(entity, isCreator, services));
		resp.setIntro(buildIntro(entity, tags));
		resp.setSideRows(buildSideRows(entity, isCreator));
		resp.setSections(buildSections(entity, isCreator, tags));
		return resp;
	}

	private List<TalentMarketDetailRespEntity.StatItem> buildStats(CreatorMarketInfoEntity entity,
			boolean isCreator, List<String> services) {
		List<TalentMarketDetailRespEntity.StatItem> stats = new ArrayList<>();
		if (isCreator) {
			stats.add(stat("综合评分", String.valueOf(entity.getScore()), "分", "orange"));
			// 好评率无真实值，无数据则展示 0%，禁止虚构 98%
			stats.add(stat("合作评价", (entity.getPraiseRate() == null ? 0 : entity.getPraiseRate()) + "%", "好评率", null));
			stats.add(stat("所在地", entity.getCity(), null, null));
			stats.add(stat("成交单数", String.valueOf(entity.getDeals() == null ? 0 : entity.getDeals()), "单", null));
		} else {
			stats.add(stat("所在地", entity.getCity(), null, null));
			stats.add(stat("综合评分", String.valueOf(entity.getScore()), "分", "orange"));
			long price = entity.getPrice() == null ? 0 : entity.getPrice().longValue();
			stats.add(stat("服务价格", "¥ " + String.format("%,d", price),
					" 起/" + (entity.getPriceUnit() == null ? "部" : entity.getPriceUnit()), "red"));
			stats.add(stat("承接服务", String.join(" / ", services), null, null));
		}
		return stats;
	}

	private TalentMarketDetailRespEntity.StatItem stat(String label, String value, String suffix, String tone) {
		TalentMarketDetailRespEntity.StatItem item = new TalentMarketDetailRespEntity.StatItem();
		item.setLabel(label);
		item.setValue(value);
		item.setSuffix(suffix);
		item.setTone(tone);
		return item;
	}

	private String buildIntro(CreatorMarketInfoEntity entity, List<String> tags) {
		if (entity.getIntro() != null && !entity.getIntro().trim().isEmpty()) {
			return entity.getIntro();
		}
		// 无真实简介时返回空，不编造营销文案
		return "";
	}

	private List<TalentMarketDetailRespEntity.SideRow> buildSideRows(CreatorMarketInfoEntity entity,
			boolean isCreator) {
		List<TalentMarketDetailRespEntity.SideRow> rows = new ArrayList<>();
		if (isCreator) {
			rows.add(sideRow("入驻时间", entity.getJoinDate(), null));
			rows.add(sideRow("平均交付周期",
					entity.getAvgDeliveryDays() == null ? "-" : entity.getAvgDeliveryDays() + " 天", null));
			String acceptLabel = TalentConstants.acceptLabelOf(entity.getAcceptStatus());
			rows.add(sideRow("接单状态", acceptLabel,
					entity.getAcceptStatus() != null && entity.getAcceptStatus() == TalentConstants.ACCEPT_OPEN ? "green" : null));
			rows.add(sideRow("身份认证", "已认证", "green"));
			rows.add(sideRow("代表作品", (entity.getWorks() == null ? 0 : entity.getWorks()) + " 部", null));
		} else {
			rows.add(sideRow("成交单数", (entity.getDeals() == null ? 0 : entity.getDeals()) + " 单", null));
			rows.add(sideRow("好评率", (entity.getPraiseRate() == null ? 0 : entity.getPraiseRate()) + "%", null));
			rows.add(sideRow("入驻时间", entity.getJoinDate(), null));
			rows.add(sideRow("平均交付周期",
					entity.getAvgDeliveryDays() == null ? "-" : entity.getAvgDeliveryDays() + " 天", null));
			rows.add(sideRow("身份认证", "已认证", "green"));
		}
		return rows;
	}

	private TalentMarketDetailRespEntity.SideRow sideRow(String label, String value, String tone) {
		TalentMarketDetailRespEntity.SideRow row = new TalentMarketDetailRespEntity.SideRow();
		row.setLabel(label);
		row.setValue(value);
		row.setTone(tone);
		return row;
	}

	private List<TalentMarketDetailRespEntity.Section> buildSections(CreatorMarketInfoEntity entity,
			boolean isCreator, List<String> tags) {
		List<TalentMarketDetailRespEntity.Section> sections = new ArrayList<>();
		if (isCreator) {
			List<Map<String, Object>> packages = parseMapArray(entity.getPackages());
			sections.add(section("服务套餐", "packages",
					packages == null ? Collections.emptyList() : packages));

			List<Map<String, Object>> worksList = parseMapArray(entity.getWorksList());
			sections.add(section("制作过的剧本", "list",
					worksList == null ? Collections.emptyList() : worksList));
		} else {
			List<Map<String, Object>> worksList = parseMapArray(entity.getWorksList());
			sections.add(section("已上线作品", "list",
					worksList == null ? Collections.emptyList() : worksList));

			List<Map<String, Object>> scriptsList = parseMapArray(entity.getScriptsList());
			sections.add(section("在售剧本", "cards",
					scriptsList == null ? Collections.emptyList() : scriptsList));
		}
		return sections;
	}

	private TalentMarketDetailRespEntity.Section section(String title, String kind, List<Map<String, Object>> items) {
		TalentMarketDetailRespEntity.Section section = new TalentMarketDetailRespEntity.Section();
		section.setTitle(title);
		section.setKind(kind);
		section.setItems(items);
		return section;
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

	/** 解析 JSON 对象数组，异常/为空返回 null（用于判断是否走兜底） */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> parseMapArray(String json) {
		if (json == null || json.trim().isEmpty()) {
			return null;
		}
		try {
			List<Map<String, Object>> parsed = (List<Map<String, Object>>) (List<?>) JSON.parseArray(json, Map.class);
			if (parsed != null && !parsed.isEmpty()) {
				return parsed;
			}
		} catch (Exception e) {
			log.warn("parse json map array fail: {}", json);
		}
		return null;
	}
}
