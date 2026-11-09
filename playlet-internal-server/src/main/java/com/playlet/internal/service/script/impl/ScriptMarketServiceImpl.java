package com.playlet.internal.service.script.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.ScriptApplyCreateRespEntity;
import com.playlet.internal.api.response.ScriptAuthorRespEntity;
import com.playlet.internal.api.response.ScriptFavoriteItemRespEntity;
import com.playlet.internal.api.response.ScriptFilterDictRespEntity;
import com.playlet.internal.api.response.ScriptLatestRespEntity;
import com.playlet.internal.api.response.ScriptMarketCardRespEntity;
import com.playlet.internal.api.response.ScriptMarketDetailRespEntity;
import com.playlet.internal.api.response.ScriptTagRow;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.ScriptConstants;
import com.playlet.internal.dao.account.UserFollowDao;
import com.playlet.internal.dao.script.ScriptDao;
import com.playlet.internal.dao.script.ScriptFavoriteDao;
import com.playlet.internal.dao.script.ScriptStatDao;
import com.playlet.internal.dao.script.ScriptTagDao;
import com.playlet.internal.dao.script.ScriptTagRelDao;
import com.playlet.internal.dao.script.ScriptTradeApplyDao;
import com.playlet.internal.entity.script.ScriptEntity;
import com.playlet.internal.entity.script.ScriptTagEntity;
import com.playlet.internal.entity.script.ScriptTradeApplyEntity;
import com.playlet.internal.enums.ScriptChannelEnums;
import com.playlet.internal.enums.ScriptLicenseTypeEnums;
import com.playlet.internal.enums.ScriptSaleStatusEnums;
import com.playlet.internal.enums.ScriptTradeApplyStatusEnums;
import com.playlet.internal.enums.ScriptTradeApplyTypeEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.query.script.ScriptMarketListQuery;
import com.playlet.internal.query.script.ScriptTradeApplyQuery;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.script.ScriptMarketService;
import com.playlet.internal.service.script.ScriptSupportService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * C 端剧本市场实现。
 */
@Slf4j
@RestController
@CrossOrigin
public class ScriptMarketServiceImpl extends BaseApiService implements ScriptMarketService {

	@Resource
	private ScriptDao scriptDao;
	@Resource
	private ScriptStatDao scriptStatDao;
	@Resource
	private ScriptTagDao scriptTagDao;
	@Resource
	private ScriptTagRelDao scriptTagRelDao;
	@Resource
	private ScriptFavoriteDao scriptFavoriteDao;
	@Resource
	private ScriptTradeApplyDao scriptTradeApplyDao;
	@Resource
	private UserFollowDao userFollowDao;
	@Resource
	private MediaUrlService mediaUrlService;
	@Resource
	private ScriptSupportService scriptSupportService;


