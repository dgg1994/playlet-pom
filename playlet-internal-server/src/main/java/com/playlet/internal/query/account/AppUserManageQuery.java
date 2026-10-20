package com.playlet.internal.query.account;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端 App 用户列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "App用户管理查询", description = "关键词/会员状态/账号状态/来源筛选")
public class AppUserManageQuery extends PageQueryHelperEntity {

	@ApiModelProperty("用户ID / 手机号 / 昵称")
	private String keyword;

	@ApiModelProperty("会员状态：0普通 1有效 2已过期；空=全部")
	private Integer memberStatus;

	@ApiModelProperty("账号状态：对齐 UserStateEnums 1正常 2禁用(已封) 0注销；空=全部")
	private Integer userState;

	@ApiModelProperty("注册来源 register_source：1一键登录 2账号注册；空=全部")
	private Integer registerSource;
}
