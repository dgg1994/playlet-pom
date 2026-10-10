package com.playlet.oversea.dao.prefer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.prefer.WatchPreferCategoryEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 看剧喜好-分类字典 Dao。
 */
@Repository
public interface WatchPreferCategoryDao extends BaseMapper<WatchPreferCategoryEntity> {

	/**
	 * 按语言查启用中的分类，按权重倒序。
	 */
	@Select("select * from dic_watch_prefer_category "
			+ "where langue = #{langue} and status = #{status} "
			+ "order by sort_weight desc, id asc")
	List<WatchPreferCategoryEntity> findByLangueAndStatus(@Param("langue") String langue,
			@Param("status") Integer status);

	/**
	 * 按分类编码 + 语言查单条。
	 */
	@Select("select * from dic_watch_prefer_category "
			+ "where category_code = #{categoryCode} and langue = #{langue} limit 1")
	WatchPreferCategoryEntity findByCodeAndLangue(@Param("categoryCode") String categoryCode,
			@Param("langue") String langue);

}
