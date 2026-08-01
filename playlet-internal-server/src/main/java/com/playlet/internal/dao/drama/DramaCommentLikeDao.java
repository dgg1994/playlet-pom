package com.playlet.internal.dao.drama;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaCommentLikeEntity;

import java.util.List;

@Repository
public interface DramaCommentLikeDao extends BaseMapper<DramaCommentLikeEntity>{

	@Delete("delete from drama_comment_like where comment_id = #{commentId} and user_id = #{userId}")
	void deleteByUser(@Param("commentId") Integer commentId,@Param("userId") Integer userId);

	@Select("select * from drama_comment_like where comment_id = #{commentId} and user_id = #{userId}")
	DramaCommentLikeEntity findOne(@Param("commentId") Integer commentId,@Param("userId") Integer userId);

	/** 当前用户已点赞的评论 id（评论点赞表 drama_comment_like，非 user_drama_like） */
	@Select("<script>"
			+ "select comment_id from drama_comment_like "
			+ "where user_id = #{userId} and comment_id in "
			+ "<foreach collection='commentIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach>"
			+ "</script>")
	List<Long> findLikedCommentIds(@Param("userId") Integer userId,
			@Param("commentIds") List<Integer> commentIds);

}
