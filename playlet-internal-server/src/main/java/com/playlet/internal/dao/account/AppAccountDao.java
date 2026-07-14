package com.playlet.internal.dao.account;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.account.AppAccountEntity;

@Repository
public interface AppAccountDao extends BaseMapper<AppAccountEntity> {

	@Select("select * from app_account where uid = #{uid}")
	AppAccountEntity findByUid(@Param("uid") String uid);

	@Select("select * from app_account where user_email = #{userEmail}")
	AppAccountEntity findByEmail(@Param("userEmail") String userEmail);

	@Select("select * from app_account where user_account = #{userAccount}")
	AppAccountEntity findByAccount(@Param("userAccount") String userAccount);

	@Select("select * from app_account where mobile_number = #{mobileNumber}")
	AppAccountEntity findByMobile(@Param("mobileNumber") String mobileNumber);

	@Select("<script>"
			+ "select * from app_account where 1=1 "
			+ "<if test = 'userState != null'> and user_state = #{userState}</if>"
			+ "<if test = 'userAccount != null and userAccount != \"\"'> and user_account like concat('%',#{userAccount},'%')</if>"
			+ "<if test = 'userEmail != null and userEmail != \"\"'> and user_email like concat('%',#{userEmail},'%')</if>"
			+ "<if test = 'mobileNumber != null and mobileNumber != \"\"'> and mobile_number = #{mobileNumber}</if>"
			+ "<if test = 'startTime != null and startTime != \"\"'> and setTime &gt;= #{startTime}</if>"
			+ "<if test = 'endTime != null and endTime != \"\"'> and setTime &lt;= #{endTime}</if>"
			+ "order by setTime desc "
			+ "</script>")
	List<AppAccountEntity> findList(AppAccountEntity entity);
}
