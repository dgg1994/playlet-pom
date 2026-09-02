package com.playlet.internal.query.wallet;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端用户持卡列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "用户持卡查询", description = "appUserCard 管理端列表")
public class WalletBankcardAdminQuery extends PageQueryHelperEntity {

	@ApiModelProperty("C 端 uid（app_account.id）")
	private String uid;

	private Integer status;
	private String cardType;
	private String cardNo;
	private String userEmail;
	private String userTel;
	private String userName;
}
