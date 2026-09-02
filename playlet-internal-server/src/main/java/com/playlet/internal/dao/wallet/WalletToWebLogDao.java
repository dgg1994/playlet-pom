package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletToWebLogEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 链上提现记录 DAO。
 */
@Repository
public interface WalletToWebLogDao extends BaseMapper<WalletToWebLogEntity> {

	@Select("<script>"
			+ "select * from wallet_to_web_log where 1=1 "
			+ "<if test='userEmail != null and userEmail != \"\"'> and user_email like concat('%', #{userEmail}, '%') </if>"
			+ "<if test='orderNo != null and orderNo != \"\"'> and order_no = #{orderNo} </if>"
			+ "<if test='walletAddress != null and walletAddress != \"\"'> and wallet_address like concat('%', #{walletAddress}, '%') </if>"
			+ "<if test='networkType != null and networkType != \"\"'> and network_type = #{networkType} </if>"
			+ "<if test='applyState != null'> and apply_state = #{applyState} </if>"
			+ " order by setTime desc"
			+ "</script>")
	List<WalletToWebLogEntity> findList(WalletToWebLogEntity entity);
}
