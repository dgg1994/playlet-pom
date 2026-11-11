package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.CreatorScriptListRespEntity;
import com.playlet.internal.api.response.ScriptAuthorRespEntity;
import com.playlet.internal.api.response.ScriptFavoriteItemRespEntity;
import com.playlet.internal.api.response.ScriptLatestRespEntity;
import com.playlet.internal.api.response.ScriptManageListRespEntity;
import com.playlet.internal.api.response.ScriptMarketCardRespEntity;
import com.playlet.internal.entity.script.ScriptEntity;
import com.playlet.internal.query.script.CreatorScriptListQuery;
import com.playlet.internal.query.script.ScriptManageListQuery;
import com.playlet.internal.query.script.ScriptMarketListQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 剧本主表查询。
 */
@Repository
public interface ScriptDao extends BaseMapper<ScriptEntity> {

	/** 市场可见：未删除 + 审核通过 + 已上架 */
	@Select("select * from script where id = #{id} "
			+ "and ifnull(delete_state,0) = 0 and audit_status = 2 and shelf_status = 1 limit 1")
	ScriptEntity findVisibleById(@Param("id") Integer id);

	@Select("select * from script where id = #{id} and ifnull(delete_state,0) = 0 limit 1")
	ScriptEntity findByIdIncludeDeleted(@Param("id") Integer id);

	/** 购买方是否为该剧本作者（通过 sys_user 关联判断） */
	@Select("select count(*) from creator_account where id = #{authorId} and sys_user_id = #{uid}")
	int countAuthorSelf(@Param("authorId") Integer authorId, @Param("uid") Integer uid);

	@Select("select ca.id as id, ca.nickname as name, ca.avatar as avatarUrl, ca.sys_user_id as sysUserId, "
			+ "  (select count(*) from script s where s.author_id = ca.id "
			+ "    and ifnull(s.delete_state,0) = 0 and s.audit_status = 2 and s.shelf_status = 1) as scriptCount, "
			+ "  (select ifnull(round(avg(s2.score), 1), 0) from script s2 where s2.author_id = ca.id "
			+ "    and ifnull(s2.delete_state,0) = 0 and s2.audit_status = 2) as score "
			+ "from creator_account ca where ca.id = #{authorId} limit 1")
	ScriptAuthorRespEntity findAuthorById(@Param("authorId") Integer authorId);

	@Select("select ca.id as id, ca.nickname as name, ca.avatar as avatarUrl, ca.sys_user_id as sysUserId, "
			+ "  (select count(*) from script s where s.author_id = ca.id "
			+ "    and ifnull(s.delete_state,0) = 0 and s.audit_status = 2 and s.shelf_status = 1) as scriptCount, "
			+ "  (select ifnull(round(avg(s2.score), 1), 0) from script s2 where s2.author_id = ca.id "
			+ "    and ifnull(s2.delete_state,0) = 0 and s2.audit_status = 2) as score "
			+ "from creator_account ca "
			+ "where ca.user_state = 1 "
			+ "  and exists (select 1 from script s3 where s3.author_id = ca.id "
			+ "    and ifnull(s3.delete_state,0) = 0 and s3.audit_status = 2 and s3.shelf_status = 1) "
			+ "order by scriptCount desc, score desc, ca.id desc limit #{limit}")
	List<ScriptAuthorRespEntity> findRecommendAuthors(@Param("limit") int limit);

	@Select("select s.id as id, s.script_name as title, s.cover_url as coverUrl, s.setTime as publishedAt "
			+ "from script s where ifnull(s.delete_state,0) = 0 and s.audit_status = 2 and s.shelf_status = 1 "
			+ "order by s.setTime desc, s.id desc limit #{limit}")
	List<ScriptLatestRespEntity> findLatest(@Param("limit") int limit);

