package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletCardLabelJoinEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * U 卡产品与标签关联。
 */
@Repository
public interface WalletCardLabelJoinDao extends BaseMapper<WalletCardLabelJoinEntity> {

	@Delete("delete from wallet_card_label_join where card_id = #{cardId}")
	void deleteByCardId(@Param("cardId") String cardId);

	@Delete("delete from wallet_card_label_join where label_id = #{labelId}")
	void deleteByLabelId(@Param("labelId") Integer labelId);
}
