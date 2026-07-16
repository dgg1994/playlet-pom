package com.playlet.internal.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.RankBoardRequestEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.RankBoardDao;
import com.playlet.internal.dao.drama.RankListDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.RankBoardEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import com.playlet.internal.service.RankManageService;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@Transactional
public class RankManageServiceImpl extends BaseApiService implements RankManageService {

	@Autowired
	private RankBoardDao rankBoardDao;
	@Autowired
	private RankListDao rankListDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "POST", remark = "榜定义列表")
	public ResponseBase boardFindList(@RequestBody RankBoardEntity entity) {
		if (entity == null) {
			entity = new RankBoardEntity();
		}
		if (StringUtils.isEmpty(entity.getLangue())) {
			entity.setLangue(LanguageContext.getLanguage());
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		return setResultSuccess(new PageInfo<>(rankBoardDao.findAdminList(entity)),
				I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "GET", remark = "榜定义详情")
	public ResponseBase boardDetail(@RequestParam Integer id) {
		RankBoardEntity board = rankBoardDao.selectById(id);
		if (board == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		return setResultSuccess(board, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "POST", remark = "新增榜定义")
	public ResponseBase boardSave(@RequestBody RankBoardRequestEntity entity) {
		try {
			if (entity == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
            List<RankBoardEntity> ranks = entity.getRanks();
            if (ranks == null || ranks.size() == 0){
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            String groupId = IdUtil.simpleUUID();
            for (RankBoardEntity rank : ranks) {
                if (StringUtils.isEmpty(rank.getLangue())) {
                    rank.setLangue(LanguageContext.getLanguage());
                }
                if (StringUtils.isEmpty(rank.getGroupId())) {
                    rank.setGroupId(groupId);
                }
                if (rankBoardDao.findByBoardNameAndLangue(rank.getBoardName().trim(), rank.getLangue()) != null) {
                    return setResultError(I18nUtil.getMessage("base_info_exist"));
                }
                rank.setBoardName(rank.getBoardName().trim());
                rank.setBoardType(entity.getBoardType() == null ? 2 : entity.getBoardType());
                rank.setTopN(entity.getTopN() == null ? 100 : entity.getTopN());
                rank.setStatus(rank.getStatus() == null ? 1 : rank.getStatus());
                rank.setSortWeight(entity.getSortWeight() == null ? 0 : entity.getSortWeight());
                GenericityUtil.setDate(rank);
                rankBoardDao.insert(rank);
            }
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "POST", remark = "变更榜状态")
	public ResponseBase boardChangeStatus(@RequestBody RankBoardEntity entity) {
        try {
            if (entity == null || entity.getGroupId() == null || entity.getStatus() == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            if (entity.getStatus() != 0 && entity.getStatus() != 1) {
                return setResultError("状态仅支持 0停用 / 1启用");
            }
            List<RankBoardEntity> exist = rankBoardDao.selectGroupId(entity.getGroupId());
            if (exist == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            rankBoardDao.updateByGroupId(entity.getGroupId(), entity.getStatus());
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "GET", remark = "删除榜定义")
	public ResponseBase boardDelete(@RequestParam Integer id) {
		RankBoardEntity exist = rankBoardDao.selectById(id);
		if (exist == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		// 同 group 下已无其它语言榜时，一并清空条目
		long otherCount = rankBoardDao.selectCount(
				new QueryWrapper<RankBoardEntity>().eq("group_id", exist.getGroupId()).ne("id", id));
		if (otherCount == 0 && StringUtils.isNotEmpty(exist.getGroupId())) {
			rankListDao.deleteByBoardGroupId(exist.getGroupId());
		}
		rankBoardDao.deleteById(id);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "POST", remark = "榜条目列表")
	public ResponseBase listFindList(@RequestBody RankListEntity entity) {
		if (entity == null) {
			entity = new RankListEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<RankListEntity> adminList = rankListDao.findAdminList(entity);
		return setResultSuccess(new PageInfo<>(adminList), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "GET", remark = "榜条目详情")
	public ResponseBase listDetail(@RequestParam Integer id) {
		RankListEntity row = rankListDao.selectById(id);
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "POST", remark = "新增榜条目")
	public ResponseBase listSave(@RequestBody RankListEntity entity) {
		try {
			if (entity == null || StringUtils.isEmpty(entity.getBoardGroupId())
					|| StringUtils.isEmpty(entity.getDramaId())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			RankBoardEntity board = rankBoardDao.findOneByGroupId(entity.getBoardGroupId());
			if (board == null) {
				return setResultError(I18nUtil.getMessage("rank_board_null"));
			}
			if (rankListDao.findByBoardAndDrama(entity.getBoardGroupId(), entity.getDramaId()) != null) {
				return setResultError(I18nUtil.getMessage("rank_drama_exist"));
			}
			if (entity.getRankNo() == null || entity.getRankNo() < 1) {
				return setResultError(I18nUtil.getMessage("rank_no_min"));
			}
			if (rankListDao.findByBoardAndRankNo(entity.getBoardGroupId(), entity.getRankNo()) != null) {
				return setResultError(I18nUtil.getMessage("rank_no_occupied"));
			}
			if (board.getTopN() != null && entity.getRankNo() > board.getTopN()) {
				return setResultError(I18nUtil.getMessage("rank_no_exceed_topn", board.getTopN()));
			}
			DramaEntity drama = dramaDao.findByDramaId(Integer.valueOf(entity.getDramaId()));
			if (drama == null) {
				return setResultError(I18nUtil.getMessage("drama_null"));
			}
			fillFromDrama(entity, drama);
			entity.setStatus(entity.getStatus() == null ? 1 : entity.getStatus());
			if (entity.getScore() == null) {
				entity.setScore(BigDecimal.ZERO);
			}
			GenericityUtil.setDate(entity);
			rankListDao.insert(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "POST", remark = "编辑榜条目")
	public ResponseBase listUpdate(@RequestBody RankListEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			RankListEntity exist = rankListDao.selectById(entity.getId());
			if (exist == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (entity.getRankNo() != null && !entity.getRankNo().equals(exist.getRankNo())) {
				if (entity.getRankNo() < 1) {
					return setResultError(I18nUtil.getMessage("rank_no_min"));
				}
				RankListEntity occupied = rankListDao.findByBoardAndRankNo(exist.getBoardGroupId(), entity.getRankNo());
				if (occupied != null && !occupied.getId().equals(exist.getId())) {
					int oldNo = exist.getRankNo();
					int newNo = entity.getRankNo();
					exist.setRankNo(-exist.getId());
					exist.setGmtModified(new Date());
					rankListDao.updateById(exist);
					occupied.setRankNo(oldNo);
					occupied.setGmtModified(new Date());
					rankListDao.updateById(occupied);
					exist.setRankNo(newNo);
				} else {
					exist.setRankNo(entity.getRankNo());
				}
			}
			if (StringUtils.isNotEmpty(entity.getDramaId()) && !entity.getDramaId().equals(exist.getDramaId())) {
				if (rankListDao.findByBoardAndDrama(exist.getBoardGroupId(), entity.getDramaId()) != null) {
					return setResultError(I18nUtil.getMessage("rank_drama_exist"));
				}
				DramaEntity drama = dramaDao.findByDramaId(Integer.valueOf(entity.getDramaId()));
				if (drama == null) {
					return setResultError(I18nUtil.getMessage("drama_null"));
				}
				exist.setDramaId(entity.getDramaId());
				fillFromDrama(exist, drama);
			} else if (entity.getTitle() != null || entity.getCoverUrl() != null
					|| entity.getHotScoreText() != null || entity.getTotalEpisodes() != null
					|| entity.getFinished() != null) {
				if (entity.getTitle() != null) {
					exist.setTitle(entity.getTitle());
				}
				if (entity.getCoverUrl() != null) {
					exist.setCoverUrl(entity.getCoverUrl());
				}
				if (entity.getHotScoreText() != null) {
					exist.setHotScoreText(entity.getHotScoreText());
				}
				if (entity.getTotalEpisodes() != null) {
					exist.setTotalEpisodes(entity.getTotalEpisodes());
				}
				if (entity.getFinished() != null) {
					exist.setFinished(entity.getFinished());
				}
			}
			if (entity.getScore() != null) {
				exist.setScore(entity.getScore());
			}
			if (entity.getRemark() != null) {
				exist.setRemark(entity.getRemark());
			}
			exist.setGmtModified(new Date());
			rankListDao.updateById(exist);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "榜单管理", type = "POST", remark = "变更条目状态")
	public ResponseBase listChangeStatus(@RequestBody RankListEntity entity) {
		try {
			if (entity == null || entity.getId() == null || entity.getStatus() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (entity.getStatus() != 0 && entity.getStatus() != 1) {
				return setResultError("状态仅支持 0停用 / 1启用");
			}
			RankListEntity exist = rankListDao.selectById(entity.getId());
			if (exist == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			exist.setStatus(entity.getStatus());
			exist.setGmtModified(new Date());
			rankListDao.updateById(exist);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void fillFromDrama(RankListEntity entity, DramaEntity drama) {
		if (StringUtils.isEmpty(entity.getTitle())) {
			entity.setTitle(drama.getDramaTitle());
		}
		if (StringUtils.isEmpty(entity.getHotScoreText())) {
			entity.setHotScoreText(drama.getHotScoreText());
		}
		if (entity.getTotalEpisodes() == null) {
			entity.setTotalEpisodes(drama.getTotalEpisodes());
		}
		if (entity.getFinished() == null) {
			entity.setFinished(drama.getFinishedState());
		}
		if (StringUtils.isEmpty(entity.getCoverUrl())) {
			entity.setCoverUrl(resolveCover(drama));
		}
		if (entity.getScore() == null && drama.getHotScore() != null) {
			entity.setScore(BigDecimal.valueOf(drama.getHotScore()));
		}
	}

	private String resolveCover(DramaEntity drama) {
		try {
			if (dramaAssetDao != null && drama.getId() != null) {
				DramaAssetEntity asset = dramaAssetDao.findEnabledByDramaId(drama.getId());
				if (asset != null && StringUtils.isNotEmpty(asset.getVideoUrl())) {
					return asset.getVideoUrl();
				}
			}
		} catch (Exception e) {
			log.debug("resolveCover: {}", e.getMessage());
		}
		return drama.getCoverUrl();
	}
}
