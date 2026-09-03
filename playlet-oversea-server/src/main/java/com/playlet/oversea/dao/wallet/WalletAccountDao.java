package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletAccountEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * 钱包账户（KYC / 开卡 / 余额缓存）。
 */
@Repository
public interface WalletAccountDao extends BaseMapper<WalletAccountEntity> {

	@Select("select * from wallet_account where wallet_user_id = #{walletUserId} limit 1")
	WalletAccountEntity findByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_account where wallet_uid = #{walletUid} limit 1")
	WalletAccountEntity findByWalletUid(@Param("walletUid") Long walletUid);

	/** 更新 KYC 状态 */
	@Update("update wallet_account set kyc_state = #{kycState}, kyc_state_name = #{kycStateName}, "
			+ "kyc_api_status = #{kycApiStatus}, kyc_audit_result = #{kycAuditResult}, gmtModified = now() "
			+ "where wallet_user_id = #{walletUserId}")
	int updateKycStatus(@Param("walletUserId") Long walletUserId, @Param("kycState") Integer kycState,
			@Param("kycStateName") String kycStateName, @Param("kycApiStatus") String kycApiStatus,
			@Param("kycAuditResult") String kycAuditResult);

	/** 标记已开卡激活 */
	@Update("update wallet_account set activation_state = 1, activation_time = now(), gmtModified = now() "
			+ "where wallet_user_id = #{walletUserId} and activation_state = 0")
	int markActivated(@Param("walletUserId") Long walletUserId);

	/** 首次绑定支付密码（仅未设置时） */
	@Update("update wallet_account set pay_password = #{payPassword}, pay_password_set_time = now(), "
			+ "gmtModified = now() where id = #{id} and pay_password is null")
	int bindPayPassword(@Param("id") Long id, @Param("payPassword") String payPassword);

	/** 同步账户余额缓存（三方查询/回调后写入） */
	@Update("update wallet_account set available_balance = #{availableBalance}, freeze_balance = #{freezeBalance}, "
			+ "open_freeze_balance = #{openFreezeBalance}, currency = #{currency}, "
			+ "balance_sync_time = now(), gmtModified = now() where id = #{id}")
	int syncBalance(@Param("id") Long id, @Param("availableBalance") BigDecimal availableBalance,
			@Param("freezeBalance") BigDecimal freezeBalance,
			@Param("openFreezeBalance") BigDecimal openFreezeBalance,
			@Param("currency") String currency);

	/** 回写 TRON USDT 充值地址 */
	@Update("update wallet_account set tron_usdt_address = #{address}, gmtModified = now() where id = #{id}")
	int updateTronUsdtAddress(@Param("id") Long id, @Param("address") String address);

	/** 充值到账后更新可用余额 */
	@Update("update wallet_account set available_balance = #{availableBalance}, balance_sync_time = now(), "
			+ "gmtModified = now() where id = #{id}")
	int updateAvailableBalance(@Param("id") Long id, @Param("availableBalance") BigDecimal availableBalance);

	/** 提现入账：原子增加可用余额 */
	@Update("update wallet_account set available_balance = ifnull(available_balance, 0) + #{delta}, "
			+ "balance_sync_time = now(), gmtModified = now() where id = #{id}")
	int addAvailableBalance(@Param("id") Long id, @Param("delta") BigDecimal delta);

	/** 卡充值扣款：余额充足时原子扣减 */
	@Update("update wallet_account set available_balance = ifnull(available_balance, 0) - #{delta}, "
			+ "balance_sync_time = now(), gmtModified = now() "
			+ "where id = #{id} and ifnull(available_balance, 0) >= #{delta}")
	int deductAvailableBalance(@Param("id") Long id, @Param("delta") BigDecimal delta);

	/** 内部转账扣款：可转余额 = available - open_freeze */
	@Update("update wallet_account set available_balance = ifnull(available_balance, 0) - #{delta}, "
			+ "balance_sync_time = now(), gmtModified = now() "
			+ "where id = #{id} and (ifnull(available_balance, 0) - ifnull(open_freeze_balance, 0)) >= #{delta}")
	int deductTransferableBalance(@Param("id") Long id, @Param("delta") BigDecimal delta);

	/** 拒绝开卡：解冻开卡冻结金额回可用余额 */
	@Update("update wallet_account set available_balance = ifnull(available_balance, 0) + #{amount}, "
			+ "open_freeze_balance = ifnull(open_freeze_balance, 0) - #{amount}, "
			+ "balance_sync_time = now(), gmtModified = now() "
			+ "where id = #{id} and ifnull(open_freeze_balance, 0) >= #{amount}")
	int unfreezeOpenCardBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

	/** 申请开卡：从可用余额转入开卡冻结（余额须充足） */
	@Update("update wallet_account set available_balance = ifnull(available_balance, 0) - #{amount}, "
			+ "open_freeze_balance = ifnull(open_freeze_balance, 0) + #{amount}, "
			+ "balance_sync_time = now(), gmtModified = now() "
			+ "where id = #{id} and ifnull(available_balance, 0) >= #{amount}")
	int freezeOpenCardBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

	/** 开卡成功核销：仅扣减开卡冻结，不回退可用余额 */
	@Update("update wallet_account set open_freeze_balance = ifnull(open_freeze_balance, 0) - #{amount}, "
			+ "balance_sync_time = now(), gmtModified = now() "
			+ "where id = #{id} and ifnull(open_freeze_balance, 0) >= #{amount}")
	int settleOpenCardFreeze(@Param("id") Long id, @Param("amount") BigDecimal amount);

	@Select("select * from wallet_account where tron_usdt_address = #{address} limit 1")
	WalletAccountEntity findByTronUsdtAddress(@Param("address") String address);

	@Select("select wa.pay_password  from wallet_account wa where wa.wallet_user_id = (select id from wallet_user wu where wu.local_uid = #{uid})")
	String selectPayPasswordById(@Param("uid") Integer uid);
}
