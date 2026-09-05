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
			+ "where id = #{uid} and ifnull(coin_balance,0) - ifnull(frozen_coin_balance,0) >= #{amt}")
	int deductCoinBalance(@Param("uid") Integer uid, @Param("amt") int amt);

	@Update("update app_account set frozen_coin_balance = ifnull(frozen_coin_balance,0) + #{amt}, gmtModified = now() "
			+ "where id = #{uid} and ifnull(coin_balance,0) - ifnull(frozen_coin_balance,0) >= #{amt}")
	int freezeCoinBalance(@Param("uid") Integer uid, @Param("amt") int amt);

	@Update("update app_account set frozen_coin_balance = ifnull(frozen_coin_balance,0) - #{amt}, gmtModified = now() "
			+ "where id = #{uid} and ifnull(frozen_coin_balance,0) >= #{amt}")
	int unfreezeCoinBalance(@Param("uid") Integer uid, @Param("amt") int amt);

	/** 提现成功：冻结转扣减，coin_balance 与 frozen 同时减少 */
	@Update("update app_account set coin_balance = ifnull(coin_balance,0) - #{amt}, "
			+ "frozen_coin_balance = ifnull(frozen_coin_balance,0) - #{amt}, gmtModified = now() "
			+ "where id = #{uid} and ifnull(frozen_coin_balance,0) >= #{amt} "
			+ "and ifnull(coin_balance,0) >= #{amt}")
	int settleFrozenCoin(@Param("uid") Integer uid, @Param("amt") int amt);

	@Update("update app_account set registration_id = #{registrationId}, device_name = #{deviceName}, "
			+ "gmtModified = now() where id = #{uid}")
	int updatePushBind(@Param("uid") Integer uid,
			@Param("registrationId") String registrationId,
			@Param("deviceName") String deviceName);

	@Update("update app_account set registration_id = #{registrationId}, gmtModified = now() where id = #{uid}")
	int updateRegistrationId(@Param("uid") Integer uid, @Param("registrationId") String registrationId);

	@Update("update app_account set nickname = #{nickname}, avatar = #{avatar}, gender = #{gender}, "
			+ "birth_month = #{birthMonth}, gmtModified = now() where id = #{id}")
	void updateProfileById(AppAccountEntity entity);

	@Update("update app_account set sys_msg_read_publish_id = #{publishId}, gmtModified = now() "
			+ "where id = #{uid} and ifnull(sys_msg_read_publish_id,0) < #{publishId}")
	int updateSysMsgReadCursor(@Param("uid") Integer uid, @Param("publishId") Long publishId);

	@Update("update app_account set push_langue = #{langue}, gmtModified = now() where id = #{uid}")
	int updatePushLangue(@Param("uid") Integer uid, @Param("langue") String langue);

	@Select("<script>"
			+ "select a.id, cast(a.id as char) as uid, a.user_account as user_account, a.user_email as user_email, "
			+ "coalesce(nullif(a.mobile_number,''), nullif(wu.mobile_number,''), nullif(h.user_tel,'')) as mobile_number, "
			+ "coalesce(nullif(a.mobile_prefix,''), nullif(wu.mobile_prefix,''), nullif(h.user_tel_dial_code,'')) as mobile_prefix, "
			+ "a.user_state as user_state, a.invitation_code as invitation_code, a.setTime, a.gmtModified, "
			+ "wa.wallet_uid as wallet_uid, "
			+ "wa.available_balance as wallet_balance, wa.freeze_balance as freeze_balance, "
			+ "wa.open_freeze_balance as open_freeze_balance, wa.kyc_state as kyc_state, "
			+ "wa.kyc_state_name as kyc_state_name, wa.kyc_audit_result as kyc_audit_result, "
			+ "wa.activation_state as activation_state, wa.activation_time as activation_time "
			+ "from app_account a "
			+ "inner join wallet_user wu on wu.user_type = 1 and wu.local_uid = a.id "
			+ "left join wallet_account wa on wa.wallet_user_id = wu.id "
			// 持卡人最新一条：开卡时填写的区号/手机号（账号注册常不填手机）
			+ "left join wallet_user_holder h on h.id = ("
			+ "  select h2.id from wallet_user_holder h2 where h2.wallet_user_id = wu.id "
			+ "  order by h2.id desc limit 1"
			+ ") "
			+ "where 1=1 "
			+ "<if test='userEmail != null and userEmail != \"\"'> and a.user_email like concat('%', #{userEmail}, '%') </if>"
			+ "<if test='mobileNumber != null and mobileNumber != \"\"'> "
			+ "and (a.mobile_number = #{mobileNumber} or wu.mobile_number = #{mobileNumber} "
			+ "or h.user_tel = #{mobileNumber}) </if>"
			+ "<if test='userState != null'> and a.user_state = #{userState} </if>"
			+ "<if test='kycState != null'> and wa.kyc_state = #{kycState} </if>"
			+ " order by a.setTime desc"
			+ "</script>")
	java.util.List<com.playlet.internal.api.response.AppUserInfoReqEntity> findWalletAppUserList(
			com.playlet.internal.entity.account.AppAccountEntity entity);
}
