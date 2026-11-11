package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorCallEntity;
import com.playlet.internal.query.creator.CallSquareListQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 征稿广场征稿信息查询。
 */
@Repository
public interface CreatorCallDao extends BaseMapper<CreatorCallEntity> {

	/** 广场可见：未删除 + 已上架 */
	@Select("select * from creator_call where id = #{id} "
			+ "and ifnull(delete_state,0) = 0 and shelf_status = 1 limit 1")
	CreatorCallEntity findVisibleById(@Param("id") Integer id);

	@Select("<script>"
			+ "select * from creator_call "
			+ "where ifnull(delete_state,0) = 0 and shelf_status = 1 "
			+ "<if test='q.keyword != null and q.keyword != \"\"'>"
			+ "  and (title like concat('%', #{q.keyword}, '%') "
			+ "    or intro like concat('%', #{q.keyword}, '%') "
			+ "    or benchmark like concat('%', #{q.keyword}, '%') "
			+ "    or tags like concat('%', #{q.keyword}, '%')) "
			+ "</if>"
			+ "<if test='q.topic != null and q.topic != \"\"'> and topic = #{q.topic} </if>"
			+ "<if test='q.deadlineStart != null and q.deadlineStart != \"\"'> and deadline &gt;= #{q.deadlineStart} </if>"
			+ "<if test='q.deadlineEnd != null and q.deadlineEnd != \"\"'> and deadline &lt;= #{q.deadlineEnd} </if>"
			+ "<if test='q.status != null and q.status == 1'>"
			+ "  and deadline &gt; date_add(curdate(), interval 15 day) "
			+ "</if>"
			+ "<if test='q.status != null and q.status == 2'>"
			+ "  and deadline &gt;= curdate() and deadline &lt;= date_add(curdate(), interval 15 day) "
			+ "</if>"
			+ "<if test='q.status != null and q.status == 3'> and deadline &lt; curdate() </if>"
			+ "<choose>"
			+ "  <when test='q.sortType != null and q.sortType == 2'> order by publish_at desc, id desc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 3'> order by amount desc, id asc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 4'> order by participants desc, id asc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 5'> order by deadline asc, id asc </when>"
			+ "  <otherwise> order by sort_weight desc, id asc </otherwise>"
			+ "</choose>"
			+ "</script>")
	List<CreatorCallEntity> findSquareList(@Param("q") CallSquareListQuery q);

	/** 相关征稿：同题材优先，不足时按综合权重补足 */
	@Select("select * from creator_call where id != #{excludeId} "
			+ "and ifnull(delete_state,0) = 0 and shelf_status = 1 "
			+ "order by (topic = #{topic}) desc, sort_weight desc, id asc limit 3")
	List<CreatorCallEntity> findRelated(@Param("excludeId") Integer excludeId, @Param("topic") String topic);
}
