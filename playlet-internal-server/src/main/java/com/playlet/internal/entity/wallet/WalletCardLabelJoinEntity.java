package com.playlet.internal.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * U 卡产品与标签关联（对齐 onetoken card_lable_join）。
 */
@Data
@TableName("wallet_card_label_join")
@ApiModel(value = "U卡标签关联", description = "卡产品 uuid 与标签 id 关联")
public class WalletCardLabelJoinEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("主键")
	private Integer id;

	@TableField("language")
	@ApiModelProperty("语言分类")
	private String language;

	@TableField("card_id")
	@ApiModelProperty("卡产品 id（wallet_card_product.id 字符串）")
	private String cardId;

	@TableField("label_id")
	@ApiModelProperty("wallet_card_label.id")
	private Integer labelId;

	@TableField("setTime")
	@ApiModelProperty("创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty("更新时间")
	private Date gmtModified;
}
