package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.RankBoardEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankBoardDao extends BaseMapper<RankBoardEntity> {

	@Select("select * from rank_board where board_code = #{boardCode} limit 1")
	RankBoardEntity findByBoardCode(@Param("boardCode") String boardCode);

	@Select("<script>"
			+ "select * from rank_board where 1=1 "
			+ "<if test='boardCode != null and boardCode != \"\"'> and board_code = #{boardCode} </if>"
			+ "<if test='boardName != null and boardName != \"\"'> and board_name like concat('%',#{boardName},'%') </if>"
			+ "<if test='boardType != null'> and board_type = #{boardType} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "order by sort_weight desc, id asc"
			+ "</script>")
	List<RankBoardEntity> findAdminList(RankBoardEntity entity);

	@Select("select * from rank_board where status = 1 order by sort_weight desc, id asc")
	List<RankBoardEntity> findEnabledList();
}
