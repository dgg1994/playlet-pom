package com.playlet.internal.dao.pay;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.pay.PayGlobalConfigEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 支付全局配置 Dao。
 */
@Repository
public interface PayGlobalConfigDao extends BaseMapper<PayGlobalConfigEntity> {

	@Select("select * from pay_global_config order by id asc limit 1")
	PayGlobalConfigEntity findOne();
}
