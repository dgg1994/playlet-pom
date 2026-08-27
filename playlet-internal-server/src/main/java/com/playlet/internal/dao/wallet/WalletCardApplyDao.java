package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 钱包 U 卡开卡申请。
 */
@Repository
public interface WalletCardApplyDao extends BaseMapper<WalletCardApplyEntity> {

	@Select("select * from wallet_card_apply where request_order_id = #{requestOrderId} limit 1")
	WalletCardApplyEntity findByRequestOrderId(@Param("requestOrderId") String requestOrderId);

	@Select("select * from wallet_card_apply where wallet_user_id = #{walletUserId} "
			+ "order by setTime desc, id desc")
	List<WalletCardApplyEntity> findByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_card_apply where wallet_user_id = #{walletUserId} "
			+ "order by setTime desc, id desc limit 1")
	WalletCardApplyEntity findLatestByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Update("update wallet_card_apply set apply_state = #{applyState}, apply_state_name = #{applyStateName}, "
			+ "reject_info = #{rejectInfo}, gmtModified = now() where id = #{id}")
	int updateApplyState(@Param("id") Long id, @Param("applyState") Integer applyState,
			@Param("applyStateName") String applyStateName, @Param("rejectInfo") String rejectInfo);
}
