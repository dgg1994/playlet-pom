package com.playlet.oversea.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 平台收款地址。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_toll_address")
@ApiModel(value = "收款地址", description = "平台 BNB/BTC/USDT 等收款地址")
public class WalletTollAddressEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("address_type")
	private String addressType;

	@TableField("address_site")
	private String addressSite;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
