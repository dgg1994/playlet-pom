package com.playlet.oversea.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.api.response.RankListItemEntity;
import com.playlet.oversea.entity.drama.RankListEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankListDao extends BaseMapper<RankListEntity> {

	@Select("select * from rank_list where board_group_id = #{boardGroupId} and drama_id = #{dramaId} limit 1")
	RankListEntity findByBoardAndDrama(@Param("boardGroupId") String boardGroupId, @Param("dramaId") String dramaId);

	@Select("select * from rank_list where board_group_id = #{boardGroupId} and rank_no = #{rankNo} limit 1")
	RankListEntity findByBoardAndRankNo(@Param("boardGroupId") String boardGroupId, @Param("rankNo") Integer rankNo);

	@Select("<script>"
			+ "select rl.id, rl.board_group_id as boardGroupId, rl.rank_no as rankNo, rl.drama_id as dramaId, "
			+ "rl.status, rl.setTime, rl.gmtModified, "
			+ "d.drama_title as title, d.cover_url as coverUrl, d.hot_score_text as hotScoreText, "
			+ "d.hot_score as hotScore, d.total_episodes as totalEpisodes, d.finished_state as finished "
			+ "from rank_list rl "
			+ "left join drama d on d.id = rl.drama_id "
			+ "where 1=1 "
			+ "<if test='boardGroupId != null and boardGroupId != \"\"'> and rl.board_group_id = #{boardGroupId} </if>"
			+ "<if test='dramaId != null and dramaId != \"\"'> and rl.drama_id = #{dramaId} </if>"
			+ "<if test='status != null'> and rl.status = #{status} </if>"
			+ "<if test='title != null and title != \"\"'> and d.drama_title like concat('%',#{title},'%') </if>"
			+ "order by rl.board_group_id asc, rl.rank_no asc"
			+ "</script>")
	List<RankListItemEntity> findAdminList(RankListEntity entity);

	@Select("select rl.id, rl.board_group_id as boardGroupId, rl.rank_no as rankNo, rl.drama_id as dramaId, "
			+ "rl.status, rl.setTime, rl.gmtModified, "
			+ "d.drama_title as title, d.cover_url as coverUrl, d.hot_score_text as hotScoreText, "
			+ "d.hot_score as hotScore, d.total_episodes as totalEpisodes, d.finished_state as finished "
			+ "from rank_list rl "
			+ "inner join drama d on d.id = rl.drama_id "
			+ "where rl.board_group_id = #{boardGroupId} and rl.status = 1 "
			+ "and d.verify_status = #{verifyStatus} and ifnull(d.delete_state, 0) = #{deleteState} "
			+ "order by rl.rank_no asc")
	List<RankListItemEntity> findEnabledWithDrama(@Param("boardGroupId") String boardGroupId,
			@Param("verifyStatus") Integer verifyStatus,
			@Param("deleteState") Integer deleteState);

	@Select("select rl.id, rl.board_group_id as boardGroupId, rl.rank_no as rankNo, rl.drama_id as dramaId, "
			+ "rl.status, rl.setTime, rl.gmtModified, "
			+ "d.drama_title as title, d.cover_url as coverUrl, d.hot_score_text as hotScoreText, "
			+ "d.hot_score as hotScore, d.total_episodes as totalEpisodes, d.finished_state as finished "
			+ "from rank_list rl "
			+ "inner join drama d on d.id = rl.drama_id "
			+ "where rl.board_group_id = #{boardGroupId} and rl.status = 1 "
			+ "and d.verify_status = #{verifyStatus} and ifnull(d.delete_state, 0) = #{deleteState} "
			+ "order by rl.rank_no asc limit #{limit}")
	List<RankListItemEntity> findEnabledWithDramaLimit(@Param("boardGroupId") String boardGroupId,
			@Param("verifyStatus") Integer verifyStatus,
			@Param("deleteState") Integer deleteState,
			@Param("limit") int limit);

	@Select("select rl.id, rl.board_group_id as boardGroupId, rl.rank_no as rankNo, rl.drama_id as dramaId, "
			+ "rl.status, rl.setTime, rl.gmtModified, "
			+ "d.drama_title as title, d.cover_url as coverUrl, d.hot_score_text as hotScoreText, "
			+ "d.hot_score as hotScore, d.total_episodes as totalEpisodes, d.finished_state as finished "
			+ "from rank_list rl "
			+ "left join drama d on d.id = rl.drama_id "
			+ "where rl.id = #{id} limit 1")
	RankListItemEntity findItemById(@Param("id") Integer id);

	@Delete("delete from rank_list where board_group_id = #{boardGroupId}")
	int deleteByBoardGroupId(@Param("boardGroupId") String boardGroupId);
}
