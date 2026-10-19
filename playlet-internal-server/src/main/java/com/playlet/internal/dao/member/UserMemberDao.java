package com.playlet.internal.dao.member;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.member.UserMemberEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * 用户会员权益 Dao。
 */
@Repository
public interface UserMemberDao extends BaseMapper<UserMemberEntity> {

	@Select("select * from user_member where uid = #{uid} limit 1")
	UserMemberEntity findByUid(@Param("uid") Integer uid);

	/** 有效且未过期 */
	@Select("select * from user_member where uid = #{uid} and status = 1 "
			+ "and expire_time > now() limit 1")
	UserMemberEntity findActiveByUid(@Param("uid") Integer uid);

	@Update("update user_member set status = #{status}, gmtModified = now() where uid = #{uid}")
	int updateStatusByUid(@Param("uid") Integer uid, @Param("status") Integer status);
}
