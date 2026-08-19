package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
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
}
