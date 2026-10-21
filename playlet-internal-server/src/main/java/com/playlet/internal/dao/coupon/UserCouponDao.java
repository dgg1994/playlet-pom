package com.playlet.internal.dao.coupon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.coupon.UserCouponEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户优惠券 Dao。
 */
@Repository
public interface UserCouponDao extends BaseMapper<UserCouponEntity> {

	@Select("select count(1) from user_coupon where uid = #{uid} and template_id = #{templateId}")
	int countByUidAndTemplate(@Param("uid") Integer uid, @Param("templateId") Integer templateId);

	@Select("<script>"
			+ "select * from user_coupon where uid = #{uid} "
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "order by id desc"
			+ "</script>")
	List<UserCouponEntity> findByUid(@Param("uid") Integer uid, @Param("status") Integer status);

	@Select("<script>"
			+ "select * from user_coupon where 1=1 "
			+ "<if test='uid != null'> and uid = #{uid} </if>"
			+ "<if test='templateId != null'> and template_id = #{templateId} </if>"
			+ "<if test='couponCode != null and couponCode != \"\"'> and coupon_code = #{couponCode} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "<if test='issueBatchNo != null and issueBatchNo != \"\"'> and issue_batch_no = #{issueBatchNo} </if>"
			+ "order by id desc"
			+ "</script>")
	List<UserCouponEntity> findAdminList(UserCouponEntity entity);

	@Select("select * from user_coupon where id = #{id} and uid = #{uid} limit 1")
	UserCouponEntity findByIdAndUid(@Param("id") Integer id, @Param("uid") Integer uid);

	/** 未用 → 已用（乐观，防重复核销） */
	@Update("update user_coupon set status = 1, used_order_no = #{orderNo}, used_time = now(), gmtModified = now() "
			+ "where id = #{id} and uid = #{uid} and status = 0")
	int markUsed(@Param("id") Integer id, @Param("uid") Integer uid, @Param("orderNo") String orderNo);
}
