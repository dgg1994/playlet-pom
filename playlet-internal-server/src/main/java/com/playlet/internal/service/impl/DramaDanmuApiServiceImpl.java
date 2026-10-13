package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.DanmuDeleteRequest;
import com.playlet.internal.api.request.DanmuListRequest;
import com.playlet.internal.api.request.DanmuSendRequest;
import com.playlet.internal.api.request.SensitiveRecordEntity;
import com.playlet.internal.api.response.DanmuItemResp;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.DanmuConstants;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDanmuDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaDanmuEntity;
import com.playlet.internal.enums.DanmuModeEnums;
import com.playlet.internal.enums.DanmuStatusEnums;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.SensitiveSourceEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.security.sensitive.SensitiveDecision;
import com.playlet.internal.service.DramaDanmuApiService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.SensitiveRecordService;
import com.playlet.internal.service.SensitiveWordService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.HtmlSanitizeUtils;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * C端弹幕一期：发送 / 时间窗列表 / 自删。
 */
@Slf4j
@RestController
@CrossOrigin
public class DramaDanmuApiServiceImpl implements DramaDanmuApiService {

	/** 颜色：#RGB 或 #RRGGBB */
	private static final Pattern COLOR_PATTERN = Pattern.compile("^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$");

	@Autowired
	private DramaDanmuDao dramaDanmuDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private SensitiveWordService sensitiveWordService;
	@Autowired
	private SensitiveRecordService sensitiveRecordService;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase send(@RequestBody DanmuSendRequest request, HttpServletRequest httpRequest) {
        try {
            Integer uid = AppTokenUtil.resolveUid(httpRequest);
            if (uid == null) {
                return setResultError(I18nUtil.getMessage("login_required"));
            }
            if (request == null || request.getVideoId() == null || request.getPlayTime() == null
                    || StringUtils.isBlank(request.getContent())) {
                return setResultError(I18nUtil.getMessage("param_not_null"));
            }

            String content = HtmlSanitizeUtils.plain(request.getContent().trim());
            if (StringUtils.isBlank(content)) {
                return setResultError(I18nUtil.getMessage("param_not_null"));
            }
            if (content.length() > DanmuConstants.CONTENT_MAX_LEN) {
                return setResultError(I18nUtil.getMessage("danmu_content_too_long"));
            }

            // 仅已上架集可发弹幕
            DramaAssetEntity asset = dramaAssetDao.findOnShelfById(request.getVideoId());
            if (asset == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            if (!isPlayTimeValid(request.getPlayTime(), asset.getDurationSeconds())) {
                return setResultError(I18nUtil.getMessage("danmu_play_time_invalid"));
            }

            Integer mode = resolveMode(request.getMode());
            if (mode == null) {
                return setResultError(I18nUtil.getMessage("param_not_null"));
            }
            Integer fontSize = resolveFontSize(request.getFontSize());
            if (fontSize == null) {
                return setResultError(I18nUtil.getMessage("param_not_null"));
            }
            String color = resolveColor(request.getColor());
            if (color == null) {
                return setResultError(I18nUtil.getMessage("param_not_null"));
            }

            // 同一用户同一集每分钟限流
            Date afterTime = minutesAgo(1);
            int recentCount = dramaDanmuDao.countByUidAndVideo(uid, request.getVideoId(), afterTime);
            if (recentCount >= DanmuConstants.SEND_RATE_LIMIT_PER_MINUTE) {
                log.info("danmu send rate limit uid={} videoId={} count={}", uid, request.getVideoId(), recentCount);
                return setResultError(I18nUtil.getMessage("danmu_rate_limit"));
            }

            SensitiveDecision decision = sensitiveWordService.decide(content);
            if (decision.isReject()) {
                sensitiveRecordService.saveRecord(new SensitiveRecordEntity(
                        null, uid, asset.getDramaId(), request.getVideoId(),
                        content, SensitiveSourceEnums.DANMU.getCode()), decision.getCheck());
                return setResultError(I18nUtil.getMessage("sensitive_forbidden"));
            }

            DramaDanmuEntity entity = new DramaDanmuEntity();
            entity.setDramaId(asset.getDramaId());
            entity.setVideoId(request.getVideoId());
            entity.setUid(uid);
            entity.setContent(decision.getContent());
            entity.setPlayTime(request.getPlayTime());
            entity.setColor(color);
            entity.setMode(mode);
            entity.setFontSize(fontSize);
            // 隐藏：status=HIDDEN，不软删，列表按 status=NORMAL 过滤
            entity.setStatus(decision.isHidden()
                    ? DanmuStatusEnums.HIDDEN.getIndex()
                    : DanmuStatusEnums.NORMAL.getIndex());
            entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
            Date now = new Date();
            entity.setSetTime(now);
            entity.setGmtModified(now);
			dramaDanmuDao.insert(entity);

            if (decision.shouldRecord()) {
                Integer recordId = toRecordCommentId(entity.getId());
                sensitiveRecordService.saveRecord(new SensitiveRecordEntity(
                        recordId, uid, asset.getDramaId(), request.getVideoId(),
                        entity.getContent(), SensitiveSourceEnums.DANMU.getCode()), decision.getCheck());
            }
            if (decision.isHidden()) {
                log.info("danmu send hidden uid={} videoId={} id={}", uid, request.getVideoId(), entity.getId());
                return setResultSuccess(I18nUtil.getMessage("sensitive_under_review"));
            }
            log.info("danmu send ok uid={} videoId={} id={} playTime={}",
                    uid, request.getVideoId(), entity.getId(), request.getPlayTime());
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public ResponseBase list(@RequestBody DanmuListRequest request) {
		if (request == null || request.getVideoId() == null
				|| request.getFromTime() == null || request.getToTime() == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		int from = request.getFromTime();
		int to = request.getToTime();
		if (from < 0 || to < 0 || from > to) {
			return setResultError(I18nUtil.getMessage("danmu_play_time_invalid"));
		}
		// 限制单次时间窗，避免全片一次拉光
		if (to - from > DanmuConstants.LIST_MAX_WINDOW_SEC) {
			return setResultError(I18nUtil.getMessage("danmu_play_time_invalid"));
		}

		List<DramaDanmuEntity> rows = dramaDanmuDao.findByVideoAndPlayTimeRange(
				request.getVideoId(), from, to,
				DanmuStatusEnums.NORMAL.getIndex(),
				DeleteStateEnum.NORMAL.getIndex(),
				DanmuConstants.LIST_WINDOW_LIMIT);

		List<DanmuItemResp> list = new ArrayList<>();
		if (rows != null) {
			for (DramaDanmuEntity row : rows) {
				String avatar = row.getAvatar() == null ? null : mediaUrlService.sign(row.getAvatar());
				list.add(toItemResp(row, row.getNickname(), avatar));
			}
		}
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase delete(@RequestBody DanmuDeleteRequest request, HttpServletRequest httpRequest) {
        try {
            Integer uid = AppTokenUtil.resolveUid(httpRequest);
            if (uid == null) {
                return setResultError(I18nUtil.getMessage("login_required"));
            }
            if (request == null || request.getId() == null) {
                return setResultError(I18nUtil.getMessage("param_not_null"));
            }

            DramaDanmuEntity exist = dramaDanmuDao.findByIdAndDeleteState(
                    request.getId(), DeleteStateEnum.NORMAL.getIndex());
            if (exist == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            if (!uid.equals(exist.getUid())) {
                log.info("danmu delete not owner uid={} id={} owner={}", uid, request.getId(), exist.getUid());
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }

			int updated = dramaDanmuDao.softDeleteByIdAndUid(
					request.getId(), uid,
					DanmuStatusEnums.USER_DELETED.getIndex(),
					DeleteStateEnum.DELETE.getIndex());
			if (updated <= 0) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}

            log.info("danmu delete ok uid={} id={}", uid, request.getId());
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
			throw new BaseException("操作失败", e);
        }
    }

	/** playTime 合法：>=0；有时长时不超过时长 */
	private static boolean isPlayTimeValid(Integer playTime, Integer durationSeconds) {
		if (playTime == null || playTime < 0) {
			return false;
		}
		if (durationSeconds != null && durationSeconds > 0 && playTime > durationSeconds) {
			return false;
		}
		return true;
	}

	private static Integer resolveMode(Integer mode) {
		if (mode == null) {
			return DanmuModeEnums.SCROLL.getIndex();
		}
		for (DanmuModeEnums item : DanmuModeEnums.values()) {
			if (item.getIndex().equals(mode)) {
				return mode;
			}
		}
		return null;
	}

	private static Integer resolveFontSize(Integer fontSize) {
		if (fontSize == null) {
			return DanmuConstants.FONT_SIZE_NORMAL;
		}
		if (DanmuConstants.FONT_SIZE_NORMAL == fontSize || DanmuConstants.FONT_SIZE_LARGE == fontSize) {
			return fontSize;
		}
		return null;
	}

	/** 空则默认白；非法格式返回 null */
	private static String resolveColor(String color) {
		if (StringUtils.isBlank(color)) {
			return DanmuConstants.DEFAULT_COLOR;
		}
		String trimmed = color.trim();
		if (!COLOR_PATTERN.matcher(trimmed).matches()) {
			return null;
		}
		return trimmed.toUpperCase();
	}

	private static Date minutesAgo(int minutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -minutes);
		return calendar.getTime();
	}

	/** SensitiveRecord.commentId 为 Integer，弹幕 id 过大时记 null */
	private static Integer toRecordCommentId(Long id) {
		if (id == null || id > Integer.MAX_VALUE || id < Integer.MIN_VALUE) {
			return null;
		}
		return id.intValue();
	}

	private static DanmuItemResp toItemResp(DramaDanmuEntity entity, String nickname, String avatar) {
		DanmuItemResp resp = new DanmuItemResp();
		resp.setId(entity.getId());
		resp.setUid(entity.getUid());
		resp.setNickname(nickname);
		resp.setAvatar(avatar);
		resp.setContent(entity.getContent());
		resp.setPlayTime(entity.getPlayTime());
		resp.setColor(entity.getColor());
		resp.setMode(entity.getMode());
		resp.setFontSize(entity.getFontSize());
		return resp;
	}
}
