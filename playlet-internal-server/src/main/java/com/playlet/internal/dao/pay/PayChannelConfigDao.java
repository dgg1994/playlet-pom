package com.playlet.internal.dao.pay;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.pay.PayChannelConfigEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 支付渠道配置 Dao。
 */
@Repository
public interface PayChannelConfigDao extends BaseMapper<PayChannelConfigEntity> {

	@Select("select * from pay_channel_config order by id asc")
	List<PayChannelConfigEntity> findList();

	@Select("select * from pay_channel_config where channel = #{channel} limit 1")
	PayChannelConfigEntity findByChannel(String channel);
}
