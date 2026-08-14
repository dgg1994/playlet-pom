package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * 作家登录账号。
 */
@Repository
public interface CreatorAccountDao extends BaseMapper<CreatorAccountEntity> {

	@Select("select * from creator_account where user_account = #{userAccount} limit 1")
	CreatorAccountEntity findByAccount(@Param("userAccount") String userAccount);

	@Update("update creator_account set last_login_time = now(), gmtModified = now() where id = #{id}")
	int updateLastLoginTime(@Param("id") Integer id);
}
