package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaAuditStepEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaAuditStepDao extends BaseMapper<DramaAuditStepEntity> {

	@Select("select * from drama_audit_step where drama_id = #{dramaId} order by step_type asc")
	List<DramaAuditStepEntity> findByDramaId(@Param("dramaId") Integer dramaId);

	@Select("select * from drama_audit_step where drama_id = #{dramaId} and step_type = #{stepType} limit 1")
	DramaAuditStepEntity findByDramaIdAndStepType(@Param("dramaId") Integer dramaId,
			@Param("stepType") Integer stepType);

	@Delete("delete from drama_audit_step where drama_id = #{dramaId}")
	void deleteByDramaId(@Param("dramaId") Integer dramaId);
}
