package com.playlet.internal.dao.medal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.medal.UserMedalEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMedalDao extends BaseMapper<UserMedalEntity> {

	@Select("select * from user_medal where uid = #{uid} and medal_id = #{medalId} limit 1")
	UserMedalEntity findByUidAndMedalId(@Param("uid") Long uid, @Param("medalId") Integer medalId);

	@Select("select * from user_medal where uid = #{uid} order by unlocked desc, unlock_time desc, id desc")
	List<UserMedalEntity> findByUid(@Param("uid") Long uid);

	@Select("select * from user_medal where uid = #{uid} and unlocked = 1 and notify_status = 0 "
			+ "order by unlock_time desc, id desc")
	List<UserMedalEntity> findPendingNotify(@Param("uid") Long uid);

	@Update("<script>"
			+ "update user_medal set notify_status = 1, gmtModified = NOW() "
			+ "where uid = #{uid} and unlocked = 1 and notify_status = 0 "
			+ "and medal_id in "
			+ "<foreach collection='medalIds' item='medalId' open='(' separator=',' close=')'>"
			+ "#{medalId}"
			+ "</foreach>"
			+ "</script>")
	int markNotified(@Param("uid") Long uid, @Param("medalIds") List<Integer> medalIds);
}
