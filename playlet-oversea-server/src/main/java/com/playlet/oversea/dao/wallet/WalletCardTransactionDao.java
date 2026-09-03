package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletCardTransactionEntity;
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

	@Select("select * from wallet_card_transaction where third_order_num = #{thirdOrderNum} limit 1")
	WalletCardTransactionEntity findByThirdOrderNum(@Param("thirdOrderNum") String thirdOrderNum);

	@Select("select * from wallet_card_transaction where user_bankcard_id = #{userBankcardId} "
			+ "and biz_type = 'RECHARGE' order by setTime desc, id desc limit 1")
	WalletCardTransactionEntity findLatestRechargeByUserBankcardId(@Param("userBankcardId") Long userBankcardId);

	@Select("select * from wallet_card_transaction where withdraw_order_id = #{withdrawOrderId} limit 1")
	WalletCardTransactionEntity findByWithdrawOrderId(@Param("withdrawOrderId") Long withdrawOrderId);

	@Select("select * from wallet_card_transaction where user_bankcard_id = #{userBankcardId} "
			+ "order by setTime desc, id desc")
	List<WalletCardTransactionEntity> findByUserBankcardId(@Param("userBankcardId") Long userBankcardId);

	/** 按钱包用户 + 三方卡 ID 查交易（分页由 PageHelper 截断） */
	@Select("select * from wallet_card_transaction where wallet_user_id = #{walletUserId} "
			+ "and user_bankcard_id = #{userBankcardId} order by setTime desc, id desc")
	List<WalletCardTransactionEntity> findByWalletUserIdAndUserBankcardId(@Param("walletUserId") Long walletUserId,
			@Param("userBankcardId") Long userBankcardId);

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

	/** 管理端卡交易流水（关联用户邮箱） */
	@Select("<script>"
			+ "select t.*, wu.email as userEmail from wallet_card_transaction t "
			+ "left join wallet_user wu on t.wallet_user_id = wu.id where 1=1 "
			+ "<if test='transType != null and transType != \"\"'> "
			+ "and (t.trans_type = #{transType} or t.biz_type = #{transType}) </if>"
			+ "<if test='cardNo != null and cardNo != \"\"'> and t.card_no like concat('%', #{cardNo}, '%') </if>"
			+ "<if test='requestOrderId != null and requestOrderId != \"\"'> and t.request_order_id = #{requestOrderId} </if>"
			+ "<if test='userEmail != null and userEmail != \"\"'> and wu.email like concat('%', #{userEmail}, '%') </if>"
			+ "<if test='walletUid != null'> and t.wallet_uid = #{walletUid} </if>"
			+ "<if test='uid != null and uid != \"\"'> and wu.local_uid = #{uid} and wu.user_type = 1 </if>"
			+ " order by t.order_state desc, t.setTime desc"
			+ "</script>")
	java.util.List<com.playlet.oversea.entity.wallet.WalletCardTransactionEntity> findPcList(
			com.playlet.oversea.query.wallet.WalletCardTransactionAdminQuery query);
}
