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

	@Select("select * from rank_list where board_code = #{boardCode} and drama_id = #{dramaId} limit 1")
	RankListEntity findByBoardAndDrama(@Param("boardCode") String boardCode, @Param("dramaId") String dramaId);

	@Select("select * from rank_list where board_code = #{boardCode} and rank_no = #{rankNo} limit 1")
	RankListEntity findByBoardAndRankNo(@Param("boardCode") String boardCode, @Param("rankNo") Integer rankNo);

	@Select("<script>"
			+ "select * from rank_list where 1=1 "
			+ "<if test='boardCode != null and boardCode != \"\"'> and board_code = #{boardCode} </if>"
			+ "<if test='dramaId != null and dramaId != \"\"'> and drama_id = #{dramaId} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "<if test='title != null and title != \"\"'> and title like concat('%',#{title},'%') </if>"
			+ "order by board_code asc, rank_no asc"
			+ "</script>")
	List<RankListEntity> findAdminList(RankListEntity entity);

	@Select("select * from rank_list where board_code = #{boardCode} and status = 1 "
			+ "order by rank_no asc")
	List<RankListEntity> findEnabledByBoardCode(@Param("boardCode") String boardCode);

	@Delete("delete from rank_list where board_code = #{boardCode}")
	int deleteByBoardCode(@Param("boardCode") String boardCode);
}
