package com.playlet.internal.dao.security;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.IllegalCommentRecordListResp;
import com.playlet.internal.entity.security.IllegalCommentRecordEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IllegalCommentRecordDao extends BaseMapper<IllegalCommentRecordEntity> {

	@Select("<script>"
			+ "select * from illegal_comment_record where 1=1 "
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "<if test='riskLevel != null'> and risk_level = #{riskLevel} </if>"
			+ "<if test='userId != null'> and user_id = #{userId} </if>"
			+ "<if test='dramaId != null'> and drama_id = #{dramaId} </if>"
			+ "<if test='sourceType != null'> and source_type = #{sourceType} </if>"
			+ "<if test='commentId != null'> and comment_id = #{commentId} </if>"
			+ "order by setTime desc, id desc"
			+ "</script>")
	List<IllegalCommentRecordEntity> findAdminList(IllegalCommentRecordEntity entity);

	@Select("<script>"
			+ "select icr.id, icr.comment_id as commentId, icr.user_id as userId, "
			+ "aa.avatar as userAvatar, aa.nickname as userNickname, "
			+ "icr.drama_id as dramaId, d.drama_title as dramaTitle, "
			+ "icr.episode_id as episodeId, da.set_num as episodeNum, "
			+ "case "
			+ " when icr.comment_id is null then 'BLOCKED' "
			+ " when ifnull(dvc.parent_id,0) = 0 then 'PUBLISH' "
			+ " else 'REPLY' end as commentActionType, "
			+ "icr.content, icr.sensitive_words as sensitiveWords, icr.risk_level as riskLevel, "
			+ "icr.status, icr.handle_type as handleType, icr.handle_remark as handleRemark, "
			+ "icr.source_type as sourceType, icr.setTime "
			+ "from illegal_comment_record icr "
			+ "left join app_account aa on aa.id = icr.user_id "
			+ "left join drama d on d.id = icr.drama_id "
			+ "left join drama_asset da on da.id = icr.episode_id "
			+ "left join drama_video_comment dvc on dvc.id = icr.comment_id "
			+ "where 1=1 "
			+ "<if test='status != null'> and icr.status = #{status} </if>"
			+ "<if test='riskLevel != null'> and icr.risk_level = #{riskLevel} </if>"
			+ "<if test='userId != null'> and icr.user_id = #{userId} </if>"
			+ "<if test='dramaId != null'> and icr.drama_id = #{dramaId} </if>"
			+ "<if test='sourceType != null'> and icr.source_type = #{sourceType} </if>"
			+ "<if test='commentId != null'> and icr.comment_id = #{commentId} </if>"
			+ "order by icr.setTime desc, icr.id desc"
			+ "</script>")
	List<IllegalCommentRecordListResp> findAdminViewList(IllegalCommentRecordEntity entity);

	@Select("select count(1) from illegal_comment_record where id = #{id} and status = 0")
	int countPendingById(@Param("id") Long id);
}
