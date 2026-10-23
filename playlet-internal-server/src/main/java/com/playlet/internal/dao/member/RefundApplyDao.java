package com.playlet.internal.dao.member;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.request.RefundApplyQuery;
import com.playlet.internal.api.response.RefundApplyItemEntity;
import com.playlet.internal.entity.member.RefundApplyEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 退款申请 Dao。
 */
@Repository
public interface RefundApplyDao extends BaseMapper<RefundApplyEntity> {

	@Select("select * from refund_apply where apply_no = #{applyNo} limit 1")
	RefundApplyEntity findByApplyNo(@Param("applyNo") String applyNo);

	@Select("select * from refund_apply where order_no = #{orderNo} and status = 0 limit 1")
	RefundApplyEntity findPendingByOrderNo(@Param("orderNo") String orderNo);

	@Select("<script>"
			+ "SELECT r.id AS id, r.apply_no AS applyNo, r.order_no AS orderNo,"
			+ "  r.product_type AS productType, r.uid AS uid, a.nickname AS nickname,"
			+ "  r.refund_reason AS refundReason, r.refund_amount AS refundAmount,"
			+ "  r.setTime AS setTime, r.status AS status,"
			+ "  r.handle_time AS handleTime, r.handle_remark AS handleRemark"
			+ " FROM refund_apply r"
			+ " LEFT JOIN app_account a ON a.id = r.uid"
			+ " WHERE 1=1"
			+ "<if test='q.keyword != null and q.keyword != \"\"'>"
			+ "  AND (r.order_no = #{q.keyword}"
			+ "    OR CAST(r.uid AS CHAR) = #{q.keyword}"
			+ "    OR a.nickname LIKE CONCAT('%', #{q.keyword}, '%'))"
			+ "</if>"
			+ "<if test='q.status != null'> AND r.status = #{q.status} </if>"
			+ " ORDER BY r.setTime DESC, r.id DESC"
			+ "</script>")
	List<RefundApplyItemEntity> findAdminList(@Param("q") RefundApplyQuery query);
}
