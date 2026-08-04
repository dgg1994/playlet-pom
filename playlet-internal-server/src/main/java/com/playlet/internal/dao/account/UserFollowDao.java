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
	UserFollowEntity findOne(@Param("uid") Integer uid, @Param("followUid") Integer followUid);

	/** 我的关注列表 */
	@Select("select * from user_follow where uid = #{uid} order by setTime desc")
	List<UserFollowEntity> findFollowing(@Param("uid") Integer uid);

	/** 我的粉丝列表 */
	@Select("select * from user_follow where follow_uid = #{followUid} order by setTime desc")
	List<UserFollowEntity> findFans(@Param("followUid") Integer followUid);

	@Select("select count(*) from user_follow where uid = #{uid}")
	long countFollowing(@Param("uid") Integer uid);

	@Select("select count(*) from user_follow where follow_uid = #{followUid}")
	long countFans(@Param("followUid") Integer followUid);

	@Delete("delete from user_follow where uid = #{uid} and follow_uid = #{followUid}")
	int deleteOne(@Param("uid") Integer uid, @Param("followUid") Integer followUid);

	/** 当前用户在候选列表中已关注的 follow_uid */
	@Select("<script>"
			+ "select follow_uid from user_follow where uid = #{uid} and follow_uid in "
			+ "<foreach collection='followUids' item='fid' open='(' separator=',' close=')'>#{fid}</foreach>"
			+ "</script>")
	List<Integer> findFollowedAmong(@Param("uid") Integer uid, @Param("followUids") List<Integer> followUids);
}
