package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletBankcardEntity;
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

	@Select("select * from wallet_bankcard where card_apply_id = #{cardApplyId} limit 1")
	WalletBankcardEntity findByCardApplyId(@Param("cardApplyId") Long cardApplyId);

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

	/** 更新用户自定义标签 */
	@Update("update wallet_bankcard set tag_name = #{tagName}, gmtModified = now() where id = #{id}")
	int updateTagName(@Param("id") Long id, @Param("tagName") String tagName);

	/** 取消该用户其它默认卡 */
	@Update("update wallet_bankcard set is_default = 0, gmtModified = now() "
			+ "where wallet_user_id = #{walletUserId} and is_default = 1")
	int clearDefault(@Param("walletUserId") Long walletUserId);

	/** 设为默认提现卡 */
	@Update("update wallet_bankcard set is_default = 1, gmtModified = now() where id = #{id}")
	int markDefault(@Param("id") Long id);

	@Update("update wallet_bankcard set shipping_state = #{shippingState}, logistics_num = #{logisticsNum}, "
			+ "gmtModified = now() where id = #{id}")
	int updateLogistics(@Param("id") Long id, @Param("shippingState") Integer shippingState,
			@Param("logisticsNum") String logisticsNum);

	/** 管理端：用户持卡分页列表（关联 wallet_user / 申请持卡人） */
	@Select("<script>"
			+ "select b.id, cast(wu.local_uid as char) as uid, b.card_product_id as cardId, b.card_uuid as cardUuid, "
			+ "b.card_apply_id as applyId, b.bankcard_nature as cardType, b.card_no as cardNo, "
			+ "b.user_bankcard_id as userBankcardId, b.card_status as status, b.card_status_name as statusName, "
			+ "b.balance, b.tag_name as tagName, wu.email as userEmail, wu.mobile_number as userTel, "
			+ "m.user_name as userName, b.setTime "
			+ "from wallet_bankcard b "
			+ "left join wallet_user wu on b.wallet_user_id = wu.id "
			+ "left join wallet_card_apply_man m on b.card_apply_id = m.apply_id "
			+ "where 1=1 "
			+ "<if test='status != null'> and b.card_status = #{status} </if>"
			+ "<if test='cardType != null and cardType != \"\"'> and b.bankcard_nature = #{cardType} </if>"
			+ "<if test='cardNo != null and cardNo != \"\"'> and b.card_no like concat('%', #{cardNo}, '%') </if>"
			+ "<if test='userEmail != null and userEmail != \"\"'> and wu.email like concat('%', #{userEmail}, '%') </if>"
			+ "<if test='userTel != null and userTel != \"\"'> and wu.mobile_number like concat('%', #{userTel}, '%') </if>"
			+ "<if test='userName != null and userName != \"\"'> "
			+ "and (m.user_name like concat('%', #{userName}, '%') or m.user_surname like concat('%', #{userName}, '%')) "
			+ "</if>"
			+ "<if test='uid != null and uid != \"\"'> and wu.local_uid = #{uid} and wu.user_type = 1 </if>"
			+ " order by b.balance desc, b.id desc"
			+ "</script>")
	java.util.List<com.playlet.oversea.api.response.WalletBankcardAdminResp> findAdminList(
			com.playlet.oversea.query.wallet.WalletBankcardAdminQuery query);
}
