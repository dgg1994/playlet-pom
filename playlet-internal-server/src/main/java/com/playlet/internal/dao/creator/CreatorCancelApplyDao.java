package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorCancelApplyEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 创作者注销申请。
 */
@Repository
public interface CreatorCancelApplyDao extends BaseMapper<CreatorCancelApplyEntity> {

	/** 取当前未终结的申请（冷静期 / 已撤销 / 已驳回均可查，用于展示状态） */
	@Select("select * from creator_cancel_apply where creator_id = #{creatorId} "
			+ "order by id desc limit 1")
	CreatorCancelApplyEntity findLatestByCreator(@Param("creatorId") Integer creatorId);

	/** 取冷静期已到期、尚未处理的申请（定时任务扫） */
	@Select("select * from creator_cancel_apply where status = 1 and cancel_deadline < now()")
	List<CreatorCancelApplyEntity> findExpiredCoolDown();
}
