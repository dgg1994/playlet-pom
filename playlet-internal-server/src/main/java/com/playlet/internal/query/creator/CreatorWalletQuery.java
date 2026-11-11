package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 钱包明细查询：收支明细用 type，提现记录用 status，二者共用分页参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "钱包明细查询")
public class CreatorWalletQuery extends PageQueryHelperEntity {

	@ApiModelProperty(value = "收支分类：全部/收入/支出/分账/提现")
	private String type;

	@ApiModelProperty(value = "提现状态：全部/处理中/已到账/已失败")
	private String status;
}
