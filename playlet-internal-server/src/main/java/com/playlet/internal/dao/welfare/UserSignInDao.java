package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.UserSignInEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSignInDao extends BaseMapper<UserSignInEntity> {

	@Select("select * from user_sign_in where uid = #{uid} limit 1")
	UserSignInEntity findByUid(@Param("uid") String uid);

	@Update("update user_sign_in set streak_days = #{streakDays}, last_sign_date = #{lastSignDate}, "
			+ "total_sign_days = #{totalSignDays}, gmtModified = now() where uid = #{uid}")
	int updateStreak(@Param("uid") String uid, @Param("streakDays") Integer streakDays,
			@Param("lastSignDate") String lastSignDate, @Param("totalSignDays") Integer totalSignDays);

	@Update("update user_sign_in set makeup_card_balance = ifnull(makeup_card_balance,0) + #{delta}, "
			+ "gmtModified = now() where uid = #{uid} and ifnull(makeup_card_balance,0) + #{delta} >= 0")
	int addMakeupCardBalance(@Param("uid") String uid, @Param("delta") int delta);

	@Update("update user_sign_in set makeup_buy_month = #{buyMonth}, makeup_buy_count = #{buyCount}, "
			+ "gmtModified = now() where uid = #{uid}")
	int updateMakeupBuyStat(@Param("uid") String uid, @Param("buyMonth") String buyMonth,
			@Param("buyCount") Integer buyCount);
}
