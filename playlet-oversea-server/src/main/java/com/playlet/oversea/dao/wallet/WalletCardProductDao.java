package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletCardProductEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 钱包 U 卡产品缓存。
 */
@Repository
public interface WalletCardProductDao extends BaseMapper<WalletCardProductEntity> {

	@Select("<script>"
			+ "select * from wallet_card_product where enable = 1 "
			+ "<if test='bankcardNature != null and bankcardNature != \"\"'>"
			+ " and bankcard_nature = #{bankcardNature} "
			+ "</if>"
			+ "order by hot desc, id asc"
			+ "</script>")
	List<WalletCardProductEntity> findEnabledList(@Param("bankcardNature") String bankcardNature);

	@Select("<script>"
			+ "select * from wallet_card_product where 1 = 1 "
			+ "<if test='enable != null'> and enable = #{enable} </if>"
			+ "<if test='cardTitle != null and cardTitle != \"\"'> and card_title like concat('%', #{cardTitle}, '%') </if>"
			+ "<if test='title != null and title != \"\"'> and card_title like concat('%', #{title}, '%') </if>"
			+ "<if test='bankcardNature != null and bankcardNature != \"\"'> and bankcard_nature = #{bankcardNature} </if>"
			+ "<if test='bankCardNatureFilter != null and bankCardNatureFilter != \"\"'> and bankcard_nature = #{bankCardNatureFilter} </if>"
			+ "order by hot desc, id asc"
			+ "</script>")
	List<WalletCardProductEntity> findAdminList(WalletCardProductEntity entity);

	@Select("select * from wallet_card_product where product_uuid = #{productUuid} limit 1")
	WalletCardProductEntity findByProductUuid(@Param("productUuid") String productUuid);

	@Select("select ifnull(max(id), 0) + 1 from wallet_card_product")
	Integer nextProductId();

	@Update("update wallet_card_product set enable = #{enable}, gmtModified = now() where id = #{id}")
	int updateEnableById(@Param("id") Integer id, @Param("enable") Integer enable);

	@Update("update wallet_card_product set card_img = #{cardImg}, gmtModified = now() where id = #{id}")
	int updateCardImgById(@Param("id") Integer id, @Param("cardImg") String cardImg);

	@Update("update wallet_card_product set card_list_img = #{cardListImg}, gmtModified = now() where id = #{id}")
	int updateCardListImgById(@Param("id") Integer id, @Param("cardListImg") String cardListImg);

	@Select("select * from wallet_card_product where id = #{id} limit 1")
	WalletCardProductEntity findById(@Param("id") Integer id);

	/** 列表展示：批量取卡图与卡名称（显式别名，避免下划线未映射导致 cardImg 为空/未签名） */
	@Select("<script>select id, card_img as cardImg, card_list_img as cardListImg, card_title as cardTitle "
			+ "from wallet_card_product where id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<WalletCardProductEntity> findCardImgByIds(@Param("ids") List<Integer> ids);
}
