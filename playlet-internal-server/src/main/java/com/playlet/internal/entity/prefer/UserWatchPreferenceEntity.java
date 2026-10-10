package com.playlet.internal.entity.prefer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户看剧喜好选择。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_watch_preference")
@ApiModel(value = "用户看剧喜好", description = "用户已选看剧喜好选项")
public class UserWatchPreferenceEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID，对应 app_account.id", dataType = "Integer")
	private Integer uid;

	@TableField("category_code")
	@ApiModelProperty(name = "categoryCode", value = "分类编码（冗余，便于按区块查询）", dataType = "String")
	private String categoryCode;

	@TableField("prefer_group_id")
	@ApiModelProperty(name = "preferGroupId", value = "偏好选项 group_id", dataType = "String")
	private String preferGroupId;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "用户选择顺序，越大越靠前", dataType = "Integer")
	private Integer sortWeight;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "首次写入时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "最近更新时间", dataType = "Date")
	private Date gmtModified;
}
