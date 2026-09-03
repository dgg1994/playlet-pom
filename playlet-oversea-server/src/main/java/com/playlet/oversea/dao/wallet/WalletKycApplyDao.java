package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletKycApplyEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 钱包 KYC 申请流水。
 */
@Repository
public interface WalletKycApplyDao extends BaseMapper<WalletKycApplyEntity> {

	@Select("select * from wallet_kyc_apply where wallet_user_id = #{walletUserId} "
			+ "order by setTime desc, id desc")
	List<WalletKycApplyEntity> findByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_kyc_apply where wallet_user_id = #{walletUserId} "
			+ "order by setTime desc, id desc limit 1")
	WalletKycApplyEntity findLatestByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_kyc_apply where wallet_uid = #{walletUid} "
			+ "order by setTime desc, id desc limit 1")
	WalletKycApplyEntity findLatestByWalletUid(@Param("walletUid") Long walletUid);

	/** 更新申请状态（Webhook / 轮询） */
	@Update("update wallet_kyc_apply set apply_status = #{applyStatus}, kyc_state = #{kycState}, "
			+ "failed_reason = #{failedReason}, gmtModified = now() where id = #{id}")
	int updateStatus(@Param("id") Long id, @Param("applyStatus") String applyStatus,
			@Param("kycState") Integer kycState, @Param("failedReason") String failedReason);
}
