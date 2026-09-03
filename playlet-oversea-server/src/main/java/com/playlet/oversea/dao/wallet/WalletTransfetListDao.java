package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletTransfetListEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 内部转账记录 DAO。
 */
@Repository
public interface WalletTransfetListDao extends BaseMapper<WalletTransfetListEntity> {

	@Select("select * from wallet_transfet_list where send_wallet_uid = #{walletUid} order by setTime desc")
	List<WalletTransfetListEntity> findBySendWalletUid(@Param("walletUid") Long walletUid);

	@Select("<script>"
			+ "select * from wallet_transfet_list where 1=1 "
			+ "<if test='orderNo != null'> and order_no = #{orderNo}</if>"
			+ "<if test='sendWalletUid != null'> and send_wallet_uid = #{sendWalletUid}</if>"
			+ "<if test='sendEmail != null'> and send_email like concat('%', #{sendEmail}, '%')</if>"
			+ "<if test='recipientWalletUid != null'> and recipient_wallet_uid = #{recipientWalletUid}</if>"
			+ "<if test='recipientEmail != null'> and recipient_email like concat('%', #{recipientEmail}, '%')</if>"
			+ " order by setTime desc"
			+ "</script>")
	List<WalletTransfetListEntity> findList(WalletTransfetListEntity entity);
}
