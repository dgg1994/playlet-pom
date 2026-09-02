package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletManualLogEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 人工充值日志 DAO。
 */
@Repository
public interface WalletManualLogDao extends BaseMapper<WalletManualLogEntity> {

	@Select("<script>"
			+ "select * from wallet_manual_log where 1=1 "
			+ "<if test='localUid != null'> and local_uid = #{localUid} </if>"
			+ "<if test='uid != null and uid != \"\"'> and local_uid = #{uid} </if>"
			+ "<if test='userEmail != null and userEmail != \"\"'> and user_email like concat('%', #{userEmail}, '%') </if>"
			+ "<if test='orderNo != null and orderNo != \"\"'> and order_no = #{orderNo} </if>"
			+ " order by setTime desc"
			+ "</script>")
	List<WalletManualLogEntity> findList(WalletManualLogEntity entity);
}
