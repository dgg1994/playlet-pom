package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorMarketInfoEntity;
import com.playlet.internal.query.creator.TalentMarketListQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 人才市场展示信息查询。
 */
@Repository
public interface CreatorMarketInfoDao extends BaseMapper<CreatorMarketInfoEntity> {

	/** 市场可见：未删除 + 已上架 */
	@Select("select * from creator_market_info where id = #{id} "
			+ "and ifnull(delete_state,0) = 0 and shelf_status = 1 limit 1")
	CreatorMarketInfoEntity findVisibleById(@Param("id") Integer id);

	@Select("<script>"
			+ "select * from creator_market_info "
			+ "where ifnull(delete_state,0) = 0 and shelf_status = 1 and market_role = #{q.role} "
			+ "<if test='q.keyword != null and q.keyword != \"\"'>"
			+ "  and (name like concat('%', #{q.keyword}, '%') "
			+ "    or badge like concat('%', #{q.keyword}, '%') "
			+ "    or tags like concat('%', #{q.keyword}, '%') "
			+ "    or services like concat('%', #{q.keyword}, '%') "
			+ "    or skills like concat('%', #{q.keyword}, '%') "
			+ "    or city like concat('%', #{q.keyword}, '%')) "
			+ "</if>"
			+ "<if test='qualType != null'> and qual_type = #{qualType} </if>"
			+ "<if test='q.service != null and q.service != \"\"'>"
			+ "  and (services like concat('%', #{q.service}, '%') or tags like concat('%', #{q.service}, '%')) "
			+ "</if>"
			+ "<if test='q.production != null and q.production != \"\"'> and production = #{q.production} </if>"
			+ "<if test='q.skill != null and q.skill != \"\"'> and skills like concat('%', #{q.skill}, '%') </if>"
			+ "<if test='q.style != null and q.style != \"\"'> and style = #{q.style} </if>"
			+ "<if test='q.contract != null and q.contract != \"\"'> and services like concat('%', #{q.contract}, '%') </if>"
			+ "<if test='q.priceRange != null and q.priceRange == 1'> and price &lt; 10000 </if>"
			+ "<if test='q.priceRange != null and q.priceRange == 2'> and price &gt;= 10000 and price &lt; 50000 </if>"
			+ "<if test='q.priceRange != null and q.priceRange == 3'> and price &gt;= 50000 and price &lt; 100000 </if>"
			+ "<if test='q.priceRange != null and q.priceRange == 4'> and price &gt;= 100000 </if>"
			+ "<choose>"
			+ "  <when test='q.sortType != null and q.sortType == 2'> order by deals desc, id asc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 3'> order by score desc, id asc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 4'> order by price asc, id asc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 5'> order by works desc, id asc </when>"
			+ "  <when test='q.sortType != null and q.sortType == 6'> order by revenue desc, id asc </when>"
			+ "  <otherwise> order by sort_weight desc, score desc, id asc </otherwise>"
			+ "</choose>"
			+ "</script>")
	List<CreatorMarketInfoEntity> findMarketList(@Param("q") TalentMarketListQuery q,
			@Param("qualType") Integer qualType);
}
