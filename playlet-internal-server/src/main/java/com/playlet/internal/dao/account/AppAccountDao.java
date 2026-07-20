package com.playlet.internal.dao.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.account.AppAccountEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface AppAccountDao extends BaseMapper<AppAccountEntity> {

	@Select("select * from app_account where id = #{uid}")
	AppAccountEntity findByUid(@Param("uid") Integer uid);

	@Select("select * from app_account where user_email = #{userEmail}")
	AppAccountEntity findByEmail(@Param("userEmail") String userEmail);

	@Select("select * from app_account where mobile_number = #{mobileNumber} and mobile_prefix = #{mobilePrefix}")
	AppAccountEntity findByTel(@Param("mobileNumber") String mobileNumber,@Param("mobilePrefix") String mobilePrefix);

	@Select("select * from app_account where user_account = #{userAccount}")
	AppAccountEntity findByAccount(@Param("userAccount") String userAccount);

	@Update("update app_account set coin_balance = ifnull(coin_balance,0) + #{amt}, gmtModified = now() "
			+ "where id = #{uid}")
	int addCoinBalance(@Param("uid") Integer uid, @Param("amt") int amt);
}
