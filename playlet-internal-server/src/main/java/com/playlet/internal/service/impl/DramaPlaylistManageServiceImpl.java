package com.playlet.internal.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.DramaPlaylistRequestEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.drama.DramaPlaylistDao;
import com.playlet.internal.dao.drama.DramaPlaylistTagDao;
import com.playlet.internal.entity.drama.DramaPlaylistEntity;
import com.playlet.internal.entity.drama.DramaPlaylistTagEntity;
import com.playlet.internal.enums.PlaylistMatchModeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.DramaPlaylistManageService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 管理端剧单 CRUD。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class DramaPlaylistManageServiceImpl extends BaseApiService implements DramaPlaylistManageService {

	private static final int DEFAULT_TOP_N = 10;
	private static final int DEFAULT_SORT_WEIGHT = 0;
	private static final int STATUS_ON = 1;
	private static final int STATUS_OFF = 0;

	@Autowired
	private DramaPlaylistDao dramaPlaylistDao;
	@Autowired
	private DramaPlaylistTagDao dramaPlaylistTagDao;

	@Override
	@SysLogAnnotation(module = "剧单管理", type = "POST", remark = "剧单列表")
	public ResponseBase findList(@RequestBody DramaPlaylistEntity entity) {
		if (entity == null) {
			entity = new DramaPlaylistEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<DramaPlaylistEntity> list = dramaPlaylistDao.findAdminList(entity);
		if (list != null) {
			for (DramaPlaylistEntity row : list) {
				fillTagGroupIds(row);
			}
		}
		return setResultSuccess(new PageInfo<>(list == null ? new ArrayList<>() : list),
				I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "剧单管理", type = "GET", remark = "剧单详情")
	public ResponseBase detail(@RequestParam("id") Integer id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		DramaPlaylistEntity row = dramaPlaylistDao.selectById(id);
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		fillTagGroupIds(row);
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "剧单管理", type = "POST", remark = "新增剧单")
	public ResponseBase save(@RequestBody DramaPlaylistRequestEntity entity) {
		try {
			if (entity == null || entity.getPlaylists() == null || entity.getPlaylists().isEmpty()) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			List<String> tagGroupIds = normalizeTagGroupIds(entity.getTagGroupIds());
			if (tagGroupIds.isEmpty()) {
				return setResultError("请至少绑定一个剧标签");
			}
			Integer matchMode = entity.getMatchMode() == null
					? PlaylistMatchModeEnums.OR.getIndex() : entity.getMatchMode();
			if (!PlaylistMatchModeEnums.isValid(matchMode)) {
				return setResultError("matchMode 仅支持 1=OR / 2=AND");
			}
			int topN = entity.getTopN() == null ? DEFAULT_TOP_N : entity.getTopN();
			int sortWeight = entity.getSortWeight() == null ? DEFAULT_SORT_WEIGHT : entity.getSortWeight();
			int status = entity.getStatus() == null ? STATUS_ON : entity.getStatus();
			if (status != STATUS_ON && status != STATUS_OFF) {
				return setResultError("状态仅支持 0停用 / 1启用");
			}

			String groupId = IdUtil.simpleUUID();
			for (DramaPlaylistEntity row : entity.getPlaylists()) {
				if (row == null || StringUtils.isEmpty(row.getPlaylistName())) {
					return setResultError(I18nUtil.getMessage("base_error"));
				}
				String langue = StringUtils.isEmpty(row.getLangue())
						? LanguageContext.getLanguage() : row.getLangue().trim();
				String name = row.getPlaylistName().trim();
				if (dramaPlaylistDao.findByNameAndLangue(name, langue) != null) {
					return setResultError(I18nUtil.getMessage("base_info_exist"));
				}
				DramaPlaylistEntity insert = new DramaPlaylistEntity();
				insert.setGroupId(groupId);
				insert.setLangue(langue);
				insert.setPlaylistName(name);
				insert.setCoverUrl(firstNonEmpty(row.getCoverUrl(), entity.getCoverUrl()));
				insert.setMatchMode(matchMode);
				insert.setTopN(topN);
				insert.setSortWeight(sortWeight);
				insert.setStatus(status);
				insert.setRemark(firstNonEmpty(row.getRemark(), entity.getRemark()));
				GenericityUtil.setDate(insert);
				dramaPlaylistDao.insert(insert);
			}
			replaceTags(groupId, tagGroupIds);
			log.info("drama playlist save groupId={} tagCount={}", groupId, tagGroupIds.size());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("drama playlist save biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("drama playlist save failed", e);
			throw new BaseException("剧单新增失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "剧单管理", type = "POST", remark = "编辑剧单")
	public ResponseBase update(@RequestBody DramaPlaylistRequestEntity entity) {
		try {
			if (entity == null || StringUtils.isEmpty(entity.getGroupId())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			String groupId = entity.getGroupId().trim();
			List<DramaPlaylistEntity> existRows = dramaPlaylistDao.selectByGroupId(groupId);
			if (existRows == null || existRows.isEmpty()) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}

			Integer matchMode = entity.getMatchMode();
			if (matchMode != null && !PlaylistMatchModeEnums.isValid(matchMode)) {
				return setResultError("matchMode 仅支持 1=OR / 2=AND");
			}
			Integer status = entity.getStatus();
			if (status != null && status != STATUS_ON && status != STATUS_OFF) {
				return setResultError("状态仅支持 0停用 / 1启用");
			}

			List<DramaPlaylistEntity> playlists = entity.getPlaylists();
			if (playlists != null && !playlists.isEmpty()) {
				for (DramaPlaylistEntity row : playlists) {
					if (row == null || StringUtils.isEmpty(row.getLangue())) {
						return setResultError(I18nUtil.getMessage("base_error"));
					}
					String langue = row.getLangue().trim();
					DramaPlaylistEntity db = dramaPlaylistDao.selectOne(
							new QueryWrapper<DramaPlaylistEntity>()
									.eq("group_id", groupId)
									.eq("langue", langue));
					if (db != null) {
						if (StringUtils.isNotEmpty(row.getPlaylistName())) {
							String name = row.getPlaylistName().trim();
							DramaPlaylistEntity sameName = dramaPlaylistDao.findByNameAndLangue(name, langue);
							if (sameName != null && !sameName.getId().equals(db.getId())) {
								return setResultError(I18nUtil.getMessage("base_info_exist"));
							}
							db.setPlaylistName(name);
						}
						applySharedFields(db, entity, row);
						db.setGmtModified(new Date());
						dramaPlaylistDao.updateById(db);
					} else {
						if (StringUtils.isEmpty(row.getPlaylistName())) {
							return setResultError(I18nUtil.getMessage("base_error"));
						}
						String name = row.getPlaylistName().trim();
						if (dramaPlaylistDao.findByNameAndLangue(name, langue) != null) {
							return setResultError(I18nUtil.getMessage("base_info_exist"));
						}
						DramaPlaylistEntity insert = new DramaPlaylistEntity();
						insert.setGroupId(groupId);
						insert.setLangue(langue);
						insert.setPlaylistName(name);
						insert.setCoverUrl(firstNonEmpty(row.getCoverUrl(), entity.getCoverUrl()));
						insert.setMatchMode(matchMode != null ? matchMode
								: existRows.get(0).getMatchMode());
						insert.setTopN(entity.getTopN() != null ? entity.getTopN()
								: existRows.get(0).getTopN());
						insert.setSortWeight(entity.getSortWeight() != null ? entity.getSortWeight()
								: existRows.get(0).getSortWeight());
						insert.setStatus(status != null ? status : existRows.get(0).getStatus());
						insert.setRemark(firstNonEmpty(row.getRemark(), entity.getRemark()));
						GenericityUtil.setDate(insert);
						dramaPlaylistDao.insert(insert);
					}
				}
			} else {
				// 仅改共享字段时，更新该 group 下全部语言行
				for (DramaPlaylistEntity db : existRows) {
					applySharedFields(db, entity, null);
					db.setGmtModified(new Date());
					dramaPlaylistDao.updateById(db);
				}
			}

			// 传入 tagGroupIds 则全量替换；未传保留原绑定
			if (entity.getTagGroupIds() != null) {
				List<String> tagGroupIds = normalizeTagGroupIds(entity.getTagGroupIds());
				if (tagGroupIds.isEmpty()) {
					return setResultError("请至少绑定一个剧标签");
				}
				replaceTags(groupId, tagGroupIds);
			}
			log.info("drama playlist update groupId={}", groupId);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("drama playlist update biz error groupId={}",
					entity == null ? null : entity.getGroupId(), e);
			throw e;
		} catch (Exception e) {
			log.error("drama playlist update failed groupId={}",
					entity == null ? null : entity.getGroupId(), e);
			throw new BaseException("剧单编辑失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "剧单管理", type = "POST", remark = "变更剧单状态")
	public ResponseBase changeStatus(@RequestBody DramaPlaylistEntity entity) {
		try {
			if (entity == null || StringUtils.isEmpty(entity.getGroupId()) || entity.getStatus() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (entity.getStatus() != STATUS_ON && entity.getStatus() != STATUS_OFF) {
				return setResultError("状态仅支持 0停用 / 1启用");
			}
			List<DramaPlaylistEntity> exist = dramaPlaylistDao.selectByGroupId(entity.getGroupId());
			if (exist == null || exist.isEmpty()) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			dramaPlaylistDao.updateStatusByGroupId(entity.getGroupId(), entity.getStatus());
			log.info("drama playlist changeStatus groupId={} status={}",
					entity.getGroupId(), entity.getStatus());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("drama playlist changeStatus biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("drama playlist changeStatus failed groupId={}",
					entity == null ? null : entity.getGroupId(), e);
			throw new BaseException("剧单状态变更失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "剧单管理", type = "GET", remark = "删除剧单")
	public ResponseBase delete(@RequestParam("id") Integer id) {
		try {
			if (id == null) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			DramaPlaylistEntity exist = dramaPlaylistDao.selectById(id);
			if (exist == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String groupId = exist.getGroupId();
			dramaPlaylistDao.deleteById(id);
			// 同 group 已无其它语言行时，清空标签关联
			long otherCount = dramaPlaylistDao.selectCount(
					new QueryWrapper<DramaPlaylistEntity>().eq("group_id", groupId));
			if (otherCount == 0 && StringUtils.isNotEmpty(groupId)) {
				dramaPlaylistTagDao.deleteByPlaylistGroupId(groupId);
			}
			log.info("drama playlist delete id={} groupId={} clearedTags={}",
					id, groupId, otherCount == 0);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("drama playlist delete biz error id={}", id, e);
			throw e;
		} catch (Exception e) {
			log.error("drama playlist delete failed id={}", id, e);
			throw new BaseException("剧单删除失败", e);
		}
	}

	private void fillTagGroupIds(DramaPlaylistEntity row) {
		if (row == null || StringUtils.isEmpty(row.getGroupId())) {
			return;
		}
		row.setTagGroupIds(dramaPlaylistTagDao.findTagGroupIds(row.getGroupId()));
	}

	/** 标签全量替换：先删后插，sort_weight 按传入顺序递减。 */
	private void replaceTags(String playlistGroupId, List<String> tagGroupIds) throws Exception {
		dramaPlaylistTagDao.deleteByPlaylistGroupId(playlistGroupId);
		int weight = tagGroupIds.size();
		Date now = new Date();
		for (String tagGroupId : tagGroupIds) {
			DramaPlaylistTagEntity tag = new DramaPlaylistTagEntity();
			tag.setPlaylistGroupId(playlistGroupId);
			tag.setTagGroupId(tagGroupId);
			tag.setSortWeight(weight--);
			tag.setSetTime(now);
			tag.setGmtModified(now);
			dramaPlaylistTagDao.insert(tag);
		}
	}

	private static List<String> normalizeTagGroupIds(List<String> raw) {
		Set<String> set = new LinkedHashSet<>();
		if (raw == null) {
			return new ArrayList<>();
		}
		for (String id : raw) {
			if (StringUtils.isNotEmpty(id)) {
				set.add(id.trim());
			}
		}
		return new ArrayList<>(set);
	}

	private static void applySharedFields(DramaPlaylistEntity db, DramaPlaylistRequestEntity req,
			DramaPlaylistEntity row) {
		if (req.getMatchMode() != null) {
			db.setMatchMode(req.getMatchMode());
		}
		if (req.getTopN() != null) {
			db.setTopN(req.getTopN());
		}
		if (req.getSortWeight() != null) {
			db.setSortWeight(req.getSortWeight());
		}
		if (req.getStatus() != null) {
			db.setStatus(req.getStatus());
		}
		String cover = row != null ? firstNonEmpty(row.getCoverUrl(), req.getCoverUrl()) : req.getCoverUrl();
		if (cover != null) {
			db.setCoverUrl(cover);
		}
		String remark = row != null ? firstNonEmpty(row.getRemark(), req.getRemark()) : req.getRemark();
		if (remark != null) {
			db.setRemark(remark);
		}
	}

	private static String firstNonEmpty(String a, String b) {
		if (StringUtils.isNotEmpty(a)) {
			return a;
		}
		return b;
	}
}
