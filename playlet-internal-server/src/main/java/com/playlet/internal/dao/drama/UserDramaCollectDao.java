package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.UserDramaCollectEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDramaCollectDao extends BaseMapper<UserDramaCollectEntity> {

	@Select("select * from user_drama_collect where uid = #{uid} and drama_id = #{dramaId} limit 1")
	UserDramaCollectEntity findByUidAndDrama(@Param("uid") Integer uid, @Param("dramaId") Integer dramaId);

	@Select("select * from user_drama_collect where uid = #{uid} order by setTime desc")
	List<UserDramaCollectEntity> findByUid(@Param("uid") Integer uid);

	@Delete("delete from user_drama_collect where uid = #{uid} and drama_id = #{dramaId}")
	int deleteByUidAndDrama(@Param("uid") Integer uid, @Param("dramaId") Integer dramaId);

	@Select("select * from user_drama_collect where drama_id = #{dramaId} and uid = #{uid}")
	UserDramaCollectEntity findByVoideId(@Param("dramaId") Integer dramaId, @Param("uid") Integer uid);
}
