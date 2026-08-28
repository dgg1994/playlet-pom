package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包用户 U 卡。
 */
@Repository
public interface WalletBankcardDao extends BaseMapper<WalletBankcardEntity> {

	@Select("select * from wallet_bankcard where user_bankcard_id = #{userBankcardId} limit 1")
	WalletBankcardEntity findByUserBankcardId(@Param("userBankcardId") Long userBankcardId);

	@Select("select * from wallet_bankcard where wallet_user_id = #{walletUserId} "
			+ "order by is_default desc, id desc")
	List<WalletBankcardEntity> findByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_bankcard where wallet_user_id = #{walletUserId} "
			+ "and is_default = 1 limit 1")
	WalletBankcardEntity findDefaultByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_bankcard where wallet_uid = #{walletUid} "
			+ "order by is_default desc, id desc")
	List<WalletBankcardEntity> findByWalletUid(@Param("walletUid") Long walletUid);

	/** 更新卡状态 */
	@Update("update wallet_bankcard set card_status = #{cardStatus}, card_status_name = #{cardStatusName}, "
			+ "gmtModified = now() where id = #{id}")
	int updateCardStatus(@Param("id") Long id, @Param("cardStatus") Integer cardStatus,
			@Param("cardStatusName") String cardStatusName);

	/** 同步余额缓存 */
	@Update("update wallet_bankcard set balance = #{balance}, gmtModified = now() where id = #{id}")
	int updateBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);

	/** 更新卡号（激活后回写） */
	@Update("update wallet_bankcard set card_no = #{cardNo}, gmtModified = now() where id = #{id}")
	int updateCardNo(@Param("id") Long id, @Param("cardNo") String cardNo);

	/** 更新 PIN 是否已设置 */
	@Update("update wallet_bankcard set pin_set = #{pinSet}, gmtModified = now() where id = #{id}")
	int updatePinSet(@Param("id") Long id, @Param("pinSet") Integer pinSet);

	/** 取消该用户其它默认卡 */
	@Update("update wallet_bankcard set is_default = 0, gmtModified = now() "
			+ "where wallet_user_id = #{walletUserId} and is_default = 1")
	int clearDefault(@Param("walletUserId") Long walletUserId);

	/** 设为默认提现卡 */
	@Update("update wallet_bankcard set is_default = 1, gmtModified = now() where id = #{id}")
	int markDefault(@Param("id") Long id);
}