	@Select("<script>"
			+ "select s.id as id, s.script_name as title, s.cover_url as coverUrl, "
			+ "  s.theme_partition as channel, s.topic as topic, s.license_type as licenseType, "
			+ "  s.episodes as episodes, s.avg_minutes as avgMinutes, s.price_yuan as price, "
			+ "  s.sale_status as saleStatus, s.score as score, s.heat_score as heat, "
			+ "  s.description as summary, s.setTime as publishedAt, "
			+ "  s.author_id as authorId, ca.nickname as authorName, ca.avatar as authorAvatar, "
			+ "  (select count(*) from script s2 where s2.author_id = s.author_id "
			+ "    and ifnull(s2.delete_state,0) = 0 and s2.audit_status = 2 and s2.shelf_status = 1) as authorScriptCount, "
			+ "  (select ifnull(round(avg(s3.score), 1), 0) from script s3 where s3.author_id = s.author_id "
			+ "    and ifnull(s3.delete_state,0) = 0 and s3.audit_status = 2) as authorScore "
			+ "from script s left join creator_account ca on ca.id = s.author_id "
			+ "where ifnull(s.delete_state,0) = 0 and s.audit_status = 2 and s.shelf_status = 1 "
			+ "<if test='q.keyword != null and q.keyword != \"\"'>"
			+ "  and (s.script_name like concat('%', #{q.keyword}, '%') "
			+ "    or s.description like concat('%', #{q.keyword}, '%') "
			+ "    or s.core_point like concat('%', #{q.keyword}, '%') "
			+ "    or ca.nickname like concat('%', #{q.keyword}, '%') "
			+ "    or exists (select 1 from script_tag_rel r join dic_script_tag t on t.id = r.tag_id "
			+ "      where r.script_id = s.id and t.tag_name like concat('%', #{q.keyword}, '%'))) "
			+ "</if>"
			+ "<if test='q.topic != null and q.topic != \"\"'> and s.topic = #{q.topic} </if>"
			+ "<if test='q.channel != null and q.channel != 0'> and s.theme_partition = #{q.channel} </if>"
			+ "<if test='q.licenseType != null and q.licenseType != 0'> and s.license_type = #{q.licenseType} </if>"
			+ "<if test='q.priceRange != null and q.priceRange == 1'> and s.price_yuan &lt; 10000 </if>"
			+ "<if test='q.priceRange != null and q.priceRange == 2'> and s.price_yuan &gt;= 10000 and s.price_yuan &lt; 50000 </if>"
			+ "<if test='q.priceRange != null and q.priceRange == 3'> and s.price_yuan &gt;= 50000 and s.price_yuan &lt; 100000 </if>"
			+ "<if test='q.priceRange != null and q.priceRange == 4'> and s.price_yuan &gt;= 100000 </if>"
			+ "<if test='q.scaleRange != null and q.scaleRange == 1'> and s.episodes &lt; 10 </if>"
			+ "<if test='q.scaleRange != null and q.scaleRange == 2'> and s.episodes &gt;= 10 and s.episodes &lt;= 30 </if>"
			+ "<if test='q.scaleRange != null and q.scaleRange == 3'> and s.episodes &gt;= 31 and s.episodes &lt;= 60 </if>"
			+ "<if test='q.scaleRange != null and q.scaleRange == 4'> and s.episodes &gt; 60 </if>"
			+ "<if test='q.saleStatus != null and q.saleStatus != -1'> and s.sale_status = #{q.saleStatus} </if>"
			+ "<choose>"
			+ "  <when test='q.sortType != null and q.sortType == 2'> order by s.setTime desc, s.id desc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 3'> order by s.score desc, s.id desc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 4'> order by s.price_yuan desc, s.id desc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 5'> order by s.heat_score desc, s.id desc </when>"
			+ "  <otherwise> order by s.sort_weight desc, s.score desc, s.heat_score desc, s.setTime desc, s.id desc </otherwise>"
			+ "</choose>"
			+ "</script>")
	List<ScriptMarketCardRespEntity> findMarketList(@Param("q") ScriptMarketListQuery q);

	@Select("<script>"
			+ "select s.id as id, s.script_name as title, s.cover_url as coverUrl, s.episodes as episodes, "
			+ "  s.price_yuan as price, s.sale_status as saleStatus, s.topic as topic, "
			+ "  ca.nickname as authorName, f.setTime as favoritedAt "
			+ "from script_favorite f join script s on s.id = f.script_id "
			+ "left join creator_account ca on ca.id = s.author_id "
			+ "where f.uid = #{uid} and ifnull(s.delete_state,0) = 0 "
			+ "<if test='keyword != null and keyword != \"\"'> and s.script_name like concat('%', #{keyword}, '%') </if>"
			+ "<if test='topic != null and topic != \"\"'> and s.topic = #{topic} </if>"
			+ "order by f.setTime desc, f.id desc"
			+ "</script>")
	List<ScriptFavoriteItemRespEntity> findFavoriteList(@Param("uid") Integer uid,
			@Param("keyword") String keyword, @Param("topic") String topic);

