package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorWithdrawApplyEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 创作者提现申请。
 */
@Repository
public interface CreatorWithdrawApplyDao extends BaseMapper<CreatorWithdrawApplyEntity> {

	/** 批量查询（用于流水列表回填提现状态） */
	@Select("<script>"
			+ "select * from creator_withdraw_apply where id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<CreatorWithdrawApplyEntity> findByIds(@Param("ids") List<Integer> ids);
}
