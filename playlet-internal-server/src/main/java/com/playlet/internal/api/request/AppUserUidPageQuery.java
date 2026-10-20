package com.playlet.internal.api.request;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端按用户分页查询（充值/观看等子列表）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "用户子列表查询", description = "uid + 分页")
public class AppUserUidPageQuery extends PageQueryHelperEntity {

	@ApiModelProperty(value = "用户ID", required = true)
	private Integer uid;
}
