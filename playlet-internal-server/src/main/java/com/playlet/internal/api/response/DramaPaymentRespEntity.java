package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端短剧付费列表项。
 */
@Data
@ApiModel(value = "短剧付费项", description = "按短剧维度的付费收入汇总")
public class DramaPaymentRespEntity {

	@ApiModelProperty("短剧ID")
	private String dramaId;

	@ApiModelProperty("短剧名称")
	private String dramaName;

	@ApiModelProperty("分类（标签，顿号拼接）")
	private String dramaTag;

	@ApiModelProperty("总集数")
	private Integer dramaSum;

	@ApiModelProperty("付费集数（unlock_type=2）")
	private Integer dramaPaySum;

	@ApiModelProperty("累计收入(元，金币/现金解锁)")
	private BigDecimal dramaTotalRevenue;

	@ApiModelProperty("购买人次")
	private Integer payPeopleSum;

	@ApiModelProperty("最近付费时间")
	private Date payTime;
}
