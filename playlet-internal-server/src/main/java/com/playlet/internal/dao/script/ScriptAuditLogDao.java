package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.script.ScriptAuditLogEntity;
import org.springframework.stereotype.Repository;

/**
 * 审核/上下架日志。
 */
@Repository
public interface ScriptAuditLogDao extends BaseMapper<ScriptAuditLogEntity> {
}
