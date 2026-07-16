package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.RankBoardEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankBoardDao extends BaseMapper<RankBoardEntity> {

	@Select("select * from rank_board where board_name = #{boardName} and langue = #{langue} limit 1")
	RankBoardEntity findByBoardNameAndLangue(@Param("boardName") String boardName, @Param("langue") String langue);

	@Select("select * from rank_board where group_id = #{groupId} and langue = #{langue} limit 1")
	RankBoardEntity findByGroupIdAndLangue(@Param("groupId") String groupId, @Param("langue") String langue);

	@Select("select * from rank_board where group_id = #{groupId} order by id asc limit 1")
	RankBoardEntity findOneByGroupId(@Param("groupId") String groupId);

	@Select("<script>"
			+ "select * from rank_board where 1=1 "
			+ "<if test='groupId != null and groupId != \"\"'> and group_id = #{groupId} </if>"
			+ "<if test='langue != null and langue != \"\"'> and langue = #{langue} </if>"
			+ "<if test='boardName != null and boardName != \"\"'> and board_name like concat('%',#{boardName},'%') </if>"
			+ "<if test='boardType != null'> and board_type = #{boardType} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "order by sort_weight desc, id asc"
			+ "</script>")
	List<RankBoardEntity> findAdminList(RankBoardEntity entity);

	@Select("<script>"
			+ "select * from rank_board where status = 1 "
			+ "<if test='langue != null and langue != \"\"'> and langue = #{langue} </if>"
			+ "order by sort_weight desc, id asc"
			+ "</script>")
	List<RankBoardEntity> findEnabledList(@Param("langue") String langue);

	@Select("select * from rank_board where group_id = #{groupId}")
	List<RankBoardEntity> selectGroupId(@Param("groupId") String groupId);

	@Update("update rank_board set status = #{status},gmtModified = NOW() where group_id = #{groupId}")
	void updateByGroupId(@Param("groupId") String groupId, @Param("status") Integer status);
}
