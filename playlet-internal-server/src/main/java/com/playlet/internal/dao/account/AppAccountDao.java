package com.playlet.internal.dao.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.AppUserInfoReqEntity;
import com.playlet.internal.entity.account.AppAccountEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppAccountDao extends BaseMapper<AppAccountEntity> {

	@Select("select * from app_account where uid = #{uid}")
	AppAccountEntity findByUid(@Param("uid") Integer uid);

	@Select("select * from app_account where user_email = #{userEmail}")
	AppAccountEntity findByEmail(@Param("userEmail") String userEmail);

	@Select("select * from app_account where mobile_number = #{mobileNumber} and mobile_prefix = #{mobilePrefix}")
	AppAccountEntity findByTel(@Param("mobileNumber") String mobileNumber,@Param("mobilePrefix") String mobilePrefix);

	@Select("select * from app_account where user_account = #{userAccount}")
	AppAccountEntity findByAccount(@Param("userAccount") String userAccount);

	@Select("select * from app_account where mobile_number = #{mobileNumber}")
	AppAccountEntity findByMobile(@Param("mobileNumber") String mobileNumber);

	@Select("<script>"
			+ "select * from app_account as a LEFT JOIN app_card_account as b on a.uid = b.uid where 1=1 "
			+ "<if test = 'uid != null'> and a.uid = #{uid}</if>"
			+ "<if test = 'userEmail != null'> and a.user_email = #{userEmail}</if>"
			+ "<if test = 'mobileNumber != null'> and a.mobile_number = #{mobileNumber}</if>"
			+ "<if test = 'userState != null'> and a.user_state = #{userState}</if>"
			+ "<if test = 'invitationCode != null'> and a.invitation_code invitationCode = #{invitationCode}</if>"
			+ " order by a.setTime desc"
			+ "</script>")
	List<AppUserInfoReqEntity> findList(AppAccountEntity entity);


	@Select("select * from app_account where invitation_code = #{enterInvitationCode}")
	AppAccountEntity findByIncitationCode(@Param("enterInvitationCode") String enterInvitationCode);

	@Update("update app_account set coin_balance = ifnull(coin_balance,0) + #{amt}, gmtModified = now() "
			+ "where uid = #{uid}")
	int addCoinBalance(@Param("uid") Integer uid, @Param("amt") int amt);
}
