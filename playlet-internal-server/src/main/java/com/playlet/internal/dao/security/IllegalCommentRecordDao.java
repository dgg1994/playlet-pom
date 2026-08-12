package com.playlet.internal.dao.security;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.security.IllegalCommentRecordEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IllegalCommentRecordDao extends BaseMapper<IllegalCommentRecordEntity> {

	@Select("<script>"
			+ "select * from illegal_comment_record where 1=1 "
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "<if test='riskLevel != null'> and risk_level = #{riskLevel} </if>"
			+ "<if test='userId != null'> and user_id = #{userId} </if>"
			+ "<if test='dramaId != null'> and drama_id = #{dramaId} </if>"
			+ "<if test='sourceType != null'> and source_type = #{sourceType} </if>"
			+ "<if test='commentId != null'> and comment_id = #{commentId} </if>"
			+ "order by setTime desc, id desc"
			+ "</script>")
	List<IllegalCommentRecordEntity> findAdminList(IllegalCommentRecordEntity entity);

	@Select("select count(1) from illegal_comment_record where id = #{id} and status = 0")
	int countPendingById(@Param("id") Long id);
}
