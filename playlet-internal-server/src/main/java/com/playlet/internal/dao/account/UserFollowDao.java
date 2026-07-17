package com.playlet.internal.dao.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.account.UserFollowEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFollowDao extends BaseMapper<UserFollowEntity> {

	@Select("select * from user_follow where uid = #{uid} and follow_uid = #{followUid} limit 1")
	UserFollowEntity findOne(@Param("uid") String uid, @Param("followUid") String followUid);

	/** 我的关注列表 */
	@Select("select * from user_follow where uid = #{uid} order by setTime desc")
	List<UserFollowEntity> findFollowing(@Param("uid") String uid);

	/** 我的粉丝列表 */
	@Select("select * from user_follow where follow_uid = #{followUid} order by setTime desc")
	List<UserFollowEntity> findFans(@Param("followUid") String followUid);

	@Select("select count(*) from user_follow where uid = #{uid}")
	long countFollowing(@Param("uid") String uid);

	@Select("select count(*) from user_follow where follow_uid = #{followUid}")
	long countFans(@Param("followUid") String followUid);

	@Delete("delete from user_follow where uid = #{uid} and follow_uid = #{followUid}")
	int deleteOne(@Param("uid") String uid, @Param("followUid") String followUid);
}
