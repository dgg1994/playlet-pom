package com.playlet.internal.dao.coupon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.coupon.CouponIssueLogEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 优惠券发放流水 Dao。
 */
@Repository
public interface CouponIssueLogDao extends BaseMapper<CouponIssueLogEntity> {

	@Select("select * from coupon_issue_log where batch_no = #{batchNo} limit 1")
	CouponIssueLogEntity findByBatchNo(@Param("batchNo") String batchNo);

	@Select("<script>"
			+ "select * from coupon_issue_log where 1=1 "
			+ "<if test='templateId != null'> and template_id = #{templateId} </if>"
			+ "<if test='couponCode != null and couponCode != \"\"'> and coupon_code = #{couponCode} </if>"
			+ "<if test='issueType != null and issueType != \"\"'> and issue_type = #{issueType} </if>"
			+ "<if test='batchNo != null and batchNo != \"\"'> and batch_no = #{batchNo} </if>"
			+ "order by id desc"
			+ "</script>")
	List<CouponIssueLogEntity> findAdminList(CouponIssueLogEntity entity);
}
