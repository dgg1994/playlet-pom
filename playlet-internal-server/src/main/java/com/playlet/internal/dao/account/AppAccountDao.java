package com.playlet.internal.dao.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.AppUserListItemEntity;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.query.account.AppUserManageQuery;
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

	/** 刷新最近登录时间（清币判定用） */
	@Update("update app_account set last_login_time = now(), gmtModified = now() where id = #{uid}")
	int touchLastLoginTime(@Param("uid") Integer uid);

	/** 久未登录且有余额：清币候选 */
	@Select("select * from app_account where ifnull(coin_balance,0) > 0 "
			+ "and user_state = 1 "
			+ "and (last_login_time is null or last_login_time < date_sub(now(), interval #{idleDays} day)) "
			+ "order by id asc limit #{limit}")
	List<AppAccountEntity> findIdleWithCoin(@Param("idleDays") int idleDays, @Param("limit") int limit);

	/** 清零可用金币（保留冻结不动；清币仅清可用余额） */
	@Update("update app_account set coin_balance = ifnull(frozen_coin_balance,0), gmtModified = now() "
			+ "where id = #{uid} and ifnull(coin_balance,0) > ifnull(frozen_coin_balance,0)")
	int clearAvailableCoin(@Param("uid") Integer uid);

	@Update("update app_account set user_state = #{state}, gmtModified = now() where id = #{uid}")
	int updateUserState(@Param("uid") Integer uid, @Param("state") Integer state);

	@Update("update app_account set mute_until = #{muteUntil}, gmtModified = now() where id = #{uid}")
	int updateMuteUntil(@Param("uid") Integer uid, @Param("muteUntil") java.util.Date muteUntil);

	@Update("update app_account set user_password = #{pwd}, gmtModified = now() where id = #{uid}")
	int updatePassword(@Param("uid") Integer uid, @Param("pwd") String pwd);

	@Select("select ifnull(count(1), 0) from app_account")
	long countAllUsers();

	@Select("select ifnull(count(1), 0) from app_account "
			+ "where setTime >= #{startTime} and setTime < #{endTimeExclusive}")
	long countNewUsers(@Param("startTime") String startTime, @Param("endTimeExclusive") String endTimeExclusive);

	@Select("select ifnull(count(1), 0) from user_member where status = 1 and expire_time > now()")
	long countActiveMembers();

	@Select("select ifnull(count(distinct uid), 0) from member_order "
			+ "where pay_status = 1 and ifnull(pay_amount, 0) > 0")
	long countPaidUsers();

	/** 管理端用户分页列表（含会员与累计充值） */
	@Select("<script>"
			+ "select a.id as uid, a.nickname as nickname, a.avatar as avatarUrl, "
			+ "a.mobile_number as mobileMasked, a.real_name as realName, "
			+ "a.register_source as registerSource, a.invited_by_uid as invitedByUid, "
			+ "a.coin_balance as coinBalance, a.user_state as userState, "
			+ "a.setTime as setTime, a.last_login_time as lastLoginTime, a.mute_until as muteUntil, "
			+ "case "
			+ "  when m.id is null then 0 "
			+ "  when m.status = 1 and m.expire_time &gt; now() then 1 "
			+ "  else 2 end as memberStatus, "
			+ "(select ifnull(sum(o.pay_amount),0) from member_order o "
			+ "  where o.uid = a.id and o.pay_status = 1) as totalRechargeYuan "
			+ "from app_account a "
			+ "left join user_member m on m.uid = a.id "
			+ "where 1=1 "
			+ "<if test='keyword != null and keyword != \"\"'>"
			+ "  and ("
			+ "    cast(a.id as char) = #{keyword} "
			+ "    or a.mobile_number like concat('%', #{keyword}, '%') "
			+ "    or a.nickname like concat('%', #{keyword}, '%') "
			+ "    or a.user_account like concat('%', #{keyword}, '%')"
			+ "  )"
			+ "</if>"
			+ "<if test='userState != null'> and a.user_state = #{userState} </if>"
			+ "<if test='registerSource != null'> and a.register_source = #{registerSource} </if>"
			+ "<if test='memberStatus != null and memberStatus == 0'> and m.id is null </if>"
			+ "<if test='memberStatus != null and memberStatus == 1'> "
			+ "  and m.status = 1 and m.expire_time &gt; now() "
			+ "</if>"
			+ "<if test='memberStatus != null and memberStatus == 2'> "
			+ "  and m.id is not null and (m.status = 0 or m.expire_time &lt;= now()) "
			+ "</if>"
			+ "order by a.id desc"
			+ "</script>")
	List<AppUserListItemEntity> findAdminUserList(AppUserManageQuery query);
}
