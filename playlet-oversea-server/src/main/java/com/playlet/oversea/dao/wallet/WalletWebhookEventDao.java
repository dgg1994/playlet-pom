package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletWebhookEventEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 钱包 WebHook 事件。
 */
@Repository
public interface WalletWebhookEventDao extends BaseMapper<WalletWebhookEventEntity> {

	@Select("select * from wallet_webhook_event where event_id = #{eventId} limit 1")
	WalletWebhookEventEntity findByEventId(@Param("eventId") String eventId);

	@Select("select * from wallet_webhook_event where process_status = #{processStatus} "
			+ "order by setTime asc, id asc limit #{limit}")
	List<WalletWebhookEventEntity> findByProcessStatus(@Param("processStatus") Integer processStatus,
			@Param("limit") Integer limit);

	@Update("update wallet_webhook_event set process_status = #{processStatus}, "
			+ "process_msg = #{processMsg}, retry_count = retry_count + 1, gmtModified = now() "
			+ "where id = #{id}")
	int updateProcessResult(@Param("id") Long id, @Param("processStatus") Integer processStatus,
			@Param("processMsg") String processMsg);
}
