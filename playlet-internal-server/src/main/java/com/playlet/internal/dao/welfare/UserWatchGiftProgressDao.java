package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.UserWatchGiftProgressEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWatchGiftProgressDao extends BaseMapper<UserWatchGiftProgressEntity> {

	@Select("select * from user_watch_gift_progress where uid = #{uid} and biz_date = #{bizDate} limit 1")
	UserWatchGiftProgressEntity findOne(@Param("uid") String uid, @Param("bizDate") String bizDate);

	@Update("update user_watch_gift_progress set watch_seconds = #{watchSeconds}, last_report_time = #{lastReportTime}, "
			+ "gmtModified = now() where id = #{id}")
	int updateWatchSeconds(@Param("id") Long id, @Param("watchSeconds") Integer watchSeconds,
			@Param("lastReportTime") java.util.Date lastReportTime);

	@Update("update user_watch_gift_progress set claimed_gears = #{claimedGears}, gmtModified = now() where id = #{id}")
	int updateClaimedGears(@Param("id") Long id, @Param("claimedGears") String claimedGears);
}
