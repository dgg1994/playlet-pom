package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.DramaRankAggRow;
import com.playlet.internal.entity.drama.DramaRankStatDailyEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaRankStatDailyDao extends BaseMapper<DramaRankStatDailyEntity> {

	@Insert("insert into drama_rank_stat_daily "
			+ "(biz_date, drama_id, play_pv, valid_seconds, collect_cnt, like_cnt, comment_cnt, search_cnt, setTime, gmtModified) "
			+ "values (#{bizDate}, #{dramaId}, #{playPv}, #{validSeconds}, #{collectCnt}, #{likeCnt}, 0, #{searchCnt}, now(), now()) "
			+ "on duplicate key update "
			+ "play_pv = play_pv + values(play_pv), "
			+ "valid_seconds = valid_seconds + values(valid_seconds), "
			+ "collect_cnt = greatest(collect_cnt + values(collect_cnt), 0), "
			+ "like_cnt = greatest(like_cnt + values(like_cnt), 0), "
			+ "search_cnt = search_cnt + values(search_cnt), "
			+ "gmtModified = now()")
	int upsertDelta(@Param("bizDate") String bizDate,
			@Param("dramaId") Integer dramaId,
			@Param("playPv") int playPv,
			@Param("validSeconds") int validSeconds,
			@Param("collectCnt") int collectCnt,
			@Param("likeCnt") int likeCnt,
			@Param("searchCnt") int searchCnt);

	/**
	 * 热播/新剧：窗口内聚合后按综合分排序。
	 * newSince 非空时仅保留 setTime &gt;= newSince 的剧（新剧榜）。
	 */
	@Select("<script>"
			+ "select d.id as dramaId, d.drama_title as dramaTitle, d.cover_url as coverUrl, "
			+ "d.hot_score_text as hotScoreText, d.total_episodes as totalEpisodes, d.finished_state as finishedState, "
			+ "ifnull(a.validSeconds,0) as validSeconds, ifnull(a.collectCnt,0) as collectCnt, "
			+ "ifnull(a.likeCnt,0) as likeCnt, ifnull(a.playPv,0) as playPv, "
			+ "(ifnull(a.validSeconds,0)*0.6 + ifnull(a.collectCnt,0)*0.2 "
			+ "+ ifnull(a.likeCnt,0)*0.1 + ifnull(a.playPv,0)*0.1) as algoScore "
			+ "from drama d "
			+ "left join ("
			+ "  select drama_id, "
			+ "    sum(valid_seconds) as validSeconds, "
			+ "    sum(collect_cnt) as collectCnt, "
			+ "    sum(like_cnt) as likeCnt, "
			+ "    sum(play_pv) as playPv "
			+ "  from drama_rank_stat_daily "
			+ "  where biz_date &gt;= #{fromDate} "
			+ "  group by drama_id"
			+ ") a on a.drama_id = d.id "
			+ "where d.verify_status = 1 and ifnull(d.delete_state,0) = 0 "
			+ "<if test='newSince != null'> and d.setTime &gt;= #{newSince} </if>"
			+ "order by algoScore desc, d.id desc "
			+ "limit #{limit}"
			+ "</script>")
	List<DramaRankAggRow> findHotPlayCandidates(@Param("fromDate") String fromDate,
			@Param("newSince") String newSince,
			@Param("limit") int limit);

	/** 收藏榜：窗口内收藏数排序 */
	@Select("select d.id as dramaId, d.drama_title as dramaTitle, d.cover_url as coverUrl, "
			+ "d.hot_score_text as hotScoreText, d.total_episodes as totalEpisodes, d.finished_state as finishedState, "
			+ "ifnull(a.validSeconds,0) as validSeconds, ifnull(a.collectCnt,0) as collectCnt, "
			+ "ifnull(a.likeCnt,0) as likeCnt, ifnull(a.playPv,0) as playPv, "
			+ "ifnull(a.collectCnt,0) as algoScore "
			+ "from drama d "
			+ "inner join ("
			+ "  select drama_id, "
			+ "    sum(valid_seconds) as validSeconds, "
			+ "    sum(collect_cnt) as collectCnt, "
			+ "    sum(like_cnt) as likeCnt, "
			+ "    sum(play_pv) as playPv "
			+ "  from drama_rank_stat_daily "
			+ "  where biz_date >= #{fromDate} "
			+ "  group by drama_id "
			+ "  having sum(collect_cnt) > 0"
			+ ") a on a.drama_id = d.id "
			+ "where d.verify_status = 1 and ifnull(d.delete_state,0) = 0 "
			+ "order by a.collectCnt desc, d.id desc "
			+ "limit #{limit}")
	List<DramaRankAggRow> findCollectCandidates(@Param("fromDate") String fromDate,
			@Param("limit") int limit);

	/**
	 * 飙升榜：近窗有效观看/播放增量 vs 前窗。
	 * score = (recentValid-prevValid)*0.7 + (recentPlay-prevPlay)*0.3
	 */
	@Select("select d.id as dramaId, d.drama_title as dramaTitle, d.cover_url as coverUrl, "
			+ "d.hot_score_text as hotScoreText, d.total_episodes as totalEpisodes, d.finished_state as finishedState, "
			+ "ifnull(r.validSeconds,0) as validSeconds, 0 as collectCnt, 0 as likeCnt, "
			+ "ifnull(r.playPv,0) as playPv, "
			+ "((ifnull(r.validSeconds,0) - ifnull(p.validSeconds,0)) * 0.7 "
			+ "+ (ifnull(r.playPv,0) - ifnull(p.playPv,0)) * 0.3) as algoScore "
			+ "from drama d "
			+ "inner join ("
			+ "  select drama_id, sum(valid_seconds) as validSeconds, sum(play_pv) as playPv "
			+ "  from drama_rank_stat_daily "
			+ "  where biz_date >= #{recentFrom} and biz_date <= #{today} "
			+ "  group by drama_id"
			+ ") r on r.drama_id = d.id "
			+ "left join ("
			+ "  select drama_id, sum(valid_seconds) as validSeconds, sum(play_pv) as playPv "
			+ "  from drama_rank_stat_daily "
			+ "  where biz_date >= #{prevFrom} and biz_date <= #{prevTo} "
			+ "  group by drama_id"
			+ ") p on p.drama_id = d.id "
			+ "where d.verify_status = 1 and ifnull(d.delete_state,0) = 0 "
			+ "  and ifnull(r.validSeconds,0) >= #{minValidSeconds} "
			+ "  and ifnull(r.validSeconds,0) > ifnull(p.validSeconds,0) "
			+ "order by algoScore desc, d.id desc "
			+ "limit #{limit}")
	List<DramaRankAggRow> findRisingCandidates(@Param("today") String today,
			@Param("recentFrom") String recentFrom,
			@Param("prevFrom") String prevFrom,
			@Param("prevTo") String prevTo,
			@Param("minValidSeconds") int minValidSeconds,
			@Param("limit") int limit);

	/** 热搜榜：窗口内搜索命中次数排序 */
	@Select("select d.id as dramaId, d.drama_title as dramaTitle, d.cover_url as coverUrl, "
			+ "d.hot_score_text as hotScoreText, d.total_episodes as totalEpisodes, d.finished_state as finishedState, "
			+ "0 as validSeconds, 0 as collectCnt, 0 as likeCnt, 0 as playPv, "
			+ "ifnull(a.searchCnt,0) as algoScore "
			+ "from drama d "
			+ "inner join ("
			+ "  select drama_id, sum(search_cnt) as searchCnt "
			+ "  from drama_rank_stat_daily "
			+ "  where biz_date >= #{fromDate} "
			+ "  group by drama_id "
			+ "  having sum(search_cnt) > 0"
			+ ") a on a.drama_id = d.id "
			+ "where d.verify_status = 1 and ifnull(d.delete_state,0) = 0 "
			+ "order by a.searchCnt desc, d.id desc "
			+ "limit #{limit}")
	List<DramaRankAggRow> findHotSearchCandidates(@Param("fromDate") String fromDate,
			@Param("limit") int limit);

	/**
	 * 推荐榜：7 日综合分 + 近 freshDays 上架新鲜度加分。
	 * score = valid*0.35 + collect*0.25 + like*0.15 + search*0.10 + play*0.15
	 *       + max(0, freshDays - ageDays) * freshWeightPerDay
	 */
	@Select("select d.id as dramaId, d.drama_title as dramaTitle, d.cover_url as coverUrl, "
			+ "d.hot_score_text as hotScoreText, d.total_episodes as totalEpisodes, d.finished_state as finishedState, "
			+ "ifnull(a.validSeconds,0) as validSeconds, ifnull(a.collectCnt,0) as collectCnt, "
			+ "ifnull(a.likeCnt,0) as likeCnt, ifnull(a.playPv,0) as playPv, "
			+ "(ifnull(a.validSeconds,0)*0.35 + ifnull(a.collectCnt,0)*0.25 "
			+ "+ ifnull(a.likeCnt,0)*0.15 + ifnull(a.searchCnt,0)*0.10 + ifnull(a.playPv,0)*0.15 "
			+ "+ greatest(0, #{freshDays} - timestampdiff(DAY, d.setTime, now())) * #{freshWeightPerDay}"
			+ ") as algoScore "
			+ "from drama d "
			+ "left join ("
			+ "  select drama_id, "
			+ "    sum(valid_seconds) as validSeconds, "
			+ "    sum(collect_cnt) as collectCnt, "
			+ "    sum(like_cnt) as likeCnt, "
			+ "    sum(play_pv) as playPv, "
			+ "    sum(search_cnt) as searchCnt "
			+ "  from drama_rank_stat_daily "
			+ "  where biz_date >= #{fromDate} "
			+ "  group by drama_id"
			+ ") a on a.drama_id = d.id "
			+ "where d.verify_status = 1 and ifnull(d.delete_state,0) = 0 "
			+ "  and d.setTime is not null "
			+ "  and ("
			+ "    ifnull(a.validSeconds,0) + ifnull(a.collectCnt,0) + ifnull(a.likeCnt,0) "
			+ "    + ifnull(a.playPv,0) + ifnull(a.searchCnt,0) > 0 "
			+ "    or d.setTime >= #{freshSince}"
			+ "  ) "
			+ "order by algoScore desc, d.id desc "
			+ "limit #{limit}")
	List<DramaRankAggRow> findRecommendCandidates(@Param("fromDate") String fromDate,
			@Param("freshSince") String freshSince,
			@Param("freshDays") int freshDays,
			@Param("freshWeightPerDay") int freshWeightPerDay,
			@Param("limit") int limit);
}
