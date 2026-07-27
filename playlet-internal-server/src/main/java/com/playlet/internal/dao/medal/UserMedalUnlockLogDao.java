package com.playlet.internal.dao.medal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.medal.UserMedalUnlockLogEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMedalUnlockLogDao extends BaseMapper<UserMedalUnlockLogEntity> {

	@Select("<script>"
			+ "select * from user_medal_unlock_log where 1=1 "
			+ "<if test='uid != null'> and uid = #{uid} </if>"
			+ "<if test='medalId != null'> and medal_id = #{medalId} </if>"
			+ "<if test='unlockFlag != null'> and unlock_flag = #{unlockFlag} </if>"
			+ "order by setTime desc, id desc"
			+ "</script>")
	List<UserMedalUnlockLogEntity> findAdminList(UserMedalUnlockLogEntity entity);
}
