package com.playlet.internal.dao.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.account.AppUserStatusLogEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户状态流水 Dao。
 */
@Repository
public interface AppUserStatusLogDao extends BaseMapper<AppUserStatusLogEntity> {

	@Select("select * from app_user_status_log where uid = #{uid} order by id desc")
	List<AppUserStatusLogEntity> findByUid(@Param("uid") Integer uid);
}
