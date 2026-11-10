package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorCertificationRecordEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 创作者认证审核记录。
 */
@Repository
public interface CreatorCertificationRecordDao extends BaseMapper<CreatorCertificationRecordEntity> {

	@Select("select * from creator_certification_record where creator_id = #{creatorId} order by setTime desc, id desc limit 50")
	List<CreatorCertificationRecordEntity> findByCreatorId(@Param("creatorId") Integer creatorId);

	/** 同类别旧的「审核中」记录置为「已处理」（重新提交时调用） */
	@Update("update creator_certification_record set status = 4 where creator_id = #{creatorId} and category = #{category} and status = 1")
	int closeReviewing(@Param("creatorId") Integer creatorId, @Param("category") Integer category);
}
