package com.playlet.oversea.query.welfare;

import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端提现订单列表查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "提现订单管理查询", description = "财务管理-用户/作家提现记录筛选")
public class WithdrawOrderAdminQuery extends PageQueryHelperEntity {

	@ApiModelProperty(value = "业务单号")
	private String orderNo;

	@ApiModelProperty(value = "用户uid / 作家id")
	private Integer uid;

	@ApiModelProperty(value = "昵称（模糊）")
	private String nickname;

	@ApiModelProperty(value = "订单状态：0待处理 1打款中 2成功 3失败 4已退回；与 processFlag 互斥优先")
	private Integer status;

	@ApiModelProperty(value = "处理态：0未处理(status in 0,1) 1已处理(status in 2,3,4)；对应页签")
	private Integer processFlag;

	@ApiModelProperty(value = "资产编码")
	private String assetCode;

	@ApiModelProperty(value = "网络")
	private String network;
}
