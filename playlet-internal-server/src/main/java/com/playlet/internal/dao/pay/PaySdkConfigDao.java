package com.playlet.internal.dao.pay;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.pay.PaySdkConfigEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 支付 SDK 商户参数配置 Dao。
 */
@Repository
public interface PaySdkConfigDao extends BaseMapper<PaySdkConfigEntity> {

	@Select("select * from pay_sdk_config where config_key = #{configKey} limit 1")
	PaySdkConfigEntity findByKey(String configKey);

	@Select("select * from pay_sdk_config order by id asc")
	List<PaySdkConfigEntity> findList();
}
