package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletCardTransactionEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 钱包 U 卡交易流水。
 */
@Repository
public interface WalletCardTransactionDao extends BaseMapper<WalletCardTransactionEntity> {

	@Select("select * from wallet_card_transaction where request_order_id = #{requestOrderId} limit 1")
	WalletCardTransactionEntity findByRequestOrderId(@Param("requestOrderId") String requestOrderId);

	@Select("select * from wallet_card_transaction where withdraw_order_id = #{withdrawOrderId} limit 1")
	WalletCardTransactionEntity findByWithdrawOrderId(@Param("withdrawOrderId") Long withdrawOrderId);

	@Select("select * from wallet_card_transaction where user_bankcard_id = #{userBankcardId} "
			+ "order by setTime desc, id desc")
	List<WalletCardTransactionEntity> findByUserBankcardId(@Param("userBankcardId") Long userBankcardId);

	@Select("select * from wallet_card_transaction where wallet_user_id = #{walletUserId} "
			+ "and biz_type = #{bizType} order by setTime desc, id desc")
	List<WalletCardTransactionEntity> findByWalletUserIdAndBizType(@Param("walletUserId") Long walletUserId,
			@Param("bizType") String bizType);

	/** 用户全部交易（分页由 PageHelper 截断） */
	@Select("select * from wallet_card_transaction where wallet_user_id = #{walletUserId} "
			+ "order by setTime desc, id desc")
	List<WalletCardTransactionEntity> findByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Update("update wallet_card_transaction set order_state = #{orderState}, "
			+ "order_state_name = #{orderStateName}, third_order_num = #{thirdOrderNum}, "
			+ "gmtModified = now() where id = #{id}")
	int updateOrderState(@Param("id") Long id, @Param("orderState") Integer orderState,
			@Param("orderStateName") String orderStateName, @Param("thirdOrderNum") String thirdOrderNum);
}
