package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletUsdtTopupEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * USDT 链上充值流水。
 */
@Repository
public interface WalletUsdtTopupDao extends BaseMapper<WalletUsdtTopupEntity> {

	@Select("select * from wallet_usdt_topup_log where tx_hash = #{txHash} limit 1")
	WalletUsdtTopupEntity findByTxHash(@Param("txHash") String txHash);

	@Select("<script>"
			+ "select t.*, wu.email as userEmail from wallet_usdt_topup_log t "
			+ "left join wallet_user wu on t.wallet_user_id = wu.id where 1=1 "
			+ "<if test='userEmail != null and userEmail != \"\"'> and wu.email like concat('%', #{userEmail}, '%') </if>"
			+ "<if test='orderNo != null and orderNo != \"\"'> and t.order_no = #{orderNo} </if>"
			+ "<if test='walletAddressFilter != null and walletAddressFilter != \"\"'> "
			+ "and (t.in_address like concat('%', #{walletAddressFilter}, '%') "
			+ "or t.out_address like concat('%', #{walletAddressFilter}, '%')) </if>"
			+ "<if test='networkType != null and networkType != \"\"'> "
			+ "and (t.network_type = #{networkType} or t.out_address like concat('%', #{networkType}, '%')) </if>"
			+ " order by t.setTime desc"
			+ "</script>")
	java.util.List<WalletUsdtTopupEntity> findAdminList(WalletUsdtTopupEntity entity);
}
