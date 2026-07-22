package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.SignInGlobalConfigEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface SignInGlobalConfigDao extends BaseMapper<SignInGlobalConfigEntity> {

	@Select("select * from sign_in_global_config where status = 1 order by id asc limit 1")
	SignInGlobalConfigEntity findEnabledOne();
}
