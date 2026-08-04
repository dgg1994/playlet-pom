package com.playlet.internal.dao.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.account.AppAccountEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppAccountDao extends BaseMapper<AppAccountEntity> {

	@Select("select * from app_account where id = #{uid}")
	AppAccountEntity findByUid(@Param("uid") Integer uid);

	@Select("<script>"
			+ "select * from app_account where id in "
			+ "<foreach collection='uids' item='uid' open='(' separator=',' close=')'>#{uid}</foreach>"
			+ "</script>")
	List<AppAccountEntity> findByUids(@Param("uids") List<Integer> uids);

	@Select("select * from app_account where user_email = #{userEmail}")
	AppAccountEntity findByEmail(@Param("userEmail") String userEmail);

	@Select("select * from app_account where mobile_number = #{mobileNumber} and mobile_prefix = #{mobilePrefix}")
	AppAccountEntity findByTel(@Param("mobileNumber") String mobileNumber,@Param("mobilePrefix") String mobilePrefix);

	@Select("select * from app_account where user_account = #{userAccount}")
	AppAccountEntity findByAccount(@Param("userAccount") String userAccount);

	@Update("update app_account set coin_balance = ifnull(coin_balance,0) + #{amt}, gmtModified = now() "
			+ "where id = #{uid}")
	int addCoinBalance(@Param("uid") Integer uid, @Param("amt") int amt);

	@Update("update app_account set coin_balance = ifnull(coin_balance,0) - #{amt}, gmtModified = now() "
			+ "where id = #{uid} and ifnull(coin_balance,0) >= #{amt}")
	int deductCoinBalance(@Param("uid") Integer uid, @Param("amt") int amt);

	@Update("update app_account set registration_id = #{registrationId}, device_name = #{deviceName}, "
			+ "gmtModified = now() where id = #{uid}")
	int updatePushBind(@Param("uid") Integer uid,
			@Param("registrationId") String registrationId,
			@Param("deviceName") String deviceName);

	@Update("update app_account set registration_id = #{registrationId}, gmtModified = now() where id = #{uid}")
	int updateRegistrationId(@Param("uid") Integer uid, @Param("registrationId") String registrationId);

	@Update("update app_account set nickname = #{nickname},avatar = #{avatar}, gmtModified = now() where id = #{id}")
    void updateNameById(AppAccountEntity entity);

	@Update("update app_account set sys_msg_read_publish_id = #{publishId}, gmtModified = now() "
			+ "where id = #{uid} and ifnull(sys_msg_read_publish_id,0) < #{publishId}")
	int updateSysMsgReadCursor(@Param("uid") Integer uid, @Param("publishId") Long publishId);
}
