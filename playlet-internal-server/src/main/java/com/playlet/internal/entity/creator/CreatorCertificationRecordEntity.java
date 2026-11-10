package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 创作者认证审核记录（认证管理页时间线）。
 */
@Data
@TableName("creator_certification_record")
@ApiModel(value = "创作者认证审核记录")
public class CreatorCertificationRecordEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "creator_account.id")
	private Integer creatorId;

	@TableField("category")
	@ApiModelProperty(name = "category", value = "类别 1个人创作者 2机构创作者")
	private Integer category;

	@TableField("title")
	@ApiModelProperty(name = "title", value = "事件描述")
	private String title;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1审核中 2已通过 3已驳回 4已处理")
	private Integer status;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
