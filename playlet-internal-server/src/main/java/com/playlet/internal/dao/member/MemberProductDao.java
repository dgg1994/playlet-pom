package com.playlet.internal.dao.member;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.member.MemberProductEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会员套餐 Dao。
 */
@Repository
public interface MemberProductDao extends BaseMapper<MemberProductEntity> {

	@Select("select * from member_product where status = 1 order by sort_weight desc, id asc")
	List<MemberProductEntity> findEnabledList();

	@Select("select * from member_product where product_code = #{productCode} limit 1")
	MemberProductEntity findByProductCode(@Param("productCode") String productCode);

	@Select("<script>"
			+ "select * from member_product where 1=1 "
			+ "<if test='productCode != null and productCode != \"\"'> and product_code = #{productCode} </if>"
			+ "<if test='productName != null and productName != \"\"'> "
			+ "  and product_name like concat('%',#{productName},'%') "
			+ "</if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "order by sort_weight desc, id asc"
			+ "</script>")
	List<MemberProductEntity> findAdminList(MemberProductEntity entity);
}
