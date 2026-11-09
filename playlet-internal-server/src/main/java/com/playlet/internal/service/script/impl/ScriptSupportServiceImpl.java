package com.playlet.internal.service.script.impl;

import com.alibaba.fastjson.JSON;
import com.playlet.internal.api.response.ScriptAuthorRespEntity;
import com.playlet.internal.api.response.ScriptMarketDetailRespEntity;
import com.playlet.internal.dao.account.UserFollowDao;
import com.playlet.internal.dao.script.ScriptAuditLogDao;
import com.playlet.internal.dao.script.ScriptCharacterDao;
import com.playlet.internal.dao.script.ScriptDao;
import com.playlet.internal.dao.script.ScriptEpisodeDao;
import com.playlet.internal.dao.script.ScriptFavoriteDao;
import com.playlet.internal.dao.script.ScriptPreviewDao;
import com.playlet.internal.dao.script.ScriptStatDao;
import com.playlet.internal.dao.script.ScriptTagRelDao;
import com.playlet.internal.dao.script.ScriptTradeApplyDao;
import com.playlet.internal.entity.script.ScriptAuditLogEntity;
import com.playlet.internal.entity.script.ScriptCharacterEntity;
import com.playlet.internal.entity.script.ScriptEntity;
import com.playlet.internal.entity.script.ScriptEpisodeEntity;
import com.playlet.internal.entity.script.ScriptPreviewEntity;
import com.playlet.internal.entity.script.ScriptStatEntity;
import com.playlet.internal.entity.script.ScriptTradeApplyEntity;
import com.playlet.internal.enums.ScriptAuditStatusEnums;
import com.playlet.internal.enums.ScriptChannelEnums;
import com.playlet.internal.enums.ScriptFinishStateEnums;
import com.playlet.internal.enums.ScriptLicenseTypeEnums;
import com.playlet.internal.enums.ScriptSaleStatusEnums;
import com.playlet.internal.enums.ScriptShelfStatusEnums;
import com.playlet.internal.enums.ScriptTradeApplyStatusEnums;
import com.playlet.internal.enums.ScriptTradeApplyTypeEnums;
import com.playlet.internal.enums.ScriptTypeEnums;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.script.ScriptSupportService;
import com.playlet.internal.utils.AesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 剧本市场公共能力实现。
 */
@Service
public class ScriptSupportServiceImpl implements ScriptSupportService {

	@Value("${script.contact.aes-key:}")
	private String aesKey;

	private final ScriptDao scriptDao;
	private final ScriptStatDao scriptStatDao;
	private final ScriptTagRelDao scriptTagRelDao;
	private final ScriptCharacterDao scriptCharacterDao;
	private final ScriptEpisodeDao scriptEpisodeDao;
	private final ScriptPreviewDao scriptPreviewDao;
	private final ScriptFavoriteDao scriptFavoriteDao;
	private final ScriptTradeApplyDao scriptTradeApplyDao;
	private final ScriptAuditLogDao scriptAuditLogDao;
	private final UserFollowDao userFollowDao;
	private final MediaUrlService mediaUrlService;

	public ScriptSupportServiceImpl(ScriptDao scriptDao,
			ScriptStatDao scriptStatDao,
			ScriptTagRelDao scriptTagRelDao,
			ScriptCharacterDao scriptCharacterDao,
			ScriptEpisodeDao scriptEpisodeDao,
			ScriptPreviewDao scriptPreviewDao,
			ScriptFavoriteDao scriptFavoriteDao,
			ScriptTradeApplyDao scriptTradeApplyDao,
			ScriptAuditLogDao scriptAuditLogDao,
			UserFollowDao userFollowDao,
			MediaUrlService mediaUrlService) {
		this.scriptDao = scriptDao;
		this.scriptStatDao = scriptStatDao;
		this.scriptTagRelDao = scriptTagRelDao;
		this.scriptCharacterDao = scriptCharacterDao;
		this.scriptEpisodeDao = scriptEpisodeDao;
		this.scriptPreviewDao = scriptPreviewDao;
		this.scriptFavoriteDao = scriptFavoriteDao;
		this.scriptTradeApplyDao = scriptTradeApplyDao;
		this.scriptAuditLogDao = scriptAuditLogDao;
		this.userFollowDao = userFollowDao;
		this.mediaUrlService = mediaUrlService;
	}

