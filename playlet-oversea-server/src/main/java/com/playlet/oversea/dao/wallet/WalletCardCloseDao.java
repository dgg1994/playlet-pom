package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletCardCloseEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 销卡记录 DAO。
 */
@Repository
public interface WalletCardCloseDao extends BaseMapper<WalletCardCloseEntity> {

	@Select("<script>"
			+ "select c.*, wu.email as userEmail from wallet_card_close c "
			+ "left join wallet_user wu on c.wallet_user_id = wu.id where 1=1 "
			+ "<if test='cardNo != null and cardNo != \"\"'> and c.card_no like concat('%', #{cardNo}, '%') </if>"
			+ "<if test='requestOrderId != null and requestOrderId != \"\"'> and c.request_order_id = #{requestOrderId} </if>"
			+ "<if test='userEmail != null and userEmail != \"\"'> and wu.email like concat('%', #{userEmail}, '%') </if>"
			+ " order by c.setTime desc"
			+ "</script>")
	List<WalletCardCloseEntity> findList(WalletCardCloseEntity entity);
}
