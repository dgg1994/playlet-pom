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
 * U 卡标签字典（对齐 onetoken card_lable）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_card_label")
@ApiModel(value = "U卡标签", description = "卡产品可选标签")
public class WalletCardLabelEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("主键")
	private Integer id;

	@TableField("language")
	@ApiModelProperty(value = "语言分类", required = true)
	private String language;

	@TableField("name")
	@ApiModelProperty(value = "标签名称", required = true)
	private String name;

	@TableField("setTime")
	@ApiModelProperty("创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty("更新时间")
	private Date gmtModified;

	@TableField(exist = false)
	@ApiModelProperty("语言分类文案")
	private String languageName;
}
