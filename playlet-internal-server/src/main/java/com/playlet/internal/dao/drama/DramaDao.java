package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.request.DramaPaymentPurchaseQuery;
import com.playlet.internal.api.request.DramaPaymentQuery;
import com.playlet.internal.api.response.DramaAnalyticsRespEntity;
import com.playlet.internal.api.response.DramaEpisodePayItemEntity;
import com.playlet.internal.api.response.DramaPaymentDetailEntity;
import com.playlet.internal.api.response.DramaPaymentRespEntity;
import com.playlet.internal.api.response.DramaPurchaseItemEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.query.creator.CreatorDramaAnalyticsQuery;
import com.playlet.internal.query.drama.QueryDramaQuery;
import com.playlet.internal.query.drama.RecommendDramaQuery;
import com.playlet.internal.api.response.RecommendDramaRespEntity;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaDao extends BaseMapper<DramaEntity> {

	/** C端：仅已上架未删除（id 为主键；verify_status=1 对应 VerifyStateEnums.AVAILABLE_NOW） */
	@Select("select * from drama where id = #{dramaId} and verify_status = 1 and delete_state = 0 limit 1")
	DramaEntity findOnlineByDramaId(@Param("dramaId") Integer dramaId);

	/** 管理端：按主键查（含草稿，不含软删） */
	@Select("select * from drama where id = #{dramaId} and delete_state = 0 limit 1")
	DramaEntity findByDramaId(@Param("dramaId") Integer dramaId);

	/** 批量：未软删（对齐 findByDramaId） */
	@Select("<script>"
			+ "select * from drama where delete_state = 0 and id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<DramaEntity> findByIds(@Param("ids") List<Integer> ids);

	/** 批量：含软删（对齐 interact 消息里 findByDramaId 失败后 selectById 回退） */
	@Select("<script>"
			+ "select * from drama where id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<DramaEntity> findByIdsIncludeDeleted(@Param("ids") List<Integer> ids);

	@Select("<script>"
			+ "select * from drama where delete_state = 0 "
			+ "<if test='verifyStatus != null'> and verify_status = #{verifyStatus} </if>"
			+ "<if test='dramaTitle != null and dramaTitle != \"\"'> and drama_title like concat('%',#{dramaTitle},'%') </if>"
			+ "<if test='id != null'> and id = #{id} </if>"
			+ "<if test='producerFirm != null and producerFirm != \"\"'> and producer_firm like concat('%',#{producerFirm},'%') </if>"
			+ "order by id desc"
			+ "</script>")
	List<DramaEntity> findAdminList(DramaEntity entity);

	/**
	 * C端剧场搜索：标题模糊 + 标签分组精确，仅已上架；
	 * preferTagGroupIds 非空时偏好命中优先（soft boost），不做硬过滤。
	 */
	@Select("<script>"
			+ "select distinct d.* from drama d "
			+ "where d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "<if test='entity.dramaTitle != null and entity.dramaTitle != \"\"'> "
			+ "  and d.drama_title like concat('%', #{entity.dramaTitle}, '%') "
			+ "</if>"
			+ "<if test='entity.tagGroupId != null and entity.tagGroupId != \"\"'> "
			+ "  and exists ( "
			+ "    select 1 from drama_tag_rel r "
			+ "    where r.drama_id = d.id and r.tag_group_id = #{entity.tagGroupId} "
			+ "  ) "
			+ "</if>"
			+ "<choose>"
			// 仅判 null：空偏好须传 null，勿传 Collections.emptyList（JDK16+ OGNL size 反射失败）
			+ "  <when test='preferTagGroupIds != null'>"
			+ "    order by ("
			+ "      case when exists ("
			+ "        select 1 from drama_tag_rel pr "
			+ "        where pr.drama_id = d.id and pr.tag_group_id in "
			+ "        <foreach collection='preferTagGroupIds' item='tg' open='(' separator=',' close=')'>#{tg}</foreach>"
			+ "      ) then 1 else 0 end"
			+ "    ) desc, ifnull(d.hot_score, 0) desc, d.id desc"
			+ "  </when>"
			+ "  <otherwise>"
			+ "    order by ifnull(d.hot_score, 0) desc, d.id desc"
			+ "  </otherwise>"
			+ "</choose>"
			+ "</script>")
	List<DramaEntity> searchOnline(@Param("entity") DramaEntity entity,
			@Param("preferTagGroupIds") List<String> preferTagGroupIds);

	/**
	 * 首页猜你喜欢：命中偏好标签的在架剧，按热度排序。
	 */
	@Select("<script>"
			+ "select d.* from drama d "
			+ "where d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "and exists ("
			+ "  select 1 from drama_tag_rel r "
			+ "  where r.drama_id = d.id and r.tag_group_id in "
			+ "  <foreach collection='preferTagGroupIds' item='tg' open='(' separator=',' close=')'>#{tg}</foreach>"
			+ ") "
			+ "order by ifnull(d.hot_score, 0) desc, d.id desc "
			+ "limit #{limit}"
			+ "</script>")
	List<DramaEntity> findOnlineByPreferTags(@Param("preferTagGroupIds") List<String> preferTagGroupIds,
			@Param("limit") int limit);

	/**
	 * 剧单 AND：须命中全部 tag_group_id 的在架剧，按热度排序。
	 */
	@Select("<script>"
			+ "select d.* from drama d "
			+ "where d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "<foreach collection='tagGroupIds' item='tg'>"
			+ "  and exists ("
			+ "    select 1 from drama_tag_rel r "
			+ "    where r.drama_id = d.id and r.tag_group_id = #{tg}"
			+ "  )"
			+ "</foreach>"
			+ "order by ifnull(d.hot_score, 0) desc, d.id desc "
			+ "limit #{limit}"
			+ "</script>")
	List<DramaEntity> findOnlineByAllTags(@Param("tagGroupIds") List<String> tagGroupIds,
			@Param("limit") int limit);

	/**
	 * 剧单分页 OR：命中任一标签（配合 PageHelper，无 limit）。
	 */
	@Select("<script>"
			+ "select d.* from drama d "
			+ "where d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "and exists ("
			+ "  select 1 from drama_tag_rel r "
			+ "  where r.drama_id = d.id and r.tag_group_id in "
			+ "  <foreach collection='tagGroupIds' item='tg' open='(' separator=',' close=')'>#{tg}</foreach>"
			+ ") "
			+ "order by ifnull(d.hot_score, 0) desc, d.id desc"
			+ "</script>")
	List<DramaEntity> pageOnlineByPreferTags(@Param("tagGroupIds") List<String> tagGroupIds);

	/**
	 * 剧单分页 AND：须命中全部标签（配合 PageHelper，无 limit）。
	 */
	@Select("<script>"
			+ "select d.* from drama d "
			+ "where d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "<foreach collection='tagGroupIds' item='tg'>"
			+ "  and exists ("
			+ "    select 1 from drama_tag_rel r "
			+ "    where r.drama_id = d.id and r.tag_group_id = #{tg}"
			+ "  )"
			+ "</foreach>"
			+ "order by ifnull(d.hot_score, 0) desc, d.id desc"
			+ "</script>")
	List<DramaEntity> pageOnlineByAllTags(@Param("tagGroupIds") List<String> tagGroupIds);

	/**
	 * 管理端短剧列表：producerFirm 取 creator_account.nickname（按 belong_user 关联）。
	 */
	@Select("<script>"
	        + "select d.id, d.drama_title, d.cover_url, d.score_num, d.hot_score, d.collect_score,"
	        + "  d.share_score, d.like_score, d.discuss_score, d.hot_score_text, d.total_episodes,"
	        + "  d.finished_state, d.video_type, d.video_width, d.video_height, d.description_info,"
	        + "  d.verify_status, d.audit_status, d.shelf_status, d.audit_reject_reason, d.audit_pass_time,"
	        + "  d.appeal_status, d.appeal_reason, d.appeal_time, d.shelf_time, d.delete_state,"
	        + "  d.recommended_carousel, d.offline_reason, d.remark_info, d.belong_user, d.is_ai,"
	        + "  d.is_member_drama,"
	        + "  d.setTime, d.gmtModified,"
	        + "  ca.nickname as producer_firm"
	        + " from drama d"
	        + " left join creator_account ca on ca.id = d.belong_user"
	        + " where 1=1"
	        + "<if test='dramaTitle != null and dramaTitle != \"\"'>"
	        + " and ("
	        + "   d.drama_title like concat('%', #{dramaTitle}, '%')"
	        + "   or exists ("
	        + "     select 1"
	        + "     from drama_tag_rel dtr"
	        + "     inner join dic_drama_tag dt on dt.group_id = dtr.tag_group_id"
	        + "     where dtr.drama_id = d.id"
	        + "       and dt.tag_name like concat('%', #{dramaTitle}, '%')"
	        + "   )"
	        + " )"
	        + "</if>"
	        + "<if test='finishedState != null'> and d.finished_state = #{finishedState}</if>"
	        + "<if test='videoType != null'> and d.video_type = #{videoType}</if>"
	        + "<if test='producerFirm != null and producerFirm != \"\"'> and ca.nickname = #{producerFirm}</if>"
	        + "<if test='deleteState != null'> and d.delete_state = #{deleteState}</if>"
	        + "<if test='verifyStatus != null'> and d.verify_status = #{verifyStatus}</if>"
	        + "<if test='recommendedCarousel != null'> and d.recommended_carousel = #{recommendedCarousel}</if>"
			+ "<if test='groupId != null'>"
			+ " and d.id in ("
			+ "   select drama_id from drama_tag_rel where tag_group_id = #{groupId}"
			+ " )"
			+ "</if>"
	        + "<if test='tagGroupIdList != null and tagGroupIdList.size() > 0'>"
	        + " and d.id in ("
	        + "   select drama_id from drama_tag_rel where tag_group_id in "
	        + "   <foreach collection='tagGroupIdList' item='tagId' open='(' separator=',' close=')'>"
	        + "     #{tagId}"
	        + "   </foreach>"
	        + " )"
	        + "</if>"
	        + "order by d.setTime desc"
	        + "</script>")
	List<DramaEntity> findList(QueryDramaQuery entity);

	@Update("update drama set collect_score = ifnull(collect_score,0) + 1, gmtModified = now() where id = #{dramaId}")
	int incrCollectScore(@Param("dramaId") Integer dramaId);

	@Update("update drama set collect_score = greatest(ifnull(collect_score,0) - 1, 0), gmtModified = now() where id = #{dramaId}")
	int decrCollectScore(@Param("dramaId") Integer dramaId);

	@Update("update drama set like_score = ifnull(like_score,0) + 1, gmtModified = now() where id = #{dramaId}")
	int incrLikeScore(@Param("dramaId") Integer dramaId);

	@Update("update drama set like_score = greatest(ifnull(like_score,0) - 1, 0), gmtModified = now() where id = #{dramaId}")
	int decrLikeScore(@Param("dramaId") Integer dramaId);

	@Update("update drama set hot_score = ifnull(hot_score,0) + #{delta}, gmtModified = now() where id = #{dramaId}")
	int incrHotScore(@Param("dramaId") Integer dramaId, @Param("delta") long delta);

	/**
	 * 推荐流：无偏好时按 seed 打散；有偏好时命中优先再 seed。
	 */
	@Select("<script>"
			+ "SELECT d.* FROM drama d "
			+ "WHERE d.delete_state = #{entity.deleteState} AND d.verify_status = #{entity.verifyStatus} "
			+ "<choose>"
			// 仅判 null：空偏好须传 null，勿传 Collections.emptyList（JDK16+ OGNL size 反射失败）
			+ "  <when test='preferTagGroupIds != null'>"
			+ "    ORDER BY ("
			+ "      case when exists ("
			+ "        select 1 from drama_tag_rel pr "
			+ "        where pr.drama_id = d.id and pr.tag_group_id in "
			+ "        <foreach collection='preferTagGroupIds' item='tg' open='(' separator=',' close=')'>#{tg}</foreach>"
			+ "      ) then 1 else 0 end"
			+ "    ) desc, ifnull(d.hot_score, 0) desc, CRC32(CONCAT(d.id, #{entity.seed})), d.id"
			+ "  </when>"
			+ "  <otherwise>"
			+ "    ORDER BY CRC32(CONCAT(d.id, #{entity.seed})), d.id"
			+ "  </otherwise>"
			+ "</choose>"
			+ "</script>")
	List<RecommendDramaRespEntity> recommendList(@Param("entity") RecommendDramaQuery entity,
			@Param("preferTagGroupIds") List<String> preferTagGroupIds);

	@Select("SELECT * from drama where id = (SELECT drama_id from drama_asset where id = #{id})")
	DramaEntity findByVideoId(@Param("id") Integer id);

	@Update("update drama set share_score = ifnull(share_score,0) + 1, gmtModified = now() where id = #{dramaId}")
	void incrShareScore(@Param("dramaId") Integer dramaId);

	/** 管理端：设置是否会员短剧 */
	@Update("update drama set is_member_drama = #{isMemberDrama}, gmtModified = now() where id = #{dramaId}")
	int updateMemberDrama(@Param("dramaId") Integer dramaId, @Param("isMemberDrama") Integer isMemberDrama);
	/**
	 * 相关作品：同标签；preferTagGroupIds 非空时在同标签集合内偏好加权。
	 */
	@Select("<script>"
			+ "SELECT d.* "
			+ "FROM drama d "
			+ "WHERE d.delete_state = #{deleteState} "
			+ "  AND d.verify_status = #{verifyStatus} "
			+ "  AND d.id != #{id} "
			+ "  AND EXISTS ("
			+ "      SELECT 1 "
			+ "      FROM drama_tag_rel dtr "
			+ "      WHERE dtr.drama_id = d.id "
			+ "        AND dtr.tag_group_id IN ( "
			+ "            SELECT tag_group_id "
			+ "            FROM drama_tag_rel "
			+ "            WHERE drama_id = #{id} "
			+ "        )"
			+ "  ) "
			+ "<choose>"
			// 仅判 null：空偏好须传 null，勿传 Collections.emptyList（JDK16+ OGNL size 反射失败）
			+ "  <when test='preferTagGroupIds != null'>"
			+ "    ORDER BY ("
			+ "      case when exists ("
			+ "        select 1 from drama_tag_rel pr "
			+ "        where pr.drama_id = d.id and pr.tag_group_id in "
			+ "        <foreach collection='preferTagGroupIds' item='tg' open='(' separator=',' close=')'>#{tg}</foreach>"
			+ "      ) then 1 else 0 end"
			+ "    ) desc, ifnull(d.hot_score, 0) desc, d.id desc"
			+ "  </when>"
			+ "  <otherwise>"
			+ "    ORDER BY ifnull(d.hot_score, 0) desc, d.id desc"
			+ "  </otherwise>"
			+ "</choose>"
			+ "LIMIT 20"
			+ "</script>")
	List<RecommendDramaRespEntity> relatedWork(@Param("id") Integer id,
			@Param("deleteState") Integer deleteState,
			@Param("verifyStatus") Integer verifyStatus,
			@Param("preferTagGroupIds") List<String> preferTagGroupIds);

	@Select("select * from drama where id = #{id}")
	RecommendDramaRespEntity findById(@Param("id") Integer id);

	@Select("select d.* from drama d "
			+ "inner join rank_list rl on d.id = rl.drama_id "
			+ "where rl.board_group_id = #{groupId} and rl.status = 1 "
			+ "and d.verify_status = #{verifyStatus} and ifnull(d.delete_state, 0) = #{deleteState} "
			+ "order by rl.rank_no asc")
	List<DramaEntity> selectListDramas(@Param("groupId") String groupId,
			@Param("verifyStatus") Integer verifyStatus,
			@Param("deleteState") Integer deleteState);

	/**
	 * 作家数据分析列表：外层无 GROUP BY，避免 PageHelper count 失真。
	 */
	@Select("<script>"
			+ "select d.id as id, d.drama_title as dramaTitle, d.cover_url as coverUrl, "
			+ "  d.total_episodes as totalEpisodes, d.hot_score as hotScore, d.setTime as setTime, "
			+ "  ifnull(a.exposure, 0) as exposure, "
			+ "  ifnull(a.completeCnt, 0) as complete, "
			+ "  ifnull(s.playPv, 0) as playPv, "
			+ "  ifnull(sc.avgScore, 0) as scoreNum "
			+ "from drama d "
			+ "left join ( "
			+ "  select drama_id, sum(ifnull(exposure_count,0)) as exposure, "
			+ "    sum(ifnull(complete_count,0)) as completeCnt "
			+ "  from drama_asset where ifnull(delete_state,0)=0 group by drama_id "
			+ ") a on a.drama_id = d.id "
			+ "left join ( "
			+ "  select drama_id, sum(play_pv) as playPv "
			+ "  from drama_rank_stat_daily group by drama_id "
			+ ") s on s.drama_id = d.id "
			+ "left join ( "
			+ "  select drama_id, avg(score) as avgScore "
			+ "  from drama_video_comment "
			+ "  where comment_type = 2 and parent_id = 0 "
			+ "    and ifnull(delete_state,0)=0 and score is not null "
			+ "  group by drama_id "
			+ ") sc on sc.drama_id = d.id "
			+ "where d.belong_user = #{belongUser} and ifnull(d.delete_state,0)=0 "
			+ "<if test='q.dramaTitle != null and q.dramaTitle != \"\"'> "
			+ "  and d.drama_title like concat('%', #{q.dramaTitle}, '%') "
			+ "</if>"
			+ "<choose>"
			+ "  <when test='q.sortType != null and q.sortType == 2'> "
			+ "    order by d.setTime desc, d.id desc "
			+ "  </when>"
			+ "  <otherwise>"
			+ "    order by ifnull(d.hot_score,0) desc, d.id desc "
			+ "  </otherwise>"
			+ "</choose>"
			+ "</script>")
	List<DramaAnalyticsRespEntity> analyticsList(@Param("q") CreatorDramaAnalyticsQuery q,
			@Param("belongUser") Integer belongUser);

	/**
	 * 短剧付费汇总：按剧维度统计现金/金币解锁收入与购买人次。
	 * payType：MEMBER=会员剧 / PAY=含单集付费 / AD=含广告解锁。
	 */
	@Select("<script>"
			+ "SELECT "
			+ "  CAST(d.id AS CHAR) AS dramaId, "
			+ "  d.drama_title AS dramaName, "
			+ "  IFNULL(( "
			+ "    SELECT GROUP_CONCAT(DISTINCT t.tag_name ORDER BY t.id SEPARATOR '、') "
			+ "    FROM drama_tag_rel r "
			+ "    INNER JOIN dic_drama_tag t ON t.group_id = r.tag_group_id "
			+ "    WHERE r.drama_id = d.id "
			+ "  ), '') AS dramaTag, "
			+ "  IFNULL(d.total_episodes, 0) AS dramaSum, "
			+ "  (SELECT COUNT(1) FROM drama_asset da "
			+ "    WHERE da.drama_id = d.id AND da.unlock_type = 2 "
			+ "      AND (da.delete_state = 0 OR da.delete_state IS NULL)"
			+ "  ) AS dramaPaySum, "
			+ "  IFNULL(SUM(CASE WHEN ueu.unlock_way IN ('COIN','CASH') "
			+ "    THEN IFNULL(ueu.cost_yuan, 0) ELSE 0 END), 0) AS dramaTotalRevenue, "
			+ "  IFNULL(SUM(CASE WHEN ueu.unlock_way IN ('COIN','CASH') THEN 1 ELSE 0 END), 0) AS payPeopleSum, "
			+ "  MAX(CASE WHEN ueu.unlock_way IN ('COIN','CASH') THEN ueu.setTime ELSE NULL END) AS payTime "
			+ "FROM drama d "
			+ "LEFT JOIN user_episode_unlock ueu ON ueu.drama_id = d.id "
			+ "WHERE (d.delete_state = 0 OR d.delete_state IS NULL) "
			+ "  AND ( "
			+ "    d.is_member_drama = 1 "
			+ "    OR EXISTS (SELECT 1 FROM drama_asset x WHERE x.drama_id = d.id AND x.unlock_type IN (1, 2) "
			+ "      AND (x.delete_state = 0 OR x.delete_state IS NULL)) "
			+ "    OR EXISTS (SELECT 1 FROM user_episode_unlock u "
			+ "      WHERE u.drama_id = d.id AND u.unlock_way IN ('COIN','CASH')) "
			+ "  ) "
			+ "<if test='q.dramaName != null and q.dramaName != \"\"'>"
			+ "  AND (CAST(d.id AS CHAR) = #{q.dramaName} "
			+ "    OR d.drama_title LIKE CONCAT('%', #{q.dramaName}, '%')) "
			+ "</if>"
			+ "<if test='q.dramaTag != null and q.dramaTag != \"\"'>"
			+ "  AND EXISTS ("
			+ "    SELECT 1 FROM drama_tag_rel r2 "
			+ "    INNER JOIN dic_drama_tag t2 ON t2.group_id = r2.tag_group_id "
			+ "    WHERE r2.drama_id = d.id "
			+ "      AND (r2.tag_group_id = #{q.dramaTag} OR t2.tag_name LIKE CONCAT('%', #{q.dramaTag}, '%')) "
			+ "  ) "
			+ "</if>"
			+ "<if test='q.payType != null and q.payType == \"MEMBER\"'>"
			+ "  AND d.is_member_drama = 1 "
			+ "</if>"
			+ "<if test='q.payType != null and q.payType == \"PAY\"'>"
			+ "  AND EXISTS (SELECT 1 FROM drama_asset da2 WHERE da2.drama_id = d.id AND da2.unlock_type = 2 "
			+ "    AND (da2.delete_state = 0 OR da2.delete_state IS NULL)) "
			+ "</if>"
			+ "<if test='q.payType != null and q.payType == \"AD\"'>"
			+ "  AND EXISTS (SELECT 1 FROM drama_asset da3 WHERE da3.drama_id = d.id AND da3.unlock_type = 1 "
			+ "    AND (da3.delete_state = 0 OR da3.delete_state IS NULL)) "
			+ "</if>"
			+ "GROUP BY d.id, d.drama_title, d.total_episodes "
			+ "ORDER BY dramaTotalRevenue DESC, d.id DESC "
			+ "</script>")
	List<DramaPaymentRespEntity> selectPayDrama(@Param("q") DramaPaymentQuery query);

	/** 短剧付费详情：剧信息 + KPI（不含剧集列表） */
	@Select("SELECT "
			+ "  d.id AS dramaId, "
			+ "  d.drama_title AS dramaName, "
			+ "  d.cover_url AS coverUrl, "
			+ "  IFNULL(( "
			+ "    SELECT GROUP_CONCAT(DISTINCT t.tag_name ORDER BY t.id SEPARATOR '、') "
			+ "    FROM drama_tag_rel r "
			+ "    INNER JOIN dic_drama_tag t ON t.group_id = r.tag_group_id "
			+ "    WHERE r.drama_id = d.id "
			+ "  ), '') AS dramaTag, "
			+ "  IFNULL(d.shelf_status, 0) AS shelfStatus, "
			+ "  IFNULL(d.total_episodes, 0) AS totalEpisodes, "
			+ "  (SELECT COUNT(1) FROM drama_asset da "
			+ "    WHERE da.drama_id = d.id AND da.unlock_type = 2 "
			+ "      AND (da.delete_state = 0 OR da.delete_state IS NULL)) AS paidEpisodes, "
			+ "  (SELECT COUNT(1) FROM drama_asset da0 "
			+ "    WHERE da0.drama_id = d.id AND IFNULL(da0.unlock_type, 0) = 0 "
			+ "      AND (da0.delete_state = 0 OR da0.delete_state IS NULL)) AS freeEpisodes, "
			+ "  IFNULL(d.shelf_time, d.setTime) AS releaseTime, "
			+ "  d.producer_firm AS producerFirm, "
			+ "  IFNULL(d.is_member_drama, 0) AS isMemberDrama, "
			+ "  EXISTS (SELECT 1 FROM drama_asset da1 WHERE da1.drama_id = d.id AND da1.unlock_type = 1 "
			+ "    AND (da1.delete_state = 0 OR da1.delete_state IS NULL)) AS adUnlockSupported, "
			+ "  (SELECT ROUND(AVG(da2.price_yuan), 2) FROM drama_asset da2 "
			+ "    WHERE da2.drama_id = d.id AND da2.unlock_type = 2 "
			+ "      AND da2.price_yuan IS NOT NULL "
			+ "      AND (da2.delete_state = 0 OR da2.delete_state IS NULL)) AS episodePriceAvg, "
			+ "  (SELECT MAX(da3.price_yuan) FROM drama_asset da3 "
			+ "    WHERE da3.drama_id = d.id AND da3.unlock_type = 2 "
			+ "      AND (da3.delete_state = 0 OR da3.delete_state IS NULL)) AS episodePriceOrigin, "
			+ "  IFNULL((SELECT SUM(IFNULL(ueu.cost_yuan, 0)) FROM user_episode_unlock ueu "
			+ "    WHERE ueu.drama_id = d.id AND ueu.unlock_way IN ('COIN','CASH')), 0) AS totalRevenue, "
			+ "  IFNULL((SELECT COUNT(1) FROM user_episode_unlock ueu2 "
			+ "    WHERE ueu2.drama_id = d.id AND ueu2.unlock_way IN ('COIN','CASH')), 0) AS purchaseCount, "
			+ "  IFNULL((SELECT "
			+ "    CASE WHEN (paid_cnt + refund_cnt) = 0 THEN 0 "
			+ "      ELSE ROUND(refund_cnt * 100.0 / (paid_cnt + refund_cnt), 2) END "
			+ "    FROM ("
			+ "      SELECT "
			+ "        SUM(CASE WHEN eo.pay_status = 1 THEN 1 ELSE 0 END) AS paid_cnt, "
			+ "        SUM(CASE WHEN eo.pay_status = 3 THEN 1 ELSE 0 END) AS refund_cnt "
			+ "      FROM episode_unlock_order eo WHERE eo.drama_id = d.id"
			+ "    ) r"
			+ "  ), 0) AS refundRate "
			+ "FROM drama d "
			+ "WHERE d.id = #{dramaId} AND (d.delete_state = 0 OR d.delete_state IS NULL) "
			+ "LIMIT 1")
	DramaPaymentDetailEntity selectPayDramaDetail(@Param("dramaId") Integer dramaId);

	/** 本月 vs 上月现金/金币解锁收入，用于环比 */
	@Select("SELECT "
			+ "  IFNULL(SUM(CASE WHEN ueu.setTime >= DATE_FORMAT(CURDATE(), '%Y-%m-01') "
			+ "    THEN IFNULL(ueu.cost_yuan, 0) ELSE 0 END), 0) AS thisMonth, "
			+ "  IFNULL(SUM(CASE WHEN ueu.setTime >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01') "
			+ "      AND ueu.setTime < DATE_FORMAT(CURDATE(), '%Y-%m-01') "
			+ "    THEN IFNULL(ueu.cost_yuan, 0) ELSE 0 END), 0) AS lastMonth "
			+ "FROM user_episode_unlock ueu "
			+ "WHERE ueu.drama_id = #{dramaId} AND ueu.unlock_way IN ('COIN','CASH')")
	java.util.Map<String, Object> selectPayDramaMom(@Param("dramaId") Integer dramaId);

	/** 付费集统计列表（按收入倒序） */
	@Select("<script>"
			+ "SELECT "
			+ "  da.id AS assetId, "
			+ "  da.set_num AS setNum, "
			+ "  IFNULL(da.video_name, CONCAT('第', da.set_num, '集')) AS episodeTitle, "
			+ "  da.unlock_type AS unlockType, "
			+ "  IFNULL(da.price_yuan, 0) AS priceYuan, "
			+ "  IFNULL(agg.purchaseCount, 0) AS purchaseCount, "
			+ "  IFNULL(agg.revenue, 0) AS revenue, "
			+ "  da.shelf_time AS shelfTime "
			+ "FROM drama_asset da "
			+ "LEFT JOIN ("
			+ "  SELECT ueu.asset_id AS assetId, "
			+ "    COUNT(1) AS purchaseCount, "
			+ "    SUM(IFNULL(ueu.cost_yuan, 0)) AS revenue "
			+ "  FROM user_episode_unlock ueu "
			+ "  WHERE ueu.drama_id = #{dramaId} AND ueu.unlock_way IN ('COIN','CASH') "
			+ "  GROUP BY ueu.asset_id"
			+ ") agg ON agg.assetId = da.id "
			+ "WHERE da.drama_id = #{dramaId} "
			+ "  AND da.unlock_type = 2 "
			+ "  AND (da.delete_state = 0 OR da.delete_state IS NULL) "
			+ "ORDER BY IFNULL(agg.revenue, 0) DESC, da.set_num ASC "
			+ "<if test='limit != null and limit &gt; 0'> LIMIT #{limit} </if>"
			+ "</script>")
	List<DramaEpisodePayItemEntity> selectEpisodePayList(@Param("dramaId") Integer dramaId,
			@Param("limit") Integer limit);

	/** 剧下购买记录：现金单集订单 ∪ 金币解锁 */
	@Select("<script>"
			+ "SELECT * FROM ("
			+ "  SELECT eo.order_no AS orderNo, eo.uid AS uid, a.nickname AS nickname, "
			+ "    CONCAT('第', IFNULL(da.set_num, '?'), '集单集解锁') AS purchaseContent, "
			+ "    eo.asset_id AS assetId, da.set_num AS setNum, "
			+ "    eo.pay_amount AS payAmount, IFNULL(eo.pay_channel, '') AS payChannel, "
			+ "    IFNULL(eo.pay_time, eo.setTime) AS payTime, eo.pay_status AS payStatus "
			+ "  FROM episode_unlock_order eo "
			+ "  LEFT JOIN app_account a ON a.id = eo.uid "
			+ "  LEFT JOIN drama_asset da ON da.id = eo.asset_id "
			+ "  WHERE eo.drama_id = #{q.dramaId} "
			+ "  UNION ALL "
			+ "  SELECT IFNULL(ueu.biz_no, CONCAT('COIN-', ueu.id)) AS orderNo, "
			+ "    ueu.uid AS uid, a2.nickname AS nickname, "
			+ "    CONCAT('第', IFNULL(da2.set_num, '?'), '集金币解锁') AS purchaseContent, "
			+ "    ueu.asset_id AS assetId, da2.set_num AS setNum, "
			+ "    IFNULL(ueu.cost_yuan, 0) AS payAmount, 'COIN' AS payChannel, "
			+ "    ueu.setTime AS payTime, 1 AS payStatus "
			+ "  FROM user_episode_unlock ueu "
			+ "  LEFT JOIN app_account a2 ON a2.id = ueu.uid "
			+ "  LEFT JOIN drama_asset da2 ON da2.id = ueu.asset_id "
			+ "  WHERE ueu.drama_id = #{q.dramaId} AND ueu.unlock_way = 'COIN' "
			+ ") u WHERE 1=1 "
			+ "<if test='q.keyword != null and q.keyword != \"\"'>"
			+ "  AND (u.orderNo = #{q.keyword}"
			+ "    OR CAST(u.uid AS CHAR) = #{q.keyword}"
			+ "    OR u.nickname LIKE CONCAT('%', #{q.keyword}, '%')) "
			+ "</if>"
			+ "<if test='q.assetId != null'> AND u.assetId = #{q.assetId} </if>"
			+ "<if test='q.payChannel != null and q.payChannel != \"\"'> AND u.payChannel = #{q.payChannel} </if>"
			+ "<if test='q.payStatus != null'> AND u.payStatus = #{q.payStatus} </if>"
			+ " ORDER BY u.payTime DESC "
			+ "</script>")
	List<DramaPurchaseItemEntity> selectDramaPurchaseList(@Param("q") DramaPaymentPurchaseQuery query);

	/** 剧下付费集下拉（筛选用） */
	@Select("SELECT da.id AS assetId, da.set_num AS setNum, "
			+ "  IFNULL(da.video_name, CONCAT('第', da.set_num, '集')) AS episodeTitle "
			+ "FROM drama_asset da "
			+ "WHERE da.drama_id = #{dramaId} AND da.unlock_type = 2 "
			+ "  AND (da.delete_state = 0 OR da.delete_state IS NULL) "
			+ "ORDER BY da.set_num ASC")
	List<DramaEpisodePayItemEntity> selectPaidEpisodeOptions(@Param("dramaId") Integer dramaId);
}
