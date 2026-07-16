package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.RankListEntity;
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
			+ "select * from rank_list where 1=1 "
			+ "<if test='boardGroupId != null and boardGroupId != \"\"'> and board_group_id = #{boardGroupId} </if>"
			+ "<if test='dramaId != null and dramaId != \"\"'> and drama_id = #{dramaId} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "<if test='title != null and title != \"\"'> and title like concat('%',#{title},'%') </if>"
			+ "order by board_group_id asc, rank_no asc"
			+ "</script>")
	List<RankListEntity> findAdminList(RankListEntity entity);

	@Select("select * from rank_list where board_group_id = #{boardGroupId} and status = 1 "
			+ "order by rank_no asc")
	List<RankListEntity> findEnabledByBoardGroupId(@Param("boardGroupId") String boardGroupId);

	@Delete("delete from rank_list where board_group_id = #{boardGroupId}")
	int deleteByBoardGroupId(@Param("boardGroupId") String boardGroupId);
}
