package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.TheaterHomeRespEntity;
import com.playlet.internal.api.response.TheaterRankBlockEntity;
import com.playlet.internal.api.response.TheaterRankPageRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.RankBoardDao;
import com.playlet.internal.dao.drama.RankListDao;
import com.playlet.internal.entity.drama.RankBoardEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import com.playlet.internal.service.TheaterService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
public class TheaterServiceImpl extends BaseApiService implements TheaterService {

	@Autowired
	private RankBoardDao rankBoardDao;
	@Autowired
	private RankListDao rankListDao;

	@Override
	public ResponseBase home() {
		TheaterHomeRespEntity resp = new TheaterHomeRespEntity();
		List<RankBoardEntity> boards = rankBoardDao.findEnabledList();
		if (boards != null) {
			for (RankBoardEntity board : boards) {
				List<RankListEntity> all = rankListDao.findEnabledByBoardCode(board.getBoardCode());
				int limit = board.getTopN() == null ? 10 : Math.min(10, board.getTopN());
				List<RankListEntity> preview = new ArrayList<>();
				if (all != null) {
					for (int i = 0; i < all.size() && i < limit; i++) {
						preview.add(all.get(i));
					}
				}
				TheaterRankBlockEntity block = new TheaterRankBlockEntity();
				block.setBoardCode(board.getBoardCode());
				block.setBoardName(board.getBoardName());
				block.setBoardType(board.getBoardType());
				block.setItems(preview);
				resp.getBlocks().add(block);
			}
		}
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase rankList() {
		List<RankBoardEntity> rankBoardEntities = rankBoardDao.selectList(null);
		return setResultSuccess(rankBoardEntities,I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase rank(@RequestParam(required = false) String boardCode,
			RankListEntity entity) {
		if (StringUtils.isEmpty(boardCode)) {
			return setResultError("boardCode 不能为空");
		}
		boardCode = boardCode.trim().toUpperCase();
		RankBoardEntity board = rankBoardDao.findByBoardCode(boardCode);
		if (board == null || board.getStatus() == null || board.getStatus() != 1) {
			return setResultError("榜单不存在或已停用");
		}
		if (entity == null) {
			entity = new RankListEntity();
		}
		entity.setBoardCode(boardCode);
		entity.setStatus(1);
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<RankListEntity> list = rankListDao.findAdminList(entity);
		PageInfo<RankListEntity> info = new PageInfo<>(list);
		TheaterRankPageRespEntity resp = new TheaterRankPageRespEntity();
		resp.setBoardCode(board.getBoardCode());
		resp.setBoardName(board.getBoardName());
		resp.setPage(info);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}
}
