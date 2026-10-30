package com.playlet.internal.dao.drama;

import com.playlet.internal.api.response.AuditModuleStatRespEntity;
import com.playlet.internal.api.response.AuditUrgentTodoRespEntity;
import com.playlet.internal.api.response.DramaCategoryAuditStatRespEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审核汇总统计：聚合剧评审 / 剧集评审 / 备案审核 / 违规评论 / 敏感词 / 创作者入驻六个模块。
 * 未定义 @Mapper，由 @MapperScan(com.playlet.internal.dao) 统一扫描。
 */
public interface AuditSummaryDao {

	/**
	 * 各模块统计。所有指标用标量子查询组装，一条 SQL 返回六个模块行，
	 * 避免逐模块多次往返。SLA 用于统计“超时未处理”。
	 */
	@Select("SELECT 'DRAMA_INFO' AS module, '短剧信息审核' AS moduleName, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.audit_status IN (0,1,4) AND IFNULL(d.delete_state,0)=0) AS pendingCount, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.audit_status=2 AND IFNULL(d.delete_state,0)=0) AS passedCount, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.audit_status=3 AND IFNULL(d.delete_state,0)=0) AS rejectedCount, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.audit_status IS NOT NULL AND IFNULL(d.delete_state,0)=0) AS totalCount, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.audit_status=2 AND d.audit_pass_time>=CURDATE() AND IFNULL(d.delete_state,0)=0) AS todayCount, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.audit_status IN (0,1,4) AND IFNULL(d.delete_state,0)=0 "
			+ " AND TIMESTAMPDIFF(MINUTE,d.setTime,NOW())>=#{slaHours}*60) AS overdueCount, "
			+ "ROUND((SELECT AVG(TIMESTAMPDIFF(MINUTE,d.setTime,d.audit_pass_time)/60) FROM drama d "
			+ " WHERE d.audit_status=2 AND d.audit_pass_time IS NOT NULL AND IFNULL(d.delete_state,0)=0),1) AS avgHours, "
			+ "'AUDIT' AS statMode, '/audit/assetAudit/basicInfo' AS targetPath "

			+ "UNION ALL SELECT 'DRAMA_EPISODE', '剧集审核', "
			+ "(SELECT COUNT(*) FROM drama_asset_audit_step s WHERE s.status=0) AS pendingCount, "
			+ "(SELECT COUNT(*) FROM drama_asset_audit_step s WHERE s.status=1) AS passedCount, "
			+ "(SELECT COUNT(*) FROM drama_asset_audit_step s WHERE s.status=2) AS rejectedCount, "
			+ "(SELECT COUNT(*) FROM drama_asset_audit_step s) AS totalCount, "
			+ "(SELECT COUNT(*) FROM drama_asset_audit_step s WHERE s.status IN (1,2) AND s.handle_time>=CURDATE()) AS todayCount, "
			+ "(SELECT COUNT(*) FROM drama_asset_audit_step s WHERE s.status=0 "
			+ " AND TIMESTAMPDIFF(MINUTE,s.setTime,NOW())>=#{slaHours}*60) AS overdueCount, "
			+ "ROUND((SELECT AVG(TIMESTAMPDIFF(MINUTE,s.setTime,s.handle_time)/60) FROM drama_asset_audit_step s "
			+ " WHERE s.handle_time IS NOT NULL),1) AS avgHours, "
			+ "'AUDIT', '/audit/assetAudit/episode' "

			+ "UNION ALL SELECT 'DRAMA_RECORD', '备案审核', "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.record_status IN (0,1) AND IFNULL(d.delete_state,0)=0) AS pendingCount, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.record_status=2 AND IFNULL(d.delete_state,0)=0) AS passedCount, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.record_status IN (3,4) AND IFNULL(d.delete_state,0)=0) AS rejectedCount, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.record_status IS NOT NULL AND IFNULL(d.delete_state,0)=0) AS totalCount, "
			+ "(SELECT COUNT(*) FROM drama_record_audit_step s WHERE s.status IN (1,2) AND s.handle_time>=CURDATE()) AS todayCount, "
			+ "(SELECT COUNT(*) FROM drama d WHERE d.record_status IN (0,1) AND IFNULL(d.delete_state,0)=0 "
			+ " AND TIMESTAMPDIFF(MINUTE,d.setTime,NOW())>=1440) AS overdueCount, "
			+ "ROUND((SELECT AVG(TIMESTAMPDIFF(MINUTE,s.setTime,s.handle_time)/60) FROM drama_record_audit_step s "
			+ " WHERE s.handle_time IS NOT NULL),1) AS avgHours, "
			+ "'AUDIT', '/audit/recordAudit' "

			+ "UNION ALL SELECT 'COMMENT', '评论管理', "
			+ "(SELECT COUNT(*) FROM illegal_comment_record c WHERE c.status=0) AS pendingCount, "
			+ "(SELECT COUNT(*) FROM illegal_comment_record c WHERE c.status=1) AS passedCount, "
			+ "(SELECT COUNT(*) FROM illegal_comment_record c WHERE c.status IN (2,3,4)) AS rejectedCount, "
			+ "(SELECT COUNT(*) FROM illegal_comment_record c) AS totalCount, "
			+ "(SELECT COUNT(*) FROM illegal_comment_record c WHERE c.status IN (1,2,3,4) AND c.gmtModified>=CURDATE()) AS todayCount, "
			+ "(SELECT COUNT(*) FROM illegal_comment_record c WHERE c.status=0 "
			+ " AND TIMESTAMPDIFF(MINUTE,IFNULL(c.setTime,c.gmtModified),NOW())>=1440) AS overdueCount, "
			+ "ROUND((SELECT AVG(TIMESTAMPDIFF(MINUTE,IFNULL(c.setTime,c.gmtModified),c.gmtModified)/60) FROM illegal_comment_record c "
			+ " WHERE c.status IN (1,2,3,4)),1) AS avgHours, "
			+ "'AUDIT', '/audit/illegalComments' "

			+ "UNION ALL SELECT 'SENSITIVE_WORD', '敏感词管理', "
			+ "(SELECT COUNT(*) FROM sensitive_word w WHERE w.status=0) AS pendingCount, "
			+ "(SELECT COUNT(*) FROM sensitive_word w WHERE w.status=1) AS passedCount, "
			+ "(SELECT COUNT(*) FROM sensitive_word w WHERE w.status=2) AS rejectedCount, "
			+ "(SELECT COUNT(*) FROM sensitive_word w) AS totalCount, "
			+ "(SELECT COUNT(*) FROM illegal_comment_record c WHERE c.setTime>=CURDATE()) AS todayCount, "
			+ "(SELECT COUNT(*) FROM sensitive_word w WHERE w.status=0 "
			+ " AND TIMESTAMPDIFF(MINUTE,w.setTime,NOW())>=1440) AS overdueCount, "
			+ "ROUND((SELECT AVG(TIMESTAMPDIFF(MINUTE,w.setTime,w.audit_time)/60) FROM sensitive_word w "
			+ " WHERE w.audit_time IS NOT NULL),1) AS avgHours, "
			+ "'WORD', '/audit/sensitiveWords' "

			+ "UNION ALL SELECT 'CREATOR_ENTRY', '入驻申请', "
			+ "0 AS pendingCount, "
			+ "(SELECT COUNT(*) FROM creator_account ca WHERE ca.user_state=1) AS passedCount, "
			+ "(SELECT COUNT(*) FROM creator_account ca WHERE ca.user_state=2) AS rejectedCount, "
			+ "(SELECT COUNT(*) FROM creator_account ca) AS totalCount, "
			+ "(SELECT COUNT(*) FROM creator_account ca WHERE ca.setTime>=CURDATE()) AS todayCount, "
			+ "0 AS overdueCount, "
			+ "NULL AS avgHours, "
			+ "'ENTRY', '' ")
	List<AuditModuleStatRespEntity> findModuleStats(@Param("slaHours") int slaHours);

	/**
	 * 按备案分类统计：总提交数 / 通过数 / 平均审核时长（按剧聚合初审+复审步骤的平均值）。
	 */
	@Select("SELECT d.filing_category AS category, COUNT(*) AS totalCount, "
			+ "COALESCE(SUM(CASE WHEN d.record_status=2 THEN 1 ELSE 0 END),0) AS passedCount, "
			+ "ROUND(AVG(t.avg_hours),1) AS avgHours "
			+ "FROM drama d "
			+ "LEFT JOIN (SELECT s.drama_id, AVG(TIMESTAMPDIFF(MINUTE,s.setTime,s.handle_time)/60) AS avg_hours "
			+ "  FROM drama_record_audit_step s WHERE s.handle_time IS NOT NULL GROUP BY s.drama_id) t "
			+ "  ON t.drama_id=d.id "
			+ "WHERE d.filing_category IN (1,2,3) AND IFNULL(d.delete_state,0)=0 "
			+ "GROUP BY d.filing_category")
	List<DramaCategoryAuditStatRespEntity> findCategoryStats();

	/**
	 * 今日紧急待办：五类待审事项合并后按剩余处理时间升序（负数=已超时），取 TOP N。
	 */
	@Select("SELECT 'DRAMA_INFO' AS module, "
			+ "CONCAT(IFNULL(CASE d.filing_category WHEN 1 THEN '一类' WHEN 2 THEN '二类' WHEN 3 THEN '三类' END,''),"
			+ "CASE d.audit_status WHEN 4 THEN '申诉' ELSE '复审' END) AS tagLabel, "
			+ "CONCAT(d.drama_title,' - 信息评审') AS itemTitle, "
			+ "(720 - TIMESTAMPDIFF(MINUTE,d.setTime,NOW())) AS remainMinutes, "
			+ "CONCAT('/audit/assetAudit/basicInfo/',d.id) AS targetPath "
			+ "FROM drama d WHERE d.audit_status IN (0,1,4) AND IFNULL(d.delete_state,0)=0 "

			+ "UNION ALL SELECT 'DRAMA_EPISODE', '剧集待审', "
			+ "CONCAT('第',a.set_num,'集 ',IFNULL(a.video_name,''),' - 剧集评审'), "
			+ "(720 - TIMESTAMPDIFF(MINUTE,s.setTime,NOW())), "
			+ "CONCAT('/audit/assetAudit/episode/',s.drama_id,'/',s.asset_id) "
			+ "FROM drama_asset_audit_step s JOIN drama_asset a ON a.id=s.asset_id "
			+ "WHERE s.status=0 AND IFNULL(a.delete_state,0)=0 "

			+ "UNION ALL SELECT 'DRAMA_RECORD', "
			+ "CONCAT(IFNULL(CASE d.filing_category WHEN 1 THEN '一类' WHEN 2 THEN '二类' WHEN 3 THEN '三类' END,''),"
			+ "CASE d.record_status WHEN 0 THEN '初审' ELSE '复审' END), "
			+ "CONCAT(d.drama_title,' - 备案',CASE d.record_status WHEN 0 THEN '初审' ELSE '复审' END), "
			+ "(1440 - TIMESTAMPDIFF(MINUTE,d.setTime,NOW())), "
			+ "CONCAT('/audit/recordAudit/',d.id) "
			+ "FROM drama d WHERE d.record_status IN (0,1) AND IFNULL(d.delete_state,0)=0 "

			+ "UNION ALL SELECT 'COMMENT', "
			+ "CASE c.risk_level WHEN 3 THEN '高危评论' ELSE '评论待审' END, "
			+ "CONCAT('用户',c.user_id,' - 违规评论'), "
			+ "(1440 - TIMESTAMPDIFF(MINUTE,IFNULL(c.setTime,c.gmtModified),NOW())), "
			+ "'/audit/illegalComments' "
			+ "FROM illegal_comment_record c WHERE c.status=0 "

			+ "UNION ALL SELECT 'SENSITIVE_WORD', '敏感词待审', "
			+ "CONCAT('敏感词“',w.word,'” - 词库审核'), "
			+ "(1440 - TIMESTAMPDIFF(MINUTE,w.setTime,NOW())), "
			+ "'/audit/sensitiveWords' "
			+ "FROM sensitive_word w WHERE w.status=0 "

			+ "ORDER BY remainMinutes ASC LIMIT #{limit}")
	List<AuditUrgentTodoRespEntity> findUrgentTodos(@Param("limit") int limit);
}
