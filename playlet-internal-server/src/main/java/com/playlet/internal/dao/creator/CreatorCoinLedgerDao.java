package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorCoinLedgerEntity;
import org.springframework.stereotype.Repository;

/**
 * 作家金币流水。
 */
@Repository
public interface CreatorCoinLedgerDao extends BaseMapper<CreatorCoinLedgerEntity> {
}
