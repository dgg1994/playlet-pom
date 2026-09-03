package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletCardLabelEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * U 卡标签字典。
 */
@Repository
public interface WalletCardLabelDao extends BaseMapper<WalletCardLabelEntity> {

	@Select("select * from wallet_card_label where id in ("
			+ "select label_id from wallet_card_label_join where card_id = #{cardId} and language = #{language})")
	List<WalletCardLabelEntity> findByCardId(@Param("cardId") String cardId, @Param("language") String language);

	@Select("select * from wallet_card_label where name = #{name} limit 1")
	WalletCardLabelEntity findByName(@Param("name") String name);

	@Select("<script>"
			+ "select * from wallet_card_label where 1=1"
			+ "<if test='name != null and name != \"\"'> and name = #{name}</if>"
			+ "<if test='language != null and language != \"\"'> and language = #{language}</if>"
			+ " order by language, id desc"
			+ "</script>")
	List<WalletCardLabelEntity> findList(WalletCardLabelEntity entity);

	@Select("select id from wallet_card_label where id in ("
			+ "select label_id from wallet_card_label_join where card_id = #{cardId})")
	List<Integer> queryLabelIdsByCardId(@Param("cardId") String cardId);
}
