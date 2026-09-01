package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 钱包 U 卡开卡申请。
 */
@Repository
public interface WalletCardApplyDao extends BaseMapper<WalletCardApplyEntity> {

	@Select("select * from wallet_card_apply where request_order_id = #{requestOrderId} limit 1")
	WalletCardApplyEntity findByRequestOrderId(@Param("requestOrderId") String requestOrderId);

	@Select("select * from wallet_card_apply where wallet_user_id = #{walletUserId} "
			+ "order by setTime desc, id desc")
	List<WalletCardApplyEntity> findByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_card_apply where wallet_user_id = #{walletUserId} "
			+ "order by setTime desc, id desc limit 1")
	WalletCardApplyEntity findLatestByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Update("update wallet_card_apply set apply_state = #{applyState}, apply_state_name = #{applyStateName}, "
			+ "reject_info = #{rejectInfo}, gmtModified = now() where id = #{id}")
	int updateApplyState(@Param("id") Long id, @Param("applyState") Integer applyState,
			@Param("applyStateName") String applyStateName, @Param("rejectInfo") String rejectInfo);

	@Update("update wallet_card_apply set kyc_state = #{kycState}, kyc_state_name = #{kycStateName}, "
			+ "kyc_audit_result = #{kycAuditResult}, gmtModified = now() where id = #{id}")
	int updateKycSnapshot(@Param("id") Long id, @Param("kycState") Integer kycState,
			@Param("kycStateName") String kycStateName, @Param("kycAuditResult") String kycAuditResult);

	@Select("select * from wallet_card_apply where wallet_user_id = #{walletUserId} "
			+ "and apply_state = 1 and upper(card_type) = 'VIRTUAL' order by setTime asc, id asc")
	List<WalletCardApplyEntity> findPendingVirtualByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_card_apply where kyc_state = #{kycState} order by setTime asc, id asc")
	List<WalletCardApplyEntity> findByKycState(@Param("kycState") Integer kycState);

	@Select("select * from wallet_card_apply where wallet_user_id = #{walletUserId} "
			+ "and kyc_state = #{kycState} order by setTime asc, id asc")
	List<WalletCardApplyEntity> findByWalletUserIdAndKycState(@Param("walletUserId") Long walletUserId,
			@Param("kycState") Integer kycState);

	@Update("update wallet_card_apply set kyc_state = #{kycState}, kyc_state_name = #{kycStateName}, "
			+ "shipping_state = #{shippingState}, shipping_state_name = #{shippingStateName}, gmtModified = now() "
			+ "where wallet_user_id = #{walletUserId} and apply_state = 1")
	int updateKycAndShippingByWalletUserId(@Param("walletUserId") Long walletUserId,
			@Param("kycState") Integer kycState, @Param("kycStateName") String kycStateName,
			@Param("shippingState") Integer shippingState, @Param("shippingStateName") String shippingStateName);

	@Select("select * from wallet_card_apply where logistics_num = #{logisticsNum}")
	List<WalletCardApplyEntity> findByLogisticsNum(@Param("logisticsNum") String logisticsNum);

	@Update("update wallet_card_apply set shipping_state = #{shippingState}, "
			+ "shipping_state_name = #{shippingStateName}, logistics_num = #{logisticsNum}, "
			+ "shipping_time = #{shippingTime}, logistics_monery = #{logisticsMonery}, gmtModified = now() "
			+ "where id = #{id}")
	int updateShipping(@Param("id") Long id, @Param("shippingState") Integer shippingState,
			@Param("shippingStateName") String shippingStateName, @Param("logisticsNum") String logisticsNum,
			@Param("shippingTime") Date shippingTime, @Param("logisticsMonery") BigDecimal logisticsMonery);

	@Select("<script>"
			+ "select * from wallet_card_apply where 1=1"
			+ "<if test='walletUid != null'> and wallet_uid = #{walletUid}</if>"
			+ "<if test='walletUserId != null'> and wallet_user_id = #{walletUserId}</if>"
			+ "<if test='cardProductId != null'> and card_product_id = #{cardProductId}</if>"
			+ "<if test='cardType != null and cardType != \"\"'> and card_type = #{cardType}</if>"
			+ "<if test='applyState != null'> and apply_state = #{applyState}</if>"
			+ "<if test='topupType != null'> and topup_type = #{topupType}</if>"
			+ "<if test='kycState != null'> and kyc_state = #{kycState}</if>"
			+ " order by setTime desc, id desc"
			+ "</script>")
	List<WalletCardApplyEntity> findAdminList(WalletCardApplyEntity entity);
}
