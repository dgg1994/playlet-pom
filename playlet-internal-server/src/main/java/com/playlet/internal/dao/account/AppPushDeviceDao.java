package com.playlet.internal.dao.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.account.AppPushDeviceEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface AppPushDeviceDao extends BaseMapper<AppPushDeviceEntity> {

	@Select("select * from app_push_device where registration_id = #{registrationId} limit 1")
	AppPushDeviceEntity findByRegistrationId(@Param("registrationId") String registrationId);

	@Select("select * from app_push_device where uid = #{uid} order by gmtModified desc limit 1")
	AppPushDeviceEntity findLatestByUid(@Param("uid") Integer uid);

	@Update("update app_push_device set uid = null, gmtModified = now() where uid = #{uid}")
	int clearUid(@Param("uid") Integer uid);
}
