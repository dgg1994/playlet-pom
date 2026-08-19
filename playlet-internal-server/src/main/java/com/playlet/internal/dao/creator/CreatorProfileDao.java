package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorProfileEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * 作家入驻资料。
 */
@Repository
public interface CreatorProfileDao extends BaseMapper<CreatorProfileEntity> {

	@Select("select * from creator_profile where creator_id = #{creatorId} limit 1")
	CreatorProfileEntity findByCreatorId(@Param("creatorId") Integer creatorId);

	/** 绑定 OnePay */
	@Update("update creator_profile set onepay_account = #{onepayAccount}, onepay_open_id = #{onepayOpenId}, "
			+ "onepay_bind_status = #{bindStatus}, onepay_bind_time = #{bindTime}, gmtModified = now() "
			+ "where creator_id = #{creatorId}")
	int updateOnePayBind(@Param("creatorId") Integer creatorId, @Param("onepayAccount") String onepayAccount,
			@Param("onepayOpenId") String onepayOpenId, @Param("bindStatus") Integer bindStatus,
			@Param("bindTime") Date bindTime);

	/** 解绑：清空 OnePay 字段 */
	@Update("update creator_profile set onepay_account = null, onepay_open_id = null, "
			+ "onepay_bind_status = #{bindStatus}, onepay_bind_time = null, gmtModified = now() "
			+ "where creator_id = #{creatorId}")
	int clearOnePayBind(@Param("creatorId") Integer creatorId, @Param("bindStatus") Integer bindStatus);
}
