package com.playlet.internal.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * U 卡产品与简介关联。
 */
@Data
@TableName("wallet_card_synopsis_join")
@ApiModel(value = "U卡简介关联", description = "卡产品 uuid 与简介 id 多对多")
public class WalletCardSynopsisJoinEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("language")
	private String language;

	@TableField("card_id")
	private String cardId;

	@TableField("synopsis_id")
	private Integer synopsisId;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