	@PostConstruct
	public void checkKey() {
		if (!StringUtils.hasText(aesKey)) {
			throw new IllegalStateException("script.contact.aes-key 未配置，禁止明文存储联系电话");
		}
		byte[] key = Base64.getDecoder().decode(aesKey);
		if (key.length != 16 && key.length != 24 && key.length != 32) {
			throw new IllegalStateException("script.contact.aes-key 必须为 16/24/32 字节 Base64");
		}
	}

	@Override
	public String channelText(Integer code) {
		return ScriptChannelEnums.getLabel(code);
	}

	@Override
	public String licenseText(Integer code) {
		return ScriptLicenseTypeEnums.getLabel(code);
	}

	@Override
	public String saleStatusText(Integer code) {
		return ScriptSaleStatusEnums.getLabel(code);
	}

	@Override
	public String auditStatusText(Integer code) {
		return ScriptAuditStatusEnums.getLabel(code);
	}

	@Override
	public String shelfStatusText(Integer code) {
		return ScriptShelfStatusEnums.getLabel(code);
	}

	@Override
	public String typeText(Integer code) {
		return ScriptTypeEnums.getLabel(code);
	}

	@Override
	public String finishStateText(Integer code) {
		return ScriptFinishStateEnums.getLabel(code);
	}

	@Override
	public String applyTypeText(Integer code) {
		return ScriptTradeApplyTypeEnums.getLabel(code);
	}

	@Override
	public String applyStatusText(Integer code) {
		return ScriptTradeApplyStatusEnums.getLabel(code);
	}

	@Override
	public String roleText(Integer code) {
		if (code == null) {
			return null;
		}
		switch (code) {
			case 1:
				return "男主";
			case 2:
				return "女主";
			case 3:
				return "男二";
			case 4:
				return "女二";
			default:
				return "其他";
		}
	}

	@Override
	public String publishedAgo(Date time) {
		if (time == null) {
			return null;
		}
		long diff = System.currentTimeMillis() - time.getTime();
		if (diff < 60 * 1000L) {
			return "刚刚";
		}
		if (diff < 60 * 60 * 1000L) {
			return (diff / (60 * 1000L)) + "分钟前";
		}
		if (diff < 24 * 60 * 60 * 1000L) {
			return (diff / (60 * 60 * 1000L)) + "小时前";
		}
		if (diff < 7 * 24 * 60 * 60 * 1000L) {
			return (diff / (24 * 60 * 60 * 1000L)) + "天前";
		}
		return new SimpleDateFormat("yyyy-MM-dd").format(time);
	}

	@Override
	public String durationText(Integer seconds) {
		if (seconds == null || seconds <= 0) {
			return null;
		}
		int min = seconds / 60;
		int sec = seconds % 60;
		return String.format("约 %d:%02d", min, sec);
	}

	@Override
	public String maskMobile(String mobile) {
		if (!StringUtils.hasText(mobile)) {
			return null;
		}
		String value = mobile.trim();
		if (value.length() < 7) {
			return "***";
		}
		return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
	}

	@Override
	public String encryptMobile(String mobile) {
		try {
			return AesUtils.encrypt(mobile, aesKey);
		} catch (Exception e) {
			throw new IllegalStateException("联系电话加密失败", e);
		}
	}

