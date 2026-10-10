package com.playlet.oversea.dao.prefer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.api.response.WatchPreferTagRespEntity;
import com.playlet.oversea.entity.prefer.WatchPreferTagEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 看剧喜好-选项字典 Dao。
 */
@Repository
public interface WatchPreferTagDao extends BaseMapper<WatchPreferTagEntity> {

	/**
	 * 按语言查全部启用选项，按分类权重外层由业务组装；此处按分类码 + 选项权重排序。
	 */
	@Select("select * from dic_watch_prefer_tag "
			+ "where langue = #{langue} and status = #{status} "
			+ "order by category_code asc, sort_weight desc, id asc")
	List<WatchPreferTagEntity> findByLangueAndStatus(@Param("langue") String langue,
			@Param("status") Integer status);

	/**
	 * 按分类 + 语言查启用选项。
	 */
	@Select("select * from dic_watch_prefer_tag "
			+ "where category_code = #{categoryCode} and langue = #{langue} and status = #{status} "
			+ "order by sort_weight desc, id asc")
	List<WatchPreferTagEntity> findByCategoryAndLangue(@Param("categoryCode") String categoryCode,
			@Param("langue") String langue, @Param("status") Integer status);

	/**
	 * 按 group_id 列表 + 语言查启用选项（save 校验用）。
	 */
	@Select("<script>"
			+ "select * from dic_watch_prefer_tag "
			+ "where langue = #{langue} and status = #{status} and group_id in "
			+ "<foreach collection='groupIds' item='gid' open='(' separator=',' close=')'>#{gid}</foreach> "
			+ "order by sort_weight desc, id asc"
			+ "</script>")
	List<WatchPreferTagEntity> findEnabledByGroupIds(@Param("groupIds") List<String> groupIds,
			@Param("langue") String langue, @Param("status") Integer status);

	/**
	 * 按 group_id + 语言查单条。
	 */
	@Select("select * from dic_watch_prefer_tag "
			+ "where group_id = #{groupId} and langue = #{langue} limit 1")
	WatchPreferTagEntity findByGroupIdAndLangue(@Param("groupId") String groupId,
			@Param("langue") String langue);

	@Select("select group_id as groupId,tag_name as tagName from dic_watch_prefer_tag where category_code = #{categoryCode}")
    List<WatchPreferTagRespEntity> selectListByCode(@Param("categoryCode") String categoryCode);
}
