package com.playlet.internal.dao.security;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.security.SensitiveWordEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensitiveWordDao extends BaseMapper<SensitiveWordEntity> {

	@Select("<script>"
			+ "select * from sensitive_word where 1=1 "
			+ "<if test='word != null and word != \"\"'> and word like concat('%', #{word}, '%') </if>"
			+ "order by id desc"
			+ "</script>")
	List<SensitiveWordEntity> findAdminList(SensitiveWordEntity entity);

	@Select("select * from sensitive_word where word = #{word} limit 1")
	SensitiveWordEntity findByWord(@Param("word") String word);
}
