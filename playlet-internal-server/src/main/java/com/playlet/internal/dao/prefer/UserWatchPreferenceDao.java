package com.playlet.internal.dao.prefer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.prefer.UserWatchPreferenceEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户看剧喜好 Dao。
 */
@Repository
public interface UserWatchPreferenceDao extends BaseMapper<UserWatchPreferenceEntity> {

	/**
	 * 查用户全部已选偏好，按权重倒序。
	 */
	@Select("select * from user_watch_preference where uid = #{uid} "
			+ "order by sort_weight desc, id asc")
	List<UserWatchPreferenceEntity> findByUid(@Param("uid") Integer uid);

	/**
	 * 按用户 + 分类查已选偏好。
	 */
	@Select("select * from user_watch_preference "
			+ "where uid = #{uid} and category_code = #{categoryCode} "
			+ "order by sort_weight desc, id asc")
	List<UserWatchPreferenceEntity> findByUidAndCategory(@Param("uid") Integer uid,
			@Param("categoryCode") String categoryCode);

	/**
	 * 全量覆盖前：按 uid 清空。
	 */
	@Delete("delete from user_watch_preference where uid = #{uid}")
	int deleteByUid(@Param("uid") Integer uid);

	/**
	 * 单条写入（save 循环插入）。
	 */
	@Insert("insert into user_watch_preference "
			+ "(uid, category_code, prefer_group_id, sort_weight, setTime, gmtModified) "
			+ "values (#{uid}, #{categoryCode}, #{preferGroupId}, #{sortWeight}, #{setTime}, #{gmtModified})")
	int insertOne(UserWatchPreferenceEntity entity);

	/**
	 * 用户是否已配置过偏好。
	 */
	@Select("select count(1) from user_watch_preference where uid = #{uid}")
	int countByUid(@Param("uid") Integer uid);
}
