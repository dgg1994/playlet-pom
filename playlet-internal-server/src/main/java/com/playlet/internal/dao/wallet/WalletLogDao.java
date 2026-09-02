package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletLogEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包账变 DAO。
 */
@Repository
public interface WalletLogDao extends BaseMapper<WalletLogEntity> {

	@Select("<script>"
			+ "select * from wallet_log where 1=1 "
			+ "<if test='orderNo != null'> and order_no = #{orderNo}</if>"
			+ "<if test='tradeType != null'> and trade_type = #{tradeType}</if>"
			+ "<if test='walletUserId != null'> and wallet_user_id = #{walletUserId}</if>"
			+ "<if test='walletUid != null'> and wallet_uid = #{walletUid}</if>"
			+ "<if test='title != null'> and title = #{title}</if>"
			+ "<if test='status != null'> and status = #{status}</if>"
			+ "<if test='userEmail != null and userEmail != \"\"'> "
			+ "and wallet_user_id in (select id from wallet_user where email like concat('%', #{userEmail}, '%')) </if>"
			+ "<if test='toAccount != null'> and to_account = #{toAccount}</if>"
			+ "<if test='yearsMonth != null'> and date_format(setTime, '%Y-%m') = #{yearsMonth}</if>"
			+ "<if test='operateTypeList != null and operateTypeList.size() &gt; 0'> and operate_type in "
			+ "<foreach item='item' collection='operateTypeList' open='(' separator=',' close=')'>#{item}</foreach>"
			+ "</if>"
			+ " order by setTime desc"
			+ "</script>")
	List<WalletLogEntity> findByConditions(WalletLogEntity entity);

	@Select("<script>"
			+ "select ifnull(sum(real_money),0) + ifnull(sum(service_charge),0) from wallet_log "
			+ "where trade_type = 1 "
			+ "<if test='walletUid != null'> and wallet_uid = #{walletUid}</if>"
			+ "<if test='yearsMonth != null'> and date_format(setTime, '%Y-%m') = #{yearsMonth}</if>"
			+ "<if test='operateTypeList != null and operateTypeList.size() &gt; 0'> and operate_type in "
			+ "<foreach item='item' collection='operateTypeList' open='(' separator=',' close=')'>#{item}</foreach>"
			+ "</if>"
			+ "</script>")
	BigDecimal sumTotalIncome(WalletLogEntity entity);

	@Select("<script>"
			+ "select ifnull(sum(real_money),0) + ifnull(sum(service_charge),0) from wallet_log "
			+ "where trade_type = 2 "
			+ "<if test='walletUid != null'> and wallet_uid = #{walletUid}</if>"
			+ "<if test='yearsMonth != null'> and date_format(setTime, '%Y-%m') = #{yearsMonth}</if>"
			+ "<if test='operateTypeList != null and operateTypeList.size() &gt; 0'> and operate_type in "
			+ "<foreach item='item' collection='operateTypeList' open='(' separator=',' close=')'>#{item}</foreach>"
			+ "</if>"
			+ "</script>")
	BigDecimal sumTotalExpenses(WalletLogEntity entity);

	@Select("select * from wallet_log where out_order_no = #{outOrderNo} limit 1")
	WalletLogEntity findByOutOrderNo(@Param("outOrderNo") String outOrderNo);
}
