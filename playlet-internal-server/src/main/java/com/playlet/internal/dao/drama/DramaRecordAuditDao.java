package com.playlet.internal.dao.drama;

import com.playlet.internal.api.response.DramaRecordAuditListRespEntity;
import com.playlet.internal.query.drama.DramaRecordAuditQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 备案审核专用查询。
 */
public interface DramaRecordAuditDao {

	/**
	 * 统计已生成的备案号数量，用于生成当日流水号。
	 */
	@Select("SELECT COUNT(*) FROM drama WHERE record_no LIKE CONCAT(#{prefix}, '%')")
	int countRecordNoByPrefix(@Param("prefix") String prefix);

	@Select("<script>" +
		"SELECT d.id AS dramaId, d.drama_title AS dramaTitle, d.cover_url AS coverUrl, " +
		"d.producer_firm AS producerFirm, d.filing_category AS filingCategory, " +
		"d.total_episodes AS totalEpisodes, d.record_status AS recordStatus, " +
		"d.record_no AS recordNo, d.setTime AS uploadDate " +
		"FROM drama d " +
		"WHERE d.delete_state = 0 " +
		"<if test='keyword != null and keyword != \"\"'>" +
		"  AND (d.drama_title LIKE CONCAT('%', #{keyword}, '%') OR d.producer_firm LIKE CONCAT('%', #{keyword}, '%'))" +
		"</if>" +
		"<if test='recordStatus != null'>" +
		"  AND d.record_status = #{recordStatus}" +
		"</if>" +
		"<if test='filingCategory != null'>" +
		"  AND d.filing_category = #{filingCategory}" +
		"</if>" +
		"ORDER BY d.setTime DESC" +
		"</script>")
	List<DramaRecordAuditListRespEntity> findRecordList(@Param("keyword") String keyword,
			@Param("recordStatus") Integer recordStatus,
			@Param("filingCategory") Integer filingCategory);
}
