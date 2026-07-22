package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.WatchGiftGlobalConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchGiftGlobalConfigDao extends BaseMapper<WatchGiftGlobalConfigEntity> {

	@Select("select * from watch_gift_global_config where status = 1 order by id asc limit 1")
	WatchGiftGlobalConfigEntity findEnabledOne();

	@Update("<script>update watch_gift_global_config set status = 0, gmtModified = NOW() "
			+ "where status = 1"
			+ "<if test='excludeId != null'> and id &lt;&gt; #{excludeId}</if>"
			+ "</script>")
	int disableOthers(@Param("excludeId") Integer excludeId);
}