	@Override
	public String hashMobile(String mobile) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest(mobile.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder(bytes.length * 2);
			for (byte b : bytes) {
				String hex = Integer.toHexString(b & 0xff);
				if (hex.length() == 1) {
					sb.append('0');
				}
				sb.append(hex);
			}
			return sb.toString();
		} catch (Exception e) {
			throw new IllegalStateException("联系电话哈希失败", e);
		}
	}

	@Override
	public String decryptMobile(String cipher) {
		if (!StringUtils.hasText(cipher)) {
			return null;
		}
		try {
			return AesUtils.decrypt(cipher, aesKey);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void fillDetail(ScriptMarketDetailRespEntity target, ScriptEntity script, Integer uid) {
		target.setId(script.getId());
		target.setTitle(script.getScriptName());
		target.setCoverUrl(mediaUrlService.sign(script.getCoverUrl()));
		target.setChannel(script.getThemePartition());
		target.setChannelText(channelText(script.getThemePartition()));
		target.setTopic(script.getTopic());
		target.setLicenseType(script.getLicenseType());
		target.setLicenseText(licenseText(script.getLicenseType()));
		target.setLicenseScope(script.getLicenseScope());
		target.setType(script.getScriptType());
		target.setTypeText(typeText(script.getScriptType()));
		target.setFinishState(script.getFinishState());
		target.setFinishStateText(finishStateText(script.getFinishState()));
		target.setEpisodes(script.getEpisodes());
		target.setAvgMinutes(script.getAvgMinutes());
		target.setPrice(script.getPriceYuan());
		target.setSaleStatus(script.getSaleStatus());
		target.setSaleStatusText(saleStatusText(script.getSaleStatus()));
		target.setScore(script.getScore());
		target.setHeat(script.getHeatScore());
		target.setSummary(script.getDescription());
		target.setUpdatedAt(script.getGmtModified());

		ScriptMarketDetailRespEntity.Core core = new ScriptMarketDetailRespEntity.Core();
		core.setCorePoint(script.getCorePoint());
		core.setOneLine(script.getOneLine());
		core.setTopics(script.getTopic());
		core.setBenchmarks(script.getBenchmarkWorks());
		target.setCore(core);

		target.setTags(scriptTagRelDao.findTagNamesByScriptId(script.getId()));

		List<ScriptCharacterEntity> charEntities = scriptCharacterDao.findByScriptId(script.getId());
		List<ScriptMarketDetailRespEntity.Character> characters = new ArrayList<>();
		for (ScriptCharacterEntity item : charEntities) {
			ScriptMarketDetailRespEntity.Character c = new ScriptMarketDetailRespEntity.Character();
			c.setName(item.getCharName());
			c.setRole(item.getCharRole());
			c.setRoleText(roleText(item.getCharRole()));
			c.setAvatarUrl(mediaUrlService.sign(item.getAvatarUrl()));
			c.setDescription(item.getDescription());
			characters.add(c);
		}
		target.setCharacters(characters);

		List<ScriptEpisodeEntity> episodeEntities = scriptEpisodeDao.findByScriptId(script.getId());
		List<ScriptMarketDetailRespEntity.Episode> episodeList = new ArrayList<>();
		Map<Integer, String> episodeTitleMap = new HashMap<>();
		for (ScriptEpisodeEntity item : episodeEntities) {
			ScriptMarketDetailRespEntity.Episode e = new ScriptMarketDetailRespEntity.Episode();
			e.setNo(item.getEpisodeNo());
			e.setTitle(item.getEpisodeTitle());
			e.setDurationSeconds(item.getDurationSeconds());
			e.setDurationText(durationText(item.getDurationSeconds()));
			episodeList.add(e);
			episodeTitleMap.put(item.getEpisodeNo(), item.getEpisodeTitle());
		}
		target.setEpisodeList(episodeList);

		target.setPreview(buildPreview(script.getId(), episodeTitleMap));

		target.setAuthor(buildAuthor(script.getAuthorId(), uid));

		ScriptStatEntity stat = scriptStatDao.findByScriptId(script.getId());
		if (stat != null) {
			target.setViews(stat.getViewCount());
			target.setFavorites(stat.getFavoriteCount());
		} else {
			target.setViews(0L);
			target.setFavorites(0L);
		}

		boolean favorited = uid != null && scriptFavoriteDao.findByUidAndScriptId(uid, script.getId()) != null;
		target.setFavorited(favorited);

		if (uid != null) {
			ScriptTradeApplyEntity pending = scriptTradeApplyDao.findPendingByBuyer(script.getId(), uid);
			if (pending != null) {
				ScriptMarketDetailRespEntity.MyApply myApply = new ScriptMarketDetailRespEntity.MyApply();
				myApply.setApplyId(pending.getId());
				myApply.setApplyType(pending.getApplyType());
				myApply.setApplyStatus(pending.getApplyStatus());
				myApply.setApplyStatusText(applyStatusText(pending.getApplyStatus()));
				target.setMyApply(myApply);
			}
		}
	}

	private ScriptMarketDetailRespEntity.Preview buildPreview(Integer scriptId, Map<Integer, String> episodeTitleMap) {
		List<ScriptPreviewEntity> rows = scriptPreviewDao.findByScriptId(scriptId);
		if (rows == null || rows.isEmpty()) {
			return null;
		}
		Integer firstEpisodeNo = rows.get(0).getEpisodeNo();
		ScriptMarketDetailRespEntity.Preview preview = new ScriptMarketDetailRespEntity.Preview();
		preview.setEpisodeNo(firstEpisodeNo);
		preview.setEpisodeTitle(episodeTitleMap.get(firstEpisodeNo));
		List<ScriptMarketDetailRespEntity.Scene> scenes = new ArrayList<>();
		for (ScriptPreviewEntity row : rows) {
			if (!firstEpisodeNo.equals(row.getEpisodeNo())) {
				continue;
			}
			ScriptMarketDetailRespEntity.Scene scene = new ScriptMarketDetailRespEntity.Scene();
			scene.setSceneTitle(row.getSceneTitle());
			if (StringUtils.hasText(row.getContentJson())) {
				scene.setLines(JSON.parseArray(row.getContentJson(), ScriptMarketDetailRespEntity.Line.class));
			} else {
				scene.setLines(new ArrayList<>());
			}
			scenes.add(scene);
		}
		preview.setScenes(scenes);
		return preview;
	}

	private ScriptAuthorRespEntity buildAuthor(Integer authorId, Integer uid) {
		if (authorId == null) {
			return null;
		}
		ScriptAuthorRespEntity author = scriptDao.findAuthorById(authorId);
		if (author == null) {
			return null;
		}
		author.setAvatarUrl(mediaUrlService.sign(author.getAvatarUrl()));
		boolean followed = false;
		if (uid != null && author.getSysUserId() != null) {
			followed = userFollowDao.findOne(uid, author.getSysUserId()) != null;
		}
		author.setFollowed(followed);
		return author;
	}

	@Override
	public void writeAuditLog(Integer scriptId,
			Integer actionType,
			Integer fromAuditStatus,
			Integer toAuditStatus,
			Integer fromShelfStatus,
			Integer toShelfStatus,
			Integer operatorId,
			Integer operatorType,
			String remark) {
		ScriptAuditLogEntity log = new ScriptAuditLogEntity();
		log.setScriptId(scriptId);
		log.setActionType(actionType);
		log.setFromAuditStatus(fromAuditStatus);
		log.setToAuditStatus(toAuditStatus);
		log.setFromShelfStatus(fromShelfStatus);
		log.setToShelfStatus(toShelfStatus);
		log.setOperatorId(operatorId);
		log.setOperatorType(operatorType);
		log.setRemark(remark);
		log.setSetTime(new Date());
		scriptAuditLogDao.insert(log);
	}
}
