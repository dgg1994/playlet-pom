package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端创作者用户列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "创作者用户管理查询", description = "邮箱/昵称/手机号模糊、账号状态、入驻审核状态")
public class CreatorAccountManageQuery extends PageQueryHelperEntity {

	@ApiModelProperty(value = "关键词：登录邮箱/昵称/手机号")
	private String keyword;

	@ApiModelProperty(value = "账号状态：0注销 1正常 2冻结")
	private Integer userState;

	@ApiModelProperty(value = "入驻审核：0待审 1审核中 2通过 3驳回")
	private Integer auditStatus;
}