	@Select("<script>"
			+ "select s.id as id, s.script_name as title, s.cover_url as coverUrl, s.episodes as episodes, "
			+ "  s.price_yuan as price, s.sale_status as saleStatus, s.audit_status as auditStatus, "
			+ "  s.audit_reject_reason as auditRejectReason, s.shelf_status as shelfStatus, "
			+ "  s.setTime as setTime, s.gmtModified as gmtModified, ifnull(st.favorite_count, 0) as favoriteCount "
			+ "from script s left join script_stat st on st.script_id = s.id "
			+ "where s.author_id = #{authorId} and ifnull(s.delete_state,0) = 0 "
			+ "<if test='q.title != null and q.title != \"\"'> and s.script_name like concat('%', #{q.title}, '%') </if>"
			+ "<if test='q.auditStatus != null'> and s.audit_status = #{q.auditStatus} </if>"
			+ "<if test='q.shelfStatus != null'> and s.shelf_status = #{q.shelfStatus} </if>"
			+ "<if test='q.saleStatus != null'> and s.sale_status = #{q.saleStatus} </if>"
			+ "order by s.gmtModified desc, s.id desc"
			+ "</script>")
	List<CreatorScriptListRespEntity> findCreatorList(@Param("q") CreatorScriptListQuery q,
			@Param("authorId") Integer authorId);

	@Select("<script>"
			+ "select s.id as id, s.script_name as title, s.cover_url as coverUrl, s.author_id as authorId, "
			+ "  ca.nickname as authorName, s.audit_status as auditStatus, s.shelf_status as shelfStatus, "
			+ "  s.sale_status as saleStatus, s.price_yuan as price, s.episodes as episodes, s.setTime as setTime "
			+ "from script s left join creator_account ca on ca.id = s.author_id "
			+ "where ifnull(s.delete_state,0) = 0 "
			+ "<if test='q.title != null and q.title != \"\"'> and s.script_name like concat('%', #{q.title}, '%') </if>"
			+ "<if test='q.authorId != null'> and s.author_id = #{q.authorId} </if>"
			+ "<if test='q.auditStatus != null'> and s.audit_status = #{q.auditStatus} </if>"
			+ "<if test='q.shelfStatus != null'> and s.shelf_status = #{q.shelfStatus} </if>"
			+ "<if test='q.saleStatus != null'> and s.sale_status = #{q.saleStatus} </if>"
			+ "<if test='q.startTime != null and q.startTime != \"\"'> and s.setTime &gt;= #{q.startTime} </if>"
			+ "<if test='q.endTime != null and q.endTime != \"\"'> and s.setTime &lt;= #{q.endTime} </if>"
			+ "order by s.setTime desc, s.id desc"
			+ "</script>")
	List<ScriptManageListRespEntity> findManageList(@Param("q") ScriptManageListQuery q);

	@Update("update script set audit_status = #{toStatus}, audit_reject_reason = #{rejectReason}, "
			+ "  audit_time = now(), auditor_id = #{auditorId}, audit_pass_time = #{auditPassTime}, "
			+ "  version = version + 1, gmtModified = now() "
			+ "where id = #{id} and audit_status = #{fromStatus} and version = #{version} "
			+ "  and ifnull(delete_state,0) = 0")
	int casAudit(@Param("id") Integer id,
			@Param("fromStatus") Integer fromStatus,
			@Param("toStatus") Integer toStatus,
			@Param("rejectReason") String rejectReason,
			@Param("auditorId") Integer auditorId,
			@Param("auditPassTime") Date auditPassTime,
			@Param("version") Integer version);

	@Update("update script set shelf_status = #{toStatus}, shelf_time = #{shelfTime}, "
			+ "  offline_reason = #{offlineReason}, version = version + 1, gmtModified = now() "
			+ "where id = #{id} and shelf_status = #{fromStatus} and version = #{version} "
			+ "  and ifnull(delete_state,0) = 0")
	int casShelf(@Param("id") Integer id,
			@Param("fromStatus") Integer fromStatus,
			@Param("toStatus") Integer toStatus,
			@Param("shelfTime") Date shelfTime,
			@Param("offlineReason") String offlineReason,
			@Param("version") Integer version);

	@Update("update script set sale_status = #{toStatus}, version = version + 1, gmtModified = now() "
			+ "where id = #{id} and sale_status = #{fromStatus} and version = #{version} "
			+ "  and ifnull(delete_state,0) = 0")
	int casSaleStatus(@Param("id") Integer id,
			@Param("fromStatus") Integer fromStatus,
			@Param("toStatus") Integer toStatus,
			@Param("version") Integer version);

	@Update("update script set delete_state = 1, version = version + 1, gmtModified = now() "
			+ "where id = #{id} and version = #{version} and ifnull(delete_state,0) = 0")
	int softDelete(@Param("id") Integer id, @Param("version") Integer version);
}
