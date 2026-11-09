package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.CreatorScriptApplyItemRespEntity;
import com.playlet.internal.entity.script.ScriptTradeApplyEntity;
import com.playlet.internal.query.script.CreatorScriptApplyListQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 购买/议价申请。
 */
@Repository
public interface ScriptTradeApplyDao extends BaseMapper<ScriptTradeApplyEntity> {

	@Select("select count(*) from script_trade_apply where script_id = #{scriptId} and apply_status = 0")
	int countPendingByScript(@Param("scriptId") Integer scriptId);

	@Select("select * from script_trade_apply where script_id = #{scriptId} and buyer_uid = #{buyerUid} "
			+ "and apply_status = 0 limit 1")
	ScriptTradeApplyEntity findPendingByBuyer(@Param("scriptId") Integer scriptId,
			@Param("buyerUid") Integer buyerUid);

	@Select("<script>"
			+ "select a.id as applyId, a.apply_no as applyNo, a.script_id as scriptId, "
			+ "  a.script_name_snapshot as scriptName, aa.nickname as buyerName, "
			+ "  a.contact_name as contactName, a.contact_mobile_cipher as contactMobileCipher, "
			+ "  a.apply_type as applyType, a.offer_price_yuan as offerPrice, a.message as message, "
			+ "  a.apply_status as applyStatus, a.setTime as setTime "
			+ "from script_trade_apply a left join app_account aa on aa.id = a.buyer_uid "
			+ "where a.author_id = #{authorId} "
			+ "<if test='q.applyType != null and q.applyType != 0'> and a.apply_type = #{q.applyType} </if>"
			+ "<if test='q.applyStatus != null and q.applyStatus != -1'> and a.apply_status = #{q.applyStatus} </if>"
			+ "<if test='q.scriptId != null'> and a.script_id = #{q.scriptId} </if>"
			+ "order by a.setTime desc, a.id desc"
			+ "</script>")
	List<CreatorScriptApplyItemRespEntity> findApplyList(@Param("q") CreatorScriptApplyListQuery q,
			@Param("authorId") Integer authorId);

	@Update("update script_trade_apply set apply_status = #{toStatus}, handle_remark = #{remark}, "
			+ "  handle_time = now(), gmtModified = now() "
			+ "where id = #{id} and apply_status = #{fromStatus}")
	int updateStatusCas(@Param("id") Long id,
			@Param("fromStatus") Integer fromStatus,
			@Param("toStatus") Integer toStatus,
			@Param("remark") String remark);

	@Update("update script_trade_apply set apply_status = 5, handle_time = now(), gmtModified = now() "
			+ "where script_id = #{scriptId} and apply_status = 0 and id != #{applyId}")
	int expireOtherPending(@Param("scriptId") Integer scriptId, @Param("applyId") Long applyId);
}
