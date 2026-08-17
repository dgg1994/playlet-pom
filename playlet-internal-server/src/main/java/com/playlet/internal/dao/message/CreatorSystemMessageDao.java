package com.playlet.internal.dao.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.CreatorMessageItemRespEntity;
import com.playlet.internal.entity.message.CreatorSystemMessageEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作家系统消息收件箱。
 */
@Repository
public interface CreatorSystemMessageDao extends BaseMapper<CreatorSystemMessageEntity> {

	@Select("select * from creator_system_message where to_creator_id = #{toCreatorId} and biz_id = #{bizId} limit 1")
	CreatorSystemMessageEntity findByBiz(@Param("toCreatorId") Integer toCreatorId, @Param("bizId") String bizId);

	/**
	 * 站务广播（读扩散）+ 个人收件箱合并流，供 PageHelper 分页。
	 */
	@Select("<script>"
			+ "SELECT * FROM ("
			+ "  SELECT #{broadcastSource} AS source, p.id AS id, CAST(NULL AS UNSIGNED) AS inboxId, p.id AS publishId,"
			+ "    #{siteType} AS messageType,"
			+ "    COALESCE(i_pref.title, i_def.title, i_zh.title) AS title,"
			+ "    COALESCE(i_pref.content, i_def.content, i_zh.content) AS content,"
			+ "    COALESCE(i_pref.cover_url, i_def.cover_url, i_zh.cover_url) AS coverUrl,"
			+ "    p.drama_id AS dramaId, CAST(NULL AS SIGNED) AS assetId, p.jump_type AS jumpType,"
			+ "    COALESCE(NULLIF(i_pref.jump_param,''), NULLIF(i_def.jump_param,''), NULLIF(i_zh.jump_param,''), p.jump_param) AS jumpParam,"
			+ "    IFNULL(p.priority,0) AS priority,"
			+ "    CASE WHEN p.id &lt;= #{cursor} THEN 1 ELSE 0 END AS isRead,"
			+ "    COALESCE(p.schedule_time, p.valid_start, p.gmtModified, p.setTime) AS setTime"
			+ "  FROM system_message_publish p"
			+ "  LEFT JOIN system_message_publish_i18n i_pref ON i_pref.publish_id = p.id AND i_pref.langue = #{langue}"
			+ "  LEFT JOIN system_message_publish_i18n i_def ON i_def.publish_id = p.id AND i_def.langue = p.default_langue"
			+ "  LEFT JOIN system_message_publish_i18n i_zh ON i_zh.publish_id = p.id AND i_zh.langue = #{fallbackLangue}"
			+ "  WHERE p.status = 1 AND p.publish_status = 1 AND p.audience_type = 1"
			+ "    AND (p.valid_start IS NULL OR p.valid_start &lt;= NOW())"
			+ "    AND (p.valid_end IS NULL OR p.valid_end &gt; NOW())"
			+ "    AND COALESCE(i_pref.title, i_def.title, i_zh.title) IS NOT NULL"
			+ "  UNION ALL"
			+ "  SELECT #{inboxSource} AS source, m.id AS id, m.id AS inboxId, m.publish_id AS publishId,"
			+ "    m.message_type AS messageType, m.title AS title, m.content AS content, m.cover_url AS coverUrl,"
			+ "    m.drama_id AS dramaId, m.asset_id AS assetId, m.jump_type AS jumpType, m.jump_param AS jumpParam,"
			+ "    0 AS priority, IFNULL(m.is_read,0) AS isRead, m.setTime AS setTime"
			+ "  FROM creator_system_message m"
			+ "  WHERE m.to_creator_id = #{toCreatorId} AND m.status = #{statusValid}"
			+ ") t ORDER BY t.priority DESC, t.setTime DESC, t.id DESC"
			+ "</script>")
	List<CreatorMessageItemRespEntity> findMergedFeed(@Param("toCreatorId") Integer toCreatorId,
			@Param("langue") String langue, @Param("cursor") Long cursor,
			@Param("siteType") String siteType, @Param("inboxSource") String inboxSource,
			@Param("broadcastSource") String broadcastSource, @Param("statusValid") Integer statusValid,
			@Param("fallbackLangue") String fallbackLangue);

	@Select("select count(1) from creator_system_message where to_creator_id = #{toCreatorId} "
			+ "and status = #{statusValid} and ifnull(is_read,0) = #{unread}")
	Integer countUnread(@Param("toCreatorId") Integer toCreatorId,
			@Param("statusValid") Integer statusValid, @Param("unread") Integer unread);

	@Update("update creator_system_message set is_read = #{readFlag}, gmtModified = now() "
			+ "where id = #{id} and to_creator_id = #{toCreatorId} and status = #{statusValid}")
	int readOne(@Param("id") Long id, @Param("toCreatorId") Integer toCreatorId,
			@Param("readFlag") Integer readFlag, @Param("statusValid") Integer statusValid);

	@Update("update creator_system_message set is_read = #{readFlag}, gmtModified = now() "
			+ "where to_creator_id = #{toCreatorId} and status = #{statusValid} and ifnull(is_read,0) = #{unread}")
	int readAll(@Param("toCreatorId") Integer toCreatorId, @Param("readFlag") Integer readFlag,
			@Param("statusValid") Integer statusValid, @Param("unread") Integer unread);
}
