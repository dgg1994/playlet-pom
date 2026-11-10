package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 创作者统计（综合评分 / 作品数 / 粉丝数）。
 */
@Data
@TableName("creator_stat")
@ApiModel(value = "创作者统计")
public class CreatorStatEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("creator_id")
	private Integer creatorId;

	/** 综合评分 0-10，一位小数 */
	@TableField("score")
	private BigDecimal score;

	@TableField("work_count")
	private Integer workCount;

	@TableField("follower_count")
	private Integer followerCount;

	@TableField("total_view")
	private Long totalView;

	@TableField("stat_date")
	private Date statDate;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
