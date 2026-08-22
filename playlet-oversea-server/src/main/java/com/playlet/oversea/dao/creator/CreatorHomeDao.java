package com.playlet.oversea.dao.creator;

import com.playlet.oversea.api.response.CreatorHomeHotTagAggRow;
import com.playlet.oversea.api.response.CreatorHomeRankAggRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作家首页统计与榜单查询。
 */
@Repository
public interface CreatorHomeDao {

	/** 指定自然日所属剧 play_pv 合计 */
	@Select("select ifnull(sum(s.play_pv), 0) from drama_rank_stat_daily s "
			+ "inner join drama d on d.id = s.drama_id "
			+ "  and d.belong_user = #{creatorId} and ifnull(d.delete_state, 0) = 0 "
			+ "where s.biz_date = #{bizDate}")
	Long sumPlayPvByCreatorAndDate(@Param("creatorId") Integer creatorId, @Param("bizDate") String bizDate);

	/** 在播短剧数：本人且已上架 */
	@Select("select ifnull(count(1), 0) from drama "
			+ "where belong_user = #{creatorId} and ifnull(delete_state, 0) = 0 "
			+ "and ifnull(shelf_status, 0) = 1")
	Integer countOnAirDrama(@Param("creatorId") Integer creatorId);

	/** 在播剧集数：本人剧下已上架集 */
	@Select("select ifnull(count(1), 0) from drama_asset a "
			+ "inner join drama d on d.id = a.drama_id "
			+ "  and d.belong_user = #{creatorId} and ifnull(d.delete_state, 0) = 0 "
			+ "where ifnull(a.delete_state, 0) = 0 and ifnull(a.shelf_status, 0) = 1")
	Integer countOnAirEpisode(@Param("creatorId") Integer creatorId);

	/**
	 * 热点题材：近窗有播放的剧关联标签，按命中剧数排序。
	 */
	@Select("select t.group_id as groupId, t.tag_name as tagName, count(distinct r.drama_id) as hitCnt "
			+ "from drama_tag_rel r "
			+ "inner join drama d on d.id = r.drama_id "
			+ "  and d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "inner join ("
			+ "  select drama_id from drama_rank_stat_daily "
			+ "  where biz_date >= #{fromDate} "
			+ "  group by drama_id "
			+ "  having ifnull(sum(play_pv), 0) + ifnull(sum(valid_seconds), 0) > 0"
			+ ") s on s.drama_id = d.id "
			+ "inner join dic_drama_tag t on t.group_id = r.tag_group_id "
			+ "  and t.langue = #{langue} and ifnull(t.status, 0) = 1 "
			+ "group by t.group_id, t.tag_name "
			+ "order by hitCnt desc, t.group_id asc "
			+ "limit #{limit}")
	List<CreatorHomeHotTagAggRow> findHotTags(@Param("fromDate") String fromDate,
			@Param("langue") String langue,
			@Param("limit") int limit);

	/**
	 * 影响力：近窗所属剧 valid_seconds 合计。
	 */
	@Select("select ca.id as creatorId, "
			+ "ifnull(nullif(ca.nickname, ''), ca.user_account) as nickname, "
			+ "ifnull(sum(s.valid_seconds), 0) as score "
			+ "from creator_account ca "
			+ "inner join drama d on d.belong_user = ca.id "
			+ "  and d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "inner join drama_rank_stat_daily s on s.drama_id = d.id and s.biz_date >= #{fromDate} "
			+ "where ifnull(ca.user_state, 0) = 1 "
			+ "group by ca.id, ca.nickname, ca.user_account "
			+ "having score > 0 "
			+ "order by score desc, ca.id asc "
			+ "limit #{limit}")
	List<CreatorHomeRankAggRow> findInfluenceRank(@Param("fromDate") String fromDate,
			@Param("limit") int limit);

	/**
	 * 成长力：近窗 valid_seconds / 前窗 valid_seconds，分值 = 倍率×100 取整。
	 */
	@Select("select ca.id as creatorId, "
			+ "ifnull(nullif(ca.nickname, ''), ca.user_account) as nickname, "
			+ "floor(ifnull(r.validSeconds, 0) * 100.0 / ifnull(p.validSeconds, 0)) as score "
			+ "from creator_account ca "
			+ "inner join ("
			+ "  select d.belong_user as creatorId, sum(s.valid_seconds) as validSeconds "
			+ "  from drama d "
			+ "  inner join drama_rank_stat_daily s on s.drama_id = d.id "
			+ "  where s.biz_date >= #{recentFrom} and s.biz_date <= #{today} "
			+ "    and d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "  group by d.belong_user"
			+ ") r on r.creatorId = ca.id "
			+ "inner join ("
			+ "  select d.belong_user as creatorId, sum(s.valid_seconds) as validSeconds "
			+ "  from drama d "
			+ "  inner join drama_rank_stat_daily s on s.drama_id = d.id "
			+ "  where s.biz_date >= #{prevFrom} and s.biz_date <= #{prevTo} "
			+ "    and d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "  group by d.belong_user"
			+ ") p on p.creatorId = ca.id "
			+ "where ifnull(ca.user_state, 0) = 1 "
			+ "  and ifnull(r.validSeconds, 0) >= #{minRecentSeconds} "
			+ "  and ifnull(p.validSeconds, 0) > 0 "
			+ "  and ifnull(r.validSeconds, 0) > ifnull(p.validSeconds, 0) "
			+ "order by score desc, ca.id asc "
			+ "limit #{limit}")
	List<CreatorHomeRankAggRow> findGrowthRank(@Param("today") String today,
			@Param("recentFrom") String recentFrom,
			@Param("prevFrom") String prevFrom,
			@Param("prevTo") String prevTo,
			@Param("minRecentSeconds") int minRecentSeconds,
			@Param("limit") int limit);
}
