package com.playlet.internal.dao.drama;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaCommentLikeEntity;

@Repository
public interface DramaCommentLikeDao extends BaseMapper<DramaCommentLikeEntity>{

	@Delete("delete from drama_comment_like where comment_id = #{commentId} and user_id = #{userId}")
	void deleteByUser(@Param("commentId") Integer commentId,@Param("userId") Integer userId);

	@Select("select * from drama_comment_like where comment_id = #{commentId} and user_id = #{userId}")
	DramaCommentLikeEntity findOne(@Param("commentId") Integer commentId,@Param("userId") Integer userId);

}
