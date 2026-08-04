package com.playlet.internal.dao.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.SystemMessageItemEntity;
import com.playlet.internal.entity.message.UserSystemMessageEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSystemMessageDao extends BaseMapper<UserSystemMessageEntity> {

	@Select("select * from user_system_message where to_uid = #{toUid} and status = 1 "
			+ "order by setTime desc, id desc")
	List<UserSystemMessageEntity> findByToUid(@Param("toUid") Integer toUid);

	/**
	 * 广播(读扩散) + 收件箱 合并流，供 PageHelper 分页。
	 * 广播展示/排序时间：定时/生效时间优先，否则用发布时间(gmtModified)，避免草稿创建时间导致「新发却排很后」。
	 * 排序：priority desc, setTime desc, id desc。
	 */
	@Select("<script>"
			+ "SELECT * FROM ("
			+ "  SELECT 'BROADCAST' AS source, p.id AS id, CAST(NULL AS UNSIGNED) AS inboxId, p.id AS publishId,"
			+ "    p.message_type AS messageType,"
			+ "    COALESCE(i_pref.title, i_def.title, i_zh.title) AS title,"
			+ "    COALESCE(i_pref.content, i_def.content, i_zh.content) AS content,"
			+ "    COALESCE(i_pref.cover_url, i_def.cover_url, i_zh.cover_url) AS coverUrl,"
			+ "    p.drama_id AS dramaId, p.jump_type AS jumpType,"
			+ "    COALESCE(NULLIF(i_pref.jump_param,''), NULLIF(i_def.jump_param,''), NULLIF(i_zh.jump_param,''), p.jump_param) AS jumpParam,"
			+ "    IFNULL(p.priority,0) AS priority,"
			+ "    CASE WHEN p.id &lt;= #{cursor} THEN 1 ELSE 0 END AS isRead,"
			+ "    COALESCE(p.schedule_time, p.valid_start, p.gmtModified, p.setTime) AS setTime"
			+ "  FROM system_message_publish p"
			+ "  LEFT JOIN system_message_publish_i18n i_pref ON i_pref.publish_id = p.id AND i_pref.langue = #{langue}"
			+ "  LEFT JOIN system_message_publish_i18n i_def ON i_def.publish_id = p.id AND i_def.langue = p.default_langue"
			+ "  LEFT JOIN system_message_publish_i18n i_zh ON i_zh.publish_id = p.id AND i_zh.langue = 'zh-cn'"
			+ "  WHERE p.status = 1 AND p.publish_status = 1 AND p.audience_type = 1"
			+ "    AND (p.valid_start IS NULL OR p.valid_start &lt;= NOW())"
			+ "    AND (p.valid_end IS NULL OR p.valid_end &gt; NOW())"
			+ "    AND COALESCE(i_pref.title, i_def.title, i_zh.title) IS NOT NULL"
			+ "  UNION ALL"
			+ "  SELECT 'INBOX' AS source, m.id AS id, m.id AS inboxId, m.publish_id AS publishId,"
			+ "    m.message_type AS messageType, m.title AS title, m.content AS content, m.cover_url AS coverUrl,"
			+ "    m.drama_id AS dramaId, m.jump_type AS jumpType, m.jump_param AS jumpParam,"
			+ "    0 AS priority, IFNULL(m.is_read,0) AS isRead, m.setTime AS setTime"
			+ "  FROM user_system_message m"
			+ "  WHERE m.to_uid = #{toUid} AND m.status = 1"
			+ ") t ORDER BY t.priority DESC, t.setTime DESC, t.id DESC"
			+ "</script>")
	List<SystemMessageItemEntity> findMergedFeed(@Param("toUid") Integer toUid,
			@Param("langue") String langue, @Param("cursor") Long cursor);

	@Select("select count(1) from user_system_message where to_uid = #{toUid} and status = 1 "
			+ "and ifnull(is_read,0)=0")
	Integer countUnread(@Param("toUid") Integer toUid);

	@Update("update user_system_message set is_read = 1, gmtModified = now() "
			+ "where id = #{id} and to_uid = #{toUid} and status = 1")
	int readOne(@Param("id") Long id, @Param("toUid") Integer toUid);

	@Update("update user_system_message set is_read = 1, gmtModified = now() "
			+ "where to_uid = #{toUid} and status = 1 and ifnull(is_read,0)=0")
	int readAll(@Param("toUid") Integer toUid);

	@Select("select * from user_system_message where to_uid = #{toUid} and biz_id = #{bizId} limit 1")
	UserSystemMessageEntity findByBiz(@Param("toUid") Integer toUid, @Param("bizId") String bizId);
}
