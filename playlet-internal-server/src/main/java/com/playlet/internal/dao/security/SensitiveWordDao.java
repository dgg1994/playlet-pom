package com.playlet.internal.dao.security;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.security.SensitiveWordEntity;
import org.springframework.stereotype.Repository;

/**
 * 类描述：敏感词库
 *
 * @author GeminiSun
 * @date 2026/08/11 17:47
 */
@Repository
public interface SensitiveWordDao extends BaseMapper<SensitiveWordEntity>  {
}
