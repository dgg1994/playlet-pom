package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaRecordAuditStepEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DramaRecordAuditStepDao extends BaseMapper<DramaRecordAuditStepEntity> {

	@Select("SELECT * FROM drama_record_audit_step WHERE drama_id = #{dramaId} AND step_type = #{stepType} LIMIT 1")
	DramaRecordAuditStepEntity findByDramaIdAndStepType(@Param("dramaId") Integer dramaId,
			@Param("stepType") Integer stepType);
}
