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
import java.util.LinkedHashMap;
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

	/** 兜底详情区块的标题池 */
	private static final String[] WORK_TITLE_POOL = {
			"逆袭千金归来", "豪门弃女的复仇", "重回十八岁", "穿书后我成了团宠",
			"总裁的替嫁新娘", "都市最强赘婿", "娘娘她总想造反", "神医下山",
			"闪婚后被宠上天", "乡村小医仙", "战神的贴身女医", "重生之豪门主母"
	};

	@Override
	public ResponseBase list(TalentMarketListQuery query, HttpServletRequest request) {
		try {
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
		} catch (Exception e) {
			log.error("talent market list error", e);
			return setResultError(Constants.HTTP_RES_CODE_500, I18nUtil.getMessage("base_error"));
		}
	}

	@Override
	public ResponseBase detail(Integer id, HttpServletRequest request) {
		try {
			CreatorMarketInfoEntity entity = creatorMarketInfoDao.findVisibleById(id);
			if (entity == null) {
				return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
			}
			return setResultSuccess(toDetail(entity));
		} catch (Exception e) {
			log.error("talent market detail error id={}", id, e);
			return setResultError(Constants.HTTP_RES_CODE_500, I18nUtil.getMessage("base_error"));
		}
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
			stats.add(stat("合作评价", (entity.getPraiseRate() == null ? 98 : entity.getPraiseRate()) + "%", "好评率", null));
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
		// 兜底简介
		return entity.getName() + "，深耕短剧行业多年，擅长" + String.join("、", tags)
				+ "等题材，交付准时，配合度高，支持修改完善。";
	}

	private List<TalentMarketDetailRespEntity.SideRow> buildSideRows(CreatorMarketInfoEntity entity,
			boolean isCreator) {
		List<TalentMarketDetailRespEntity.SideRow> rows = new ArrayList<>();
		if (isCreator) {
			rows.add(sideRow("入驻时间", entity.getJoinDate(), null));
			rows.add(sideRow("平均交付周期", (entity.getAvgDeliveryDays() == null ? 15 : entity.getAvgDeliveryDays()) + " 天", null));
			String acceptLabel = TalentConstants.acceptLabelOf(entity.getAcceptStatus());
			rows.add(sideRow("接单状态", acceptLabel,
					entity.getAcceptStatus() != null && entity.getAcceptStatus() == TalentConstants.ACCEPT_OPEN ? "green" : null));
			rows.add(sideRow("身份认证", "已认证", "green"));
			rows.add(sideRow("代表作品", (entity.getWorks() == null ? 0 : entity.getWorks()) + " 部", null));
		} else {
			rows.add(sideRow("成交单数", (entity.getDeals() == null ? 0 : entity.getDeals()) + " 单", null));
			rows.add(sideRow("好评率", (entity.getPraiseRate() == null ? 98 : entity.getPraiseRate()) + "%", null));
			rows.add(sideRow("入驻时间", entity.getJoinDate(), null));
			rows.add(sideRow("平均交付周期", (entity.getAvgDeliveryDays() == null ? 15 : entity.getAvgDeliveryDays()) + " 天", null));
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
			if (packages == null) {
				packages = fallbackPackages(entity);
			}
			sections.add(section("服务套餐", "packages", packages));

			List<Map<String, Object>> worksList = parseMapArray(entity.getWorksList());
			if (worksList == null) {
				worksList = fallbackCreatorWorks(entity, tags);
			}
			sections.add(section("制作过的剧本", "list", worksList));
		} else {
			List<Map<String, Object>> worksList = parseMapArray(entity.getWorksList());
			if (worksList == null) {
				worksList = fallbackWriterWorks(entity, tags);
			}
			sections.add(section("已上线作品", "list", worksList));

			List<Map<String, Object>> scriptsList = parseMapArray(entity.getScriptsList());
			if (scriptsList == null) {
				scriptsList = fallbackScripts(entity, tags);
			}
			sections.add(section("在售剧本", "cards", scriptsList));
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

	/** 兜底：编剧已上线作品 */
	private List<Map<String, Object>> fallbackCreatorWorks(CreatorMarketInfoEntity entity, List<String> tags) {
		List<Map<String, Object>> items = new ArrayList<>();
		int offset = entity.getId() == null ? 0 : entity.getId();
		for (int i = 0; i < 4; i++) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("title", WORK_TITLE_POOL[(offset + i) % WORK_TITLE_POOL.length]);
			String tag = tags.isEmpty() ? "都市" : tags.get(i % tags.size());
			item.put("meta", tag + " · " + (20 + i * 8) + "集");
			item.put("rightLabel", "评分");
			item.put("rightValue", String.format("%.1f", 8.6 + (i % 4) * 0.2));
			item.put("highlight", true);
			items.add(item);
		}
		return items;
	}

	/** 兜底：编剧已上线作品（播放量维度） */
	private List<Map<String, Object>> fallbackWriterWorks(CreatorMarketInfoEntity entity, List<String> tags) {
		List<Map<String, Object>> items = new ArrayList<>();
		int offset = entity.getId() == null ? 0 : entity.getId();
		for (int i = 0; i < 4; i++) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("title", WORK_TITLE_POOL[(offset + i) % WORK_TITLE_POOL.length]);
			String tag = tags.isEmpty() ? "都市" : tags.get(i % tags.size());
			item.put("meta", tag + " · " + (30 + i * 10) + "集");
			item.put("rightLabel", "平台播放量");
			item.put("rightValue", (9 - i) + "." + (i % 5) + "千万");
			items.add(item);
		}
		return items;
	}

	/** 兜底：编剧在售剧本 */
	private List<Map<String, Object>> fallbackScripts(CreatorMarketInfoEntity entity, List<String> tags) {
		List<Map<String, Object>> items = new ArrayList<>();
		int offset = entity.getId() == null ? 0 : entity.getId();
		for (int i = 0; i < 4; i++) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("title", WORK_TITLE_POOL[(offset + 4 + i) % WORK_TITLE_POOL.length]);
			item.put("tags", tags.size() > 3 ? tags.subList(0, 3) : tags);
			item.put("meta", "全本约 " + String.format("%.1f", 3 + i * 0.3) + " 万字 / " + (28 + i * 6) + "集");
			items.add(item);
		}
		return items;
	}

	/** 兜底：创作者服务套餐 */
	private List<Map<String, Object>> fallbackPackages(CreatorMarketInfoEntity entity) {
		long price = entity.getPrice() == null ? 0 : entity.getPrice().longValue();
		String unit = entity.getPriceUnit() == null ? "部" : entity.getPriceUnit();
		List<String> skills = parseStrArray(entity.getSkills());

		List<Map<String, Object>> items = new ArrayList<>();
		Map<String, Object> full = new LinkedHashMap<>();
		full.put("title", "全流程制作");
		full.put("desc", "包含" + String.join("、", skills.size() > 3 ? skills.subList(0, 3) : skills) + "全流程服务");
		full.put("price", price);
		full.put("priceUnit", unit);
		items.add(full);

		Map<String, Object> polish = new LinkedHashMap<>();
		polish.put("title", "精剪+调色包装");
		polish.put("desc", "素材精剪、电影感调色、片头片尾特效包装");
		polish.put("price", Math.round(price * 0.3));
		polish.put("priceUnit", "集");
		items.add(polish);

		Map<String, Object> single = new LinkedHashMap<>();
		single.put("title", "单集定制剪辑");
		single.put("desc", "单集单独剪辑优化，调整节奏、提升质感");
		single.put("price", Math.round(price * 0.2));
		single.put("priceUnit", "集");
		items.add(single);
		return items;
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
