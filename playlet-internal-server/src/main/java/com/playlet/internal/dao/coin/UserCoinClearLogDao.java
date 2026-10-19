package com.playlet.internal.dao.coin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.coin.UserCoinClearLogEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 清币流水 Dao。
 */
@Repository
public interface UserCoinClearLogDao extends BaseMapper<UserCoinClearLogEntity> {

	@Select("select * from user_coin_clear_log where uid = #{uid} order by id desc")
	List<UserCoinClearLogEntity> findByUid(@Param("uid") Integer uid);
}
