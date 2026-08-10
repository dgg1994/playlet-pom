package com.playlet.oversea.dao.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.account.AppAccountEntity;
import com.playlet.oversea.entity.account.AppPushDeviceEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppPushDeviceDao extends BaseMapper<AppPushDeviceEntity> {

	@Select("select * from app_push_device where registration_id = #{registrationId} limit 1")
	AppPushDeviceEntity findByRegistrationId(@Param("registrationId") String registrationId);

	@Select("select * from app_push_device where uid = #{uid} order by gmtModified desc limit 1")
	AppPushDeviceEntity findLatestByUid(@Param("uid") Integer uid);

	@Update("update app_push_device set uid = null, gmtModified = now() where uid = #{uid}")
	int clearUid(@Param("uid") Integer uid);

	@Update("update app_push_device set push_enabled = #{enabled}, gmtModified = now() "
			+ "where registration_id = #{registrationId}")
	int updatePushEnabled(@Param("registrationId") String registrationId, @Param("enabled") Integer enabled);

	/** 已开启推送的 registrationId 列表 */
	@Select("select registration_id from app_push_device "
			+ "where ifnull(push_enabled,1) = 1 "
			+ "and registration_id is not null and registration_id != ''")
	List<String> findEnabledRegistrationIds();

	/**
	 * 已开启推送的设备 + 关联账号语言（用于按语言分组广播）。
	 * registrationId/pushLangue 映射到 AppAccountEntity 同名属性。
	 */
	@Select("select d.registration_id as registrationId, "
			+ "ifnull(nullif(a.push_langue,''), 'zh-cn') as pushLangue "
			+ "from app_push_device d "
			+ "left join app_account a on a.id = d.uid "
			+ "where ifnull(d.push_enabled,1) = 1 "
			+ "and d.registration_id is not null and d.registration_id != ''")
	List<AppAccountEntity> findEnabledPushTargets();
}
