package com.playlet.internal.dao.coupon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.coupon.CouponTemplateEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 优惠券模板 Dao。
 */
@Repository
public interface CouponTemplateDao extends BaseMapper<CouponTemplateEntity> {

	@Select("select * from coupon_template where coupon_code = #{couponCode} limit 1")
	CouponTemplateEntity findByCouponCode(@Param("couponCode") String couponCode);

	@Select("select * from coupon_template where status = 1 order by id desc")
	List<CouponTemplateEntity> findEnabledList();

	@Select("<script>"
			+ "select * from coupon_template where 1=1 "
			+ "<if test='couponCode != null and couponCode != \"\"'> and coupon_code = #{couponCode} </if>"
			+ "<if test='couponName != null and couponName != \"\"'> "
			+ "  and coupon_name like concat('%',#{couponName},'%') "
			+ "</if>"
			+ "<if test='couponType != null and couponType != \"\"'> and coupon_type = #{couponType} </if>"
			+ "<if test='scope != null and scope != \"\"'> and scope = #{scope} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "order by id desc"
			+ "</script>")
	List<CouponTemplateEntity> findAdminList(CouponTemplateEntity entity);

	/** 发放成功后累加已发放数（带总上限乐观校验） */
	@Update("<script>"
			+ "update coupon_template set issued_count = issued_count + #{delta}, gmtModified = now() "
			+ "where id = #{id} "
			+ "<if test='checkLimit'>"
			+ "  and (total_limit is null or issued_count + #{delta} &lt;= total_limit) "
			+ "</if>"
			+ "</script>")
	int incrIssuedCount(@Param("id") Integer id, @Param("delta") int delta, @Param("checkLimit") boolean checkLimit);

	@Update("update coupon_template set used_count = used_count + 1, gmtModified = now() where id = #{id}")
	int incrUsedCount(@Param("id") Integer id);
}
