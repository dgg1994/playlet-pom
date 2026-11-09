package com.playlet.internal.service.script.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.CreatorScriptApplyItemRespEntity;
import com.playlet.internal.api.response.CreatorScriptInfoRespEntity;
import com.playlet.internal.api.response.CreatorScriptListRespEntity;
import com.playlet.internal.api.response.ScriptTradeApplyRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.ScriptConstants;
import com.playlet.internal.dao.script.ScriptCharacterDao;
import com.playlet.internal.dao.script.ScriptDao;
import com.playlet.internal.dao.script.ScriptEpisodeDao;
import com.playlet.internal.dao.script.ScriptPreviewDao;
import com.playlet.internal.dao.script.ScriptStatDao;
import com.playlet.internal.dao.script.ScriptTagDao;
import com.playlet.internal.dao.script.ScriptTagRelDao;
import com.playlet.internal.dao.script.ScriptTradeApplyDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.script.ScriptCharacterEntity;
import com.playlet.internal.entity.script.ScriptEntity;
import com.playlet.internal.entity.script.ScriptEpisodeEntity;
import com.playlet.internal.entity.script.ScriptPreviewEntity;
import com.playlet.internal.entity.script.ScriptTagEntity;
import com.playlet.internal.entity.script.ScriptTagRelEntity;
import com.playlet.internal.entity.script.ScriptTradeApplyEntity;
import com.playlet.internal.enums.ScriptAuditStatusEnums;
import com.playlet.internal.enums.ScriptSaleStatusEnums;
import com.playlet.internal.enums.ScriptShelfStatusEnums;
import com.playlet.internal.enums.ScriptTradeApplyStatusEnums;
import com.playlet.internal.query.script.CreatorScriptApplyListQuery;
import com.playlet.internal.query.script.CreatorScriptListQuery;
import com.playlet.internal.query.script.ScriptApplyHandleQuery;
import com.playlet.internal.query.script.ScriptIdQuery;
import com.playlet.internal.query.script.ScriptSaveQuery;
import com.playlet.internal.query.script.ScriptShelfQuery;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.script.CreatorScriptService;
import com.playlet.internal.service.script.ScriptSupportService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作家端剧本管理实现。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorScriptServiceImpl extends BaseApiService implements CreatorScriptService {

	@Autowired
	private ScriptDao scriptDao;
	@Autowired
	private ScriptStatDao scriptStatDao;
	@Autowired
	private ScriptTagDao scriptTagDao;
	@Autowired
	private ScriptTagRelDao scriptTagRelDao;
	@Autowired
	private ScriptCharacterDao scriptCharacterDao;
	@Autowired
	private ScriptEpisodeDao scriptEpisodeDao;
	@Autowired
	private ScriptPreviewDao scriptPreviewDao;
	@Autowired
	private ScriptTradeApplyDao scriptTradeApplyDao;
	@Autowired
	private MediaUrlService mediaUrlService;
	@Autowired
	private ScriptSupportService scriptSupportService;

	@Override
	public ResponseBase save(@Valid @RequestBody ScriptSaveQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		Date now = new Date();
		int submit = query.getSubmit() == null ? 1 : query.getSubmit();
		ScriptEntity entity;
		if (query.getId() == null) {
			entity = new ScriptEntity();
			applyBasic(entity, query);
			entity.setAuthorId(account.getId());
			entity.setAuditStatus(submit == 0 ? ScriptAuditStatusEnums.DRAFT.getCode()
					: ScriptAuditStatusEnums.PENDING.getCode());
			entity.setShelfStatus(ScriptShelfStatusEnums.OFF.getCode());
			entity.setSaleStatus(ScriptSaleStatusEnums.ON_SALE.getCode());
			entity.setScore(BigDecimal.ZERO);
			entity.setHeatScore(0L);
			entity.setSortWeight(0);
			entity.setVersion(0);
			entity.setDeleteState(0);
			entity.setSetTime(now);
			entity.setGmtModified(now);
			scriptDao.insert(entity);
			scriptStatDao.initIfAbsent(entity.getId());
			scriptSupportService.writeAuditLog(entity.getId(), ScriptConstants.LOG_ACTION_CREATE,
					null, entity.getAuditStatus(), null, entity.getShelfStatus(),
					account.getId(), ScriptConstants.OPERATOR_CREATOR, "创建剧本");
		} else {
			entity = scriptDao.findByIdIncludeDeleted(query.getId());
			if (entity == null) {
				return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
			}
			if (!account.getId().equals(entity.getAuthorId())) {
				return setResultError(I18nUtil.getMessage("purview_error_null"));
			}
			Integer fromAudit = entity.getAuditStatus();
			applyBasic(entity, query);
			entity.setAuditStatus(submit == 0 ? ScriptAuditStatusEnums.DRAFT.getCode()
					: ScriptAuditStatusEnums.PENDING.getCode());
			entity.setAuditRejectReason(null);
			entity.setGmtModified(now);
			entity.setVersion((entity.getVersion() == null ? 0 : entity.getVersion()) + 1);
			scriptDao.updateById(entity);
			scriptSupportService.writeAuditLog(entity.getId(), ScriptConstants.LOG_ACTION_EDIT,
					fromAudit, entity.getAuditStatus(), null, entity.getShelfStatus(),
					account.getId(), ScriptConstants.OPERATOR_CREATOR, "编辑剧本");
		}

		saveChildren(entity.getId(), query);
		Map<String, Object> data = new HashMap<>();
		data.put("id", entity.getId());
		log.info("creator script saved creatorId={} scriptId={} submit={}", account.getId(), entity.getId(), submit);
		return setResultSuccess(data);
	}

	@Override
	public ResponseBase findList(CreatorScriptListQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query == null) {
			query = new CreatorScriptListQuery();
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<CreatorScriptListRespEntity> list = scriptDao.findCreatorList(query, account.getId());
		if (list != null) {
			for (CreatorScriptListRespEntity item : list) {
				item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
				item.setSaleStatusText(scriptSupportService.saleStatusText(item.getSaleStatus()));
				item.setAuditStatusText(scriptSupportService.auditStatusText(item.getAuditStatus()));
				item.setShelfStatusText(scriptSupportService.shelfStatusText(item.getShelfStatus()));
			}
		}
		return setResultSuccess(new PageInfo<>(list));
	}

	@Override
	public ResponseBase findInfo(@RequestParam("id") Integer id, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		ScriptEntity entity = scriptDao.findByIdIncludeDeleted(id);
		if (entity == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		if (!account.getId().equals(entity.getAuthorId())) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		CreatorScriptInfoRespEntity resp = new CreatorScriptInfoRespEntity();
		scriptSupportService.fillDetail(resp, entity, null);
		resp.setAuditStatus(entity.getAuditStatus());
		resp.setAuditStatusText(scriptSupportService.auditStatusText(entity.getAuditStatus()));
		resp.setAuditRejectReason(entity.getAuditRejectReason());
		resp.setShelfStatus(entity.getShelfStatus());
		resp.setShelfStatusText(scriptSupportService.shelfStatusText(entity.getShelfStatus()));
		resp.setOfflineReason(entity.getOfflineReason());
		resp.setAttachmentUrl(mediaUrlService.sign(entity.getAttachmentKey()));
		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase shelf(@Valid @RequestBody ScriptShelfQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		ScriptEntity entity = scriptDao.findByIdIncludeDeleted(query.getId());
		if (entity == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		if (!account.getId().equals(entity.getAuthorId())) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		if (query.getShelfStatus() != ScriptShelfStatusEnums.ON.getCode()
				&& query.getShelfStatus() != ScriptShelfStatusEnums.OFF.getCode()) {
			return setResultError(Constants.HTTP_RES_CODE_400, "上架状态不合法");
		}
		if (query.getShelfStatus() == ScriptShelfStatusEnums.ON.getCode()
				&& !ScriptAuditStatusEnums.isApproved(entity.getAuditStatus())) {
			return setResultError(Constants.HTTP_RES_CODE_400, "审核通过后才能上架");
		}
		int fromShelf = entity.getShelfStatus() == null ? ScriptShelfStatusEnums.OFF.getCode() : entity.getShelfStatus();
		int version = entity.getVersion() == null ? 0 : entity.getVersion();
		Date shelfTime = query.getShelfStatus() == ScriptShelfStatusEnums.ON.getCode() ? new Date() : null;
		int rows = scriptDao.casShelf(entity.getId(), fromShelf, query.getShelfStatus(), shelfTime,
				query.getShelfStatus() == ScriptShelfStatusEnums.OFF.getCode() ? query.getReason() : null, version);
		if (rows <= 0) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		scriptSupportService.writeAuditLog(entity.getId(), ScriptConstants.LOG_ACTION_SHELF,
				null, null, fromShelf, query.getShelfStatus(),
				account.getId(), ScriptConstants.OPERATOR_CREATOR, query.getReason());
		return setResultSuccess(true);
	}

	@Override
	public ResponseBase delete(@Valid @RequestBody ScriptIdQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		ScriptEntity entity = scriptDao.findByIdIncludeDeleted(query.getId());
		if (entity == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		if (!account.getId().equals(entity.getAuthorId())) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		if (scriptTradeApplyDao.countPendingByScript(entity.getId()) > 0) {
			return setResultError(Constants.HTTP_RES_CODE_400, "存在待处理申请，暂不能删除");
		}
		int version = entity.getVersion() == null ? 0 : entity.getVersion();
		int rows = scriptDao.softDelete(entity.getId(), version);
		if (rows <= 0) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		return setResultSuccess(true);
	}

	@Override
	public ResponseBase applyList(CreatorScriptApplyListQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (query == null) {
			query = new CreatorScriptApplyListQuery();
		}
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<CreatorScriptApplyItemRespEntity> rows = scriptTradeApplyDao.findApplyList(query, account.getId());
		List<ScriptTradeApplyRespEntity> list = new ArrayList<>();
		if (rows != null) {
			for (CreatorScriptApplyItemRespEntity row : rows) {
				ScriptTradeApplyRespEntity item = new ScriptTradeApplyRespEntity();
				item.setApplyId(row.getApplyId());
				item.setApplyNo(row.getApplyNo());
				item.setScriptId(row.getScriptId());
				item.setScriptName(row.getScriptName());
				item.setBuyerName(row.getBuyerName());
				item.setContactName(row.getContactName());
				String mobile = scriptSupportService.decryptMobile(row.getContactMobileCipher());
				item.setContactMobile(scriptSupportService.maskMobile(mobile));
				item.setApplyType(row.getApplyType());
				item.setApplyTypeText(scriptSupportService.applyTypeText(row.getApplyType()));
				item.setOfferPrice(row.getOfferPrice());
				item.setMessage(row.getMessage());
				item.setApplyStatus(row.getApplyStatus());
				item.setApplyStatusText(scriptSupportService.applyStatusText(row.getApplyStatus()));
				item.setSetTime(row.getSetTime());
				list.add(item);
			}
		}
		return setResultSuccess(new PageInfo<>(list));
	}

	@Override
	public ResponseBase applyHandle(@Valid @RequestBody ScriptApplyHandleQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		// 查询剧本交易申请
		ScriptTradeApplyEntity apply = scriptTradeApplyDao.selectById(query.getApplyId());
		if (apply == null) {
			return setResultError(Constants.HTTP_RES_CODE_404, I18nUtil.getMessage("base_data_null"));
		}
		if (!account.getId().equals(apply.getAuthorId())) {
			return setResultError(I18nUtil.getMessage("purview_error_null"));
		}
		if (apply.getApplyStatus() == null
				|| apply.getApplyStatus() != ScriptTradeApplyStatusEnums.PENDING.getCode()) {
			return setResultError(Constants.HTTP_RES_CODE_400, "该申请已处理");
		}

		if (query.getAction() != ScriptConstants.HANDLE_AGREE && query.getAction() != ScriptConstants.HANDLE_REJECT) {
			return setResultError(Constants.HTTP_RES_CODE_400, "处理动作不合法");
		}

		int toStatus = query.getAction() == ScriptConstants.HANDLE_AGREE
				? ScriptTradeApplyStatusEnums.AGREED.getCode()
				: ScriptTradeApplyStatusEnums.REJECTED.getCode();
		// 修改剧本市场交易状态
		int rows = scriptTradeApplyDao.updateStatusCas(apply.getId(),
				ScriptTradeApplyStatusEnums.PENDING.getCode(), toStatus, query.getReply());
		if (rows <= 0) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}

		// 查询剧本信息
		ScriptEntity script = scriptDao.selectById(apply.getScriptId());
		if (script != null) {
			int version = script.getVersion() == null ? 0 : script.getVersion();
			int fromSale = script.getSaleStatus() == null ? ScriptSaleStatusEnums.ON_SALE.getCode() : script.getSaleStatus();
			if (query.getAction() == ScriptConstants.HANDLE_AGREE) {
				// 修改剧本售卖状态
				scriptDao.casSaleStatus(script.getId(), fromSale, ScriptSaleStatusEnums.SIGNING.getCode(), version);
				// 修改剧本市场状态
				scriptTradeApplyDao.expireOtherPending(script.getId(), apply.getId());
			} else if (scriptTradeApplyDao.countPendingByScript(script.getId()) == 0) {
				// 修改剧本售卖状态
				scriptDao.casSaleStatus(script.getId(), fromSale, ScriptSaleStatusEnums.ON_SALE.getCode(), version);
			}
		}
		log.info("creator script apply handled creatorId={} applyId={} action={}",
				account.getId(), apply.getId(), query.getAction());
		return setResultSuccess(true);
	}

	private void applyBasic(ScriptEntity entity, ScriptSaveQuery query) {
		entity.setScriptName(query.getTitle().trim());
		entity.setCoverUrl(query.getCoverKey());
		entity.setThemePartition(query.getChannel());
		entity.setTopic(query.getTopic() == null ? "" : query.getTopic().trim());
		entity.setLicenseType(query.getLicenseType() == null
				? com.playlet.internal.enums.ScriptLicenseTypeEnums.NORMAL.getCode() : query.getLicenseType());
		entity.setLicenseScope(query.getLicenseScope());
		entity.setEpisodes(query.getEpisodes() == null ? 0 : query.getEpisodes());
		entity.setAvgMinutes(query.getAvgMinutes());
		entity.setPriceYuan(query.getPrice() == null ? BigDecimal.ZERO : query.getPrice());
		entity.setScriptType(query.getScriptType() == null
				? com.playlet.internal.enums.ScriptTypeEnums.REAL.getCode() : query.getScriptType());
		entity.setFinishState(query.getFinishState() == null
				? com.playlet.internal.enums.ScriptFinishStateEnums.FINISHED.getCode() : query.getFinishState());
		entity.setDescription(query.getSummary());
		entity.setCorePoint(query.getCorePoint());
		entity.setOneLine(query.getOneLine());
		entity.setBenchmarkWorks(query.getBenchmarkWorks());
		entity.setAttachmentKey(query.getAttachmentKey());
	}

	private void saveChildren(Integer scriptId, ScriptSaveQuery query) {
		// 人物
		scriptCharacterDao.deleteByScriptId(scriptId);
		if (query.getCharacters() != null && !query.getCharacters().isEmpty()) {
			List<ScriptCharacterEntity> list = new ArrayList<>();
			int index = 0;
			for (ScriptSaveQuery.CharacterItem item : query.getCharacters()) {
				if (item == null || item.getName() == null || item.getName().trim().isEmpty()) {
					continue;
				}
				ScriptCharacterEntity entity = new ScriptCharacterEntity();
				entity.setScriptId(scriptId);
				entity.setCharName(item.getName().trim());
				entity.setCharRole(item.getRole() == null ? 9 : item.getRole());
				entity.setAvatarUrl(item.getAvatarUrl());
				entity.setDescription(item.getDescription());
				entity.setSortOrder(index++);
				list.add(entity);
			}
			if (!list.isEmpty()) {
				scriptCharacterDao.batchInsert(list);
			}
		}

		// 分集
		scriptEpisodeDao.deleteByScriptId(scriptId);
		if (query.getEpisodeList() != null && !query.getEpisodeList().isEmpty()) {
			List<ScriptEpisodeEntity> list = new ArrayList<>();
			int index = 1;
			for (ScriptSaveQuery.EpisodeItem item : query.getEpisodeList()) {
				if (item == null) {
					continue;
				}
				ScriptEpisodeEntity entity = new ScriptEpisodeEntity();
				entity.setScriptId(scriptId);
				entity.setEpisodeNo(item.getNo() == null ? index : item.getNo());
				entity.setEpisodeTitle(item.getTitle() == null ? "" : item.getTitle());
				entity.setDurationSeconds(item.getDurationSeconds());
				list.add(entity);
				index++;
			}
			if (!list.isEmpty()) {
				scriptEpisodeDao.batchInsert(list);
			}
		}

		// 试读
		scriptPreviewDao.deleteByScriptId(scriptId);
		if (query.getPreviews() != null && !query.getPreviews().isEmpty()) {
			List<ScriptPreviewEntity> list = new ArrayList<>();
			int index = 1;
			for (ScriptSaveQuery.PreviewItem item : query.getPreviews()) {
				if (item == null) {
					continue;
				}
				ScriptPreviewEntity entity = new ScriptPreviewEntity();
				entity.setScriptId(scriptId);
				entity.setEpisodeNo(item.getEpisodeNo() == null ? 1 : item.getEpisodeNo());
				entity.setSceneNo(item.getSceneNo() == null ? index : item.getSceneNo());
				entity.setSceneTitle(item.getSceneTitle());
				entity.setContentJson(item.getLines() == null ? "[]" : JSON.toJSONString(item.getLines()));
				list.add(entity);
				index++;
			}
			if (!list.isEmpty()) {
				scriptPreviewDao.batchInsert(list);
			}
		}

		// 标签
		scriptTagRelDao.deleteByScriptId(scriptId);
		if (query.getTags() != null && !query.getTags().isEmpty()) {
			Set<String> names = new LinkedHashSet<>();
			for (String tag : query.getTags()) {
				if (tag != null && !tag.trim().isEmpty()) {
					names.add(tag.trim());
				}
			}
			List<ScriptTagRelEntity> rels = new ArrayList<>();
			int order = 0;
			for (String name : names) {
				scriptTagDao.insertIgnore(ScriptConstants.TAG_GROUP_ELEMENT, ScriptConstants.LANGUE, name);
				ScriptTagEntity tag = scriptTagDao.findByName(ScriptConstants.TAG_GROUP_ELEMENT,
						ScriptConstants.LANGUE, name);
				if (tag == null) {
					continue;
				}
				ScriptTagRelEntity rel = new ScriptTagRelEntity();
				rel.setScriptId(scriptId);
				rel.setTagId(tag.getId());
				rel.setSortOrder(order++);
				rels.add(rel);
			}
			if (!rels.isEmpty()) {
				scriptTagRelDao.batchInsert(rels);
			}
		}
	}
}