	@Override
	public ResponseBase filters() {
		ScriptFilterDictRespEntity resp = new ScriptFilterDictRespEntity();
		List<ScriptFilterDictRespEntity.FilterGroup> groups = new ArrayList<>();

		ScriptFilterDictRespEntity.FilterGroup topicGroup = new ScriptFilterDictRespEntity.FilterGroup();
		topicGroup.setKey("topic");
		topicGroup.setLabel("题材类型");
		List<ScriptFilterDictRespEntity.Option> topicOptions = new ArrayList<>();
		topicOptions.add(option("全部", ScriptConstants.TOPIC_ALL));
		List<ScriptTagEntity> topicTags = scriptTagDao.findByGroup(ScriptConstants.TAG_GROUP_TOPIC, ScriptConstants.LANGUE);
		if (topicTags == null || topicTags.isEmpty()) {
			for (String name : ScriptConstants.TOPIC_OPTIONS) {
				topicOptions.add(option(name, name));
			}
		} else {
			for (ScriptTagEntity tag : topicTags) {
				topicOptions.add(option(tag.getTagName(), tag.getTagName()));
			}
		}
		topicGroup.setOptions(topicOptions);
		groups.add(topicGroup);

		ScriptFilterDictRespEntity.FilterGroup channelGroup = new ScriptFilterDictRespEntity.FilterGroup();
		channelGroup.setKey("channel");
		channelGroup.setLabel("内容分区");
		List<ScriptFilterDictRespEntity.Option> channelOptions = new ArrayList<>();
		channelOptions.add(option("全部", 0));
		for (ScriptChannelEnums item : ScriptChannelEnums.values()) {
			channelOptions.add(option(item.getLabel(), item.getCode()));
		}
		channelGroup.setOptions(channelOptions);
		groups.add(channelGroup);

		ScriptFilterDictRespEntity.FilterGroup licenseGroup = new ScriptFilterDictRespEntity.FilterGroup();
		licenseGroup.setKey("licenseType");
		licenseGroup.setLabel("授权类型");
		List<ScriptFilterDictRespEntity.Option> licenseOptions = new ArrayList<>();
		licenseOptions.add(option("全部", 0));
		for (ScriptLicenseTypeEnums item : ScriptLicenseTypeEnums.values()) {
			licenseOptions.add(option(item.getLabel(), item.getCode()));
		}
		licenseGroup.setOptions(licenseOptions);
		groups.add(licenseGroup);

		ScriptFilterDictRespEntity.FilterGroup priceGroup = new ScriptFilterDictRespEntity.FilterGroup();
		priceGroup.setKey("priceRange");
		priceGroup.setLabel("价格范围");
		List<ScriptFilterDictRespEntity.Option> priceOptions = new ArrayList<>();
		priceOptions.add(option("全部", 0));
		priceOptions.add(option("1万以下", 1));
		priceOptions.add(option("1-5万", 2));
		priceOptions.add(option("5-10万", 3));
		priceOptions.add(option("10万以上", 4));
		priceGroup.setOptions(priceOptions);
		groups.add(priceGroup);

		ScriptFilterDictRespEntity.FilterGroup scaleGroup = new ScriptFilterDictRespEntity.FilterGroup();
		scaleGroup.setKey("scaleRange");
		scaleGroup.setLabel("剧集规模");
		List<ScriptFilterDictRespEntity.Option> scaleOptions = new ArrayList<>();
		scaleOptions.add(option("全部", 0));
		scaleOptions.add(option("10集以下", 1));
		scaleOptions.add(option("10-30集", 2));
		scaleOptions.add(option("30-60集", 3));
		scaleOptions.add(option("60集以上", 4));
		scaleGroup.setOptions(scaleOptions);
		groups.add(scaleGroup);

		ScriptFilterDictRespEntity.FilterGroup saleGroup = new ScriptFilterDictRespEntity.FilterGroup();
		saleGroup.setKey("saleStatus");
		saleGroup.setLabel("剧本状态");
		List<ScriptFilterDictRespEntity.Option> saleOptions = new ArrayList<>();
		saleOptions.add(option("全部", -1));
		for (ScriptSaleStatusEnums item : ScriptSaleStatusEnums.values()) {
			saleOptions.add(option(item.getLabel(), item.getCode()));
		}
		saleGroup.setOptions(saleOptions);
		groups.add(saleGroup);

		resp.setFilterGroups(groups);

		List<ScriptFilterDictRespEntity.SortTab> sortTabs = new ArrayList<>();
		sortTabs.add(sortTab("综合", ScriptConstants.SORT_COMPREHENSIVE));
		sortTabs.add(sortTab("最新发布", ScriptConstants.SORT_LATEST));
		sortTabs.add(sortTab("评分最高", ScriptConstants.SORT_SCORE));
		sortTabs.add(sortTab("价格优先", ScriptConstants.SORT_PRICE));
		sortTabs.add(sortTab("热度最高", ScriptConstants.SORT_HEAT));
		resp.setSortTabs(sortTabs);

		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase list(ScriptMarketListQuery query, HttpServletRequest request) {
		if (query == null) {
			query = new ScriptMarketListQuery();
		}
		normalizeListQuery(query);
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<ScriptMarketCardRespEntity> list = scriptDao.findMarketList(query);
		fillCardTags(list);
		for (ScriptMarketCardRespEntity card : list) {
			card.setChannelText(scriptSupportService.channelText(card.getChannel()));
			card.setLicenseText(scriptSupportService.licenseText(card.getLicenseType()));
			card.setSaleStatusText(scriptSupportService.saleStatusText(card.getSaleStatus()));
			card.setCoverUrl(mediaUrlService.sign(card.getCoverUrl()));
			ScriptAuthorRespEntity author = new ScriptAuthorRespEntity();
			author.setId(card.getAuthorId());
			author.setName(card.getAuthorName());
			author.setAvatarUrl(mediaUrlService.sign(card.getAuthorAvatar()));
			author.setScriptCount(card.getAuthorScriptCount());
			author.setScore(card.getAuthorScore());
			card.setAuthor(author);
		}
		return setResultSuccess(new PageInfo<>(list));
	}

	@Override
	public ResponseBase detail(@RequestParam("id") Integer id, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		ScriptEntity script = scriptDao.findVisibleById(id);
		if (script == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		scriptStatDao.initIfAbsent(id);
		scriptStatDao.incrView(id);
		ScriptMarketDetailRespEntity resp = new ScriptMarketDetailRespEntity();
		scriptSupportService.fillDetail(resp, script, uid);
		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase recommendAuthors(@RequestParam(value = "limit", required = false) Integer limit,
			HttpServletRequest request) {
		int size = normalizeLimit(limit);
		Integer uid = AppTokenUtil.resolveUid(request);
		// 返回剧本编剧信息
		List<ScriptAuthorRespEntity> authors = scriptDao.findRecommendAuthors(size);
		Set<Integer> followedIds = new HashSet<>();
		if (uid != null && authors != null && !authors.isEmpty()) {
			List<Integer> sysUserIds = new ArrayList<>();
			for (ScriptAuthorRespEntity author : authors) {
				if (author.getSysUserId() != null) {
					sysUserIds.add(author.getSysUserId());
				}
			}
			if (!sysUserIds.isEmpty()) {
				// 当前用户在候选列表中已关注的 follow_uid
				List<Integer> followed = userFollowDao.findFollowedAmong(uid, sysUserIds);
				if (followed != null) {
					followedIds.addAll(followed);
				}
			}
		}
		if (authors != null) {
			for (ScriptAuthorRespEntity author : authors) {
				author.setAvatarUrl(mediaUrlService.sign(author.getAvatarUrl()));
				author.setFollowed(author.getSysUserId() != null && followedIds.contains(author.getSysUserId()));
			}
		}
		return setResultSuccess(authors);
	}

	@Override
	public ResponseBase latest(@RequestParam(value = "limit", required = false) Integer limit) {
		int size = normalizeLimit(limit);
		List<ScriptLatestRespEntity> list = scriptDao.findLatest(size);
		if (list != null) {
			for (ScriptLatestRespEntity item : list) {
				item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
				String ago = scriptSupportService.publishedAgo(item.getPublishedAt());
				item.setPublishedAtText(ago == null ? null : ago + "发布");
			}
		}
		return setResultSuccess(list);
	}

	@Override
	public ResponseBase favoriteAdd(@RequestParam("scriptId") Integer scriptId, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (scriptDao.findVisibleById(scriptId) == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		int inserted = scriptFavoriteDao.insertIgnore(uid, scriptId);
		if (inserted > 0) {
			scriptStatDao.initIfAbsent(scriptId);
			scriptStatDao.incrFavorite(scriptId, 1);
		}
		return setResultSuccess(true);
	}

	@Override
	public ResponseBase favoriteCancel(@RequestParam("scriptId") Integer scriptId, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		int deleted = scriptFavoriteDao.deleteByUidAndScriptId(uid, scriptId);
		if (deleted > 0) {
			scriptStatDao.initIfAbsent(scriptId);
			scriptStatDao.incrFavorite(scriptId, -1);
		}
		return setResultSuccess(true);
	}

	@Override
	public ResponseBase favoriteList(PageQueryHelperEntity query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query == null) {
			query = new PageQueryHelperEntity();
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<ScriptFavoriteItemRespEntity> list = scriptDao.findFavoriteList(uid);
		if (list != null) {
			for (ScriptFavoriteItemRespEntity item : list) {
				item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
				item.setSaleStatusText(scriptSupportService.saleStatusText(item.getSaleStatus()));
			}
		}
		return setResultSuccess(new PageInfo<>(list));
	}

	@Override
	public ResponseBase tradeApply(@Valid @RequestBody ScriptTradeApplyQuery query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (!ScriptTradeApplyTypeEnums.isValid(query.getApplyType())) {
			return setResultError(Constants.HTTP_RES_CODE_400, "申请类型不合法");
		}
		// 查询剧本信息
		ScriptEntity script = scriptDao.findVisibleById(query.getScriptId());
		if (script == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		// 购买方是否为该剧本作者
		if (scriptDao.countAuthorSelf(script.getAuthorId(), uid) > 0) {
			return setResultError(Constants.HTTP_RES_CODE_400, "不能申请自己发布的剧本");
		}
		if (!ScriptSaleStatusEnums.canApply(script.getSaleStatus())) {
			return setResultError(Constants.HTTP_RES_CODE_400, "该剧本当前不可申请");
		}
		// 查询剧本市场根据购买用户id，剧本id
		if (scriptTradeApplyDao.findPendingByBuyer(script.getId(), uid) != null) {
			return setResultError(I18nUtil.getMessage("base_info_exist"));
		}

		BigDecimal originalPrice = script.getPriceYuan() == null ? BigDecimal.ZERO : script.getPriceYuan();
		BigDecimal offerPrice = query.getOfferPrice();
		if (ScriptTradeApplyTypeEnums.BUY.getCode() == query.getApplyType()) {
			offerPrice = originalPrice;
		} else {
			if (offerPrice == null || offerPrice.compareTo(BigDecimal.ZERO) <= 0) {
				return setResultError(Constants.HTTP_RES_CODE_400, "议价金额必须大于 0");
			}
			if (offerPrice.compareTo(originalPrice) >= 0) {
				return setResultError(Constants.HTTP_RES_CODE_400, "议价金额必须低于剧本售价");
			}
		}

		// 构建剧本交易申请
		Date now = new Date();
		ScriptTradeApplyEntity entity = new ScriptTradeApplyEntity();
		entity.setApplyNo(buildApplyNo(now));
		entity.setScriptId(script.getId());
		entity.setBuyerUid(uid);
		entity.setAuthorId(script.getAuthorId());
		entity.setApplyType(query.getApplyType());
		entity.setLicenseType(script.getLicenseType());
		entity.setOriginalPriceYuan(originalPrice);
		entity.setOfferPriceYuan(offerPrice);
		entity.setContactName(query.getContactName());
		entity.setContactMobileCipher(scriptSupportService.encryptMobile(query.getContactMobile().trim()));
		entity.setContactMobileHash(scriptSupportService.hashMobile(query.getContactMobile().trim()));
		entity.setMessage(query.getMessage());
		entity.setApplyStatus(ScriptTradeApplyStatusEnums.PENDING.getCode());
		entity.setExpireTime(new Date(now.getTime() + ScriptConstants.APPLY_EXPIRE_DAYS * 24L * 60 * 60 * 1000));
		entity.setScriptNameSnapshot(script.getScriptName());
		ScriptAuthorRespEntity author = scriptDao.findAuthorById(script.getAuthorId());
		entity.setAuthorNameSnapshot(author == null ? null : author.getName());
		entity.setCoverKeySnapshot(script.getCoverUrl());
		entity.setLicenseScopeSnapshot(script.getLicenseScope());
		entity.setSetTime(now);
		entity.setGmtModified(now);
		scriptTradeApplyDao.insert(entity);

		// 新增剧本查看记录
		scriptStatDao.initIfAbsent(script.getId());
		// 添加申请数量
		scriptStatDao.incrApply(script.getId());
		if (ScriptSaleStatusEnums.ON_SALE.getCode() == (script.getSaleStatus() == null ? -1 : script.getSaleStatus())) {
			int version = script.getVersion() == null ? 0 : script.getVersion();
			// 行锁
			scriptDao.casSaleStatus(script.getId(), ScriptSaleStatusEnums.ON_SALE.getCode(),
					ScriptSaleStatusEnums.NEGOTIATING.getCode(), version);
		}

		ScriptApplyCreateRespEntity resp = new ScriptApplyCreateRespEntity();
		resp.setApplyId(entity.getId());
		resp.setApplyNo(entity.getApplyNo());
		resp.setApplyStatus(entity.getApplyStatus());
		resp.setApplyStatusText(scriptSupportService.applyStatusText(entity.getApplyStatus()));
		log.info("script trade apply created applyId={} scriptId={} uid={}", entity.getId(), script.getId(), uid);
		return setResultSuccess(resp);
	}

	private void normalizeListQuery(ScriptMarketListQuery query) {
		if (query.getKeyword() != null) {
			String keyword = query.getKeyword().trim();
			query.setKeyword(keyword.isEmpty() ? null : keyword);
		}
		if (query.getTopic() != null) {
			String topic = query.getTopic().trim();
			if (topic.isEmpty() || ScriptConstants.TOPIC_ALL.equals(topic)) {
				query.setTopic(null);
			} else {
				query.setTopic(topic);
			}
		}
		if (query.getSortType() == null) {
			query.setSortType(ScriptConstants.SORT_COMPREHENSIVE);
		}
	}

	private void fillCardTags(List<ScriptMarketCardRespEntity> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<Integer> ids = new ArrayList<>();
		for (ScriptMarketCardRespEntity card : list) {
			ids.add(card.getId());
		}
		List<ScriptTagRow> rows = scriptTagRelDao.findTagsByScriptIds(ids);
		Map<Integer, List<String>> tagMap = new HashMap<>();
		if (rows != null) {
			for (ScriptTagRow row : rows) {
				tagMap.computeIfAbsent(row.getScriptId(), k -> new ArrayList<>()).add(row.getTagName());
			}
		}
		for (ScriptMarketCardRespEntity card : list) {
			card.setTags(tagMap.getOrDefault(card.getId(), new ArrayList<>()));
		}
	}

	private int normalizeLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return ScriptConstants.DEFAULT_LIMIT;
		}
		return Math.min(limit, ScriptConstants.MAX_LIMIT);
	}

	private String buildApplyNo(Date now) {
		String time = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		return "ST" + time + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
	}

	private ScriptFilterDictRespEntity.Option option(String label, Object value) {
		ScriptFilterDictRespEntity.Option option = new ScriptFilterDictRespEntity.Option();
		option.setLabel(label);
		option.setValue(value);
		return option;
	}

	private ScriptFilterDictRespEntity.SortTab sortTab(String label, Object value) {
		ScriptFilterDictRespEntity.SortTab tab = new ScriptFilterDictRespEntity.SortTab();
		tab.setLabel(label);
		tab.setValue(value);
		return tab;
	}
}
