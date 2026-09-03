package com.playlet.oversea.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 钱包内部转账通讯录。
 */
@Data
@TableName("wallet_transfet_contacts")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "钱包内部转账通讯录", description = "常用转账联系人")
public class WalletTransfetContactsEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "本人 wallet_uid")
	private Long walletUid;

	@TableField("contacts_wallet_uid")
	@ApiModelProperty(name = "contactsWalletUid", value = "联系人 wallet_uid")
	private Long contactsWalletUid;

	@TableField("contacts_label")
	@ApiModelProperty(name = "contactsLabel", value = "联系人标签")
	private String contactsLabel;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;

	/** 兼容 onetoken：uid */
	@TableField(exist = false)
	@ApiModelProperty(name = "uid", value = "本人 uid（兼容）")
	private Long uid;

	/** 兼容 onetoken：contactsUid */
	@TableField(exist = false)
	@ApiModelProperty(name = "contactsUid", value = "联系人 uid（兼容）")
	private Long contactsUid;
}
