package com.playlet.internal.dao.member;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.member.MemberOrderEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员订单 Dao。
 */
@Repository
public interface MemberOrderDao extends BaseMapper<MemberOrderEntity> {

	@Select("select * from member_order where order_no = #{orderNo} limit 1")
	MemberOrderEntity findByOrderNo(@Param("orderNo") String orderNo);

	@Select("select * from member_order where third_trade_no = #{thirdTradeNo} limit 1")
	MemberOrderEntity findByThirdTradeNo(@Param("thirdTradeNo") String thirdTradeNo);

	@Select("<script>"
			+ "select * from member_order where 1=1 "
			+ "<if test='uid != null'> and uid = #{uid} </if>"
			+ "<if test='orderNo != null and orderNo != \"\"'> and order_no = #{orderNo} </if>"
			+ "<if test='payStatus != null'> and pay_status = #{payStatus} </if>"
			+ "<if test='productId != null'> and product_id = #{productId} </if>"
			+ "order by id desc"
			+ "</script>")
	List<MemberOrderEntity> findAdminList(MemberOrderEntity entity);

	@Select("select ifnull(sum(pay_amount), 0) from member_order "
			+ "where uid = #{uid} and pay_status = 1")
	BigDecimal sumPaidAmountByUid(@Param("uid") Integer uid);

	@Select("select * from member_order where uid = #{uid} and pay_status = 1 "
			+ "order by id desc")
	List<MemberOrderEntity> findPaidByUid(@Param("uid") Integer uid);

	/** 按协议取历次续费订单（含失败单），用于推算下一期期数 */
	@Select("select * from member_order where agreement_id = #{agreementId} order by period_index desc")
	List<MemberOrderEntity> findByAgreementId(@Param("agreementId") Integer agreementId);
}
