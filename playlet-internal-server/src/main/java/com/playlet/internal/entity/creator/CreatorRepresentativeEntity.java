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
 * 创作者代表作（产品管理：过往短剧作品）。
 */
@Data
@TableName("creator_representative")
@ApiModel(value = "创作者代表作")
public class CreatorRepresentativeEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "creator_account.id")
	private Integer creatorId;

	@TableField("drama_name")
	@ApiModelProperty(name = "dramaName", value = "短剧名称")
	private String dramaName;

	@TableField("category")
	@ApiModelProperty(name = "category", value = "备案分类：一类短剧/二类短剧/三类短剧")
	private String category;

	@TableField("cover")
	@ApiModelProperty(name = "cover", value = "封面图片七牛key（选填）")
	private String cover;

	@TableField("work_link")
	@ApiModelProperty(name = "workLink", value = "作品链接（必填）")
	private String workLink;

	@TableField("intro")
	@ApiModelProperty(name = "intro", value = "简介")
	private String intro;

	@TableField("display_status")
	@ApiModelProperty(name = "displayStatus", value = "0未上架 1已上架")
	private Integer displayStatus;

	@TableField("audit_status")
	@ApiModelProperty(name = "auditStatus", value = "0待审 1审核中 2通过 3驳回")
	private Integer auditStatus;

	@TableField("audit_remark")
	@ApiModelProperty(name = "auditRemark", value = "审核备注")
	private String auditRemark;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
