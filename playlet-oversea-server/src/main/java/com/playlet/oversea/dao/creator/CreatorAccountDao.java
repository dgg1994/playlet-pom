package com.playlet.oversea.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.api.response.CreatorAccountManageItemEntity;
import com.playlet.oversea.entity.creator.CreatorAccountEntity;
import com.playlet.oversea.query.creator.CreatorAccountManageQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作家登录账号。
 */
@Repository
public interface CreatorAccountDao extends BaseMapper<CreatorAccountEntity> {

	@Select("select * from creator_account where user_account = #{userAccount} limit 1")
	CreatorAccountEntity findByAccount(@Param("userAccount") String userAccount);

	@Select("<script>"
			+ "select * from creator_account where id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<CreatorAccountEntity> findByIds(@Param("ids") List<Integer> ids);

	@Update("update creator_account set last_login_time = now(), gmtModified = now() where id = #{id}")
	int updateLastLoginTime(@Param("id") Integer id);

	/** 推进站务广播已读游标（只增不减） */
	@Update("update creator_account set sys_msg_read_publish_id = #{publishId}, gmtModified = now() "
			+ "where id = #{creatorId} and ifnull(sys_msg_read_publish_id,0) < #{publishId}")
	int updateSysMsgReadCursor(@Param("creatorId") Integer creatorId, @Param("publishId") Long publishId);

	/** 提现提交：只冻冻结余额，不扣 coin_balance */
	@Update("update creator_account set frozen_coin_balance = ifnull(frozen_coin_balance,0) + #{amt}, gmtModified = now() "
			+ "where id = #{uid} and ifnull(coin_balance,0) - ifnull(frozen_coin_balance,0) >= #{amt}")
	int freezeCoinBalance(@Param("uid") Integer uid, @Param("amt") int amt);

	/** 提现失败：解冻 */
	@Update("update creator_account set frozen_coin_balance = ifnull(frozen_coin_balance,0) - #{amt}, gmtModified = now() "
			+ "where id = #{uid} and ifnull(frozen_coin_balance,0) >= #{amt}")
	int unfreezeCoinBalance(@Param("uid") Integer uid, @Param("amt") int amt);

	/** 提现成功：冻结转扣减 */
	@Update("update creator_account set coin_balance = ifnull(coin_balance,0) - #{amt}, "
			+ "frozen_coin_balance = ifnull(frozen_coin_balance,0) - #{amt}, gmtModified = now() "
			+ "where id = #{uid} and ifnull(frozen_coin_balance,0) >= #{amt} "
			+ "and ifnull(coin_balance,0) >= #{amt}")
	int settleFrozenCoin(@Param("uid") Integer uid, @Param("amt") int amt);

	/** 管理端：软删除/改状态 */
	@Update("update creator_account set user_state = #{state}, gmtModified = now() where id = #{id}")
	int updateUserState(@Param("id") Integer id, @Param("state") Integer state);

	/** 管理端创作者用户分页列表 */
	@Select("<script>"
			+ "select ca.id, ca.user_account as userAccount, ca.nickname, "
			+ "concat(ifnull(ca.mobile_prefix,''), ifnull(ca.mobile_number,'')) as mobile, "
			+ "ca.avatar as avatarUrl, ca.user_state as userState, "
			+ "ca.coin_balance as coinBalance, ca.frozen_coin_balance as frozenCoinBalance, "
			+ "ca.total_income_coin as totalIncomeCoin, ca.last_login_time as lastLoginTime, ca.setTime, "
			+ "cp.identity_type as identityType, cp.real_name as realName, cp.audit_status as auditStatus, "
			+ "cp.onepay_bind_status as onepayBindStatus "
			+ "from creator_account ca "
			+ "left join creator_profile cp on cp.creator_id = ca.id "
			+ "where 1=1 "
			+ "<if test='query.userState != null'> and ca.user_state = #{query.userState} </if>"
			+ "<if test='query.auditStatus != null'> and cp.audit_status = #{query.auditStatus} </if>"
			+ "<if test='query.keyword != null and query.keyword != \"\"'> "
			+ "  and (ca.user_account like concat('%',#{query.keyword},'%') "
			+ "    or ca.nickname like concat('%',#{query.keyword},'%') "
			+ "    or ca.mobile_number like concat('%',#{query.keyword},'%')) "
			+ "</if>"
			+ "<if test='query.startTime != null and query.startTime != \"\"'> and ca.setTime &gt;= #{query.startTime} </if>"
			+ "<if test='query.endTime != null and query.endTime != \"\"'> and ca.setTime &lt;= #{query.endTime} </if>"
			+ "order by ca.id desc"
			+ "</script>")
	List<CreatorAccountManageItemEntity> findAdminList(@Param("query") CreatorAccountManageQuery query);
}
