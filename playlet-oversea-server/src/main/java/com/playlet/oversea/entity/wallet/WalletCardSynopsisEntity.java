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
 * U 卡产品简介。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_card_synopsis")
@ApiModel(value = "U卡产品简介", description = "多语言卡简介文案")
public class WalletCardSynopsisEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("language")
	private String language;

	@TableField("title")
	private String title;

	@TableField("content")
	private String content;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;

	@TableField(exist = false)
	@ApiModelProperty("语言名称")
	private String languageName;
}
