package com.playlet.internal.entity.security;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 类描述：敏感词库
 *
 * @author GeminiSun
 * @date 2026/08/11 17:41
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sensitive_word")
@ApiModel(value = "敏感词实体", description = "评论内容敏感词库")
public class SensitiveWordEntity extends PageQueryHelperEntity {


    @TableId(type = IdType.AUTO)
    @ApiModelProperty(name = "id", value = "主键ID", dataType = "Long")
    private Long id;


    @TableField("word")
    @ApiModelProperty(name = "word", value = "敏感词内容", dataType = "String")
    private String word;


    @TableField("category")
    @ApiModelProperty(name = "category", value = "敏感词分类：色情/广告/辱骂/诈骗等", dataType = "String")
    private String category;


    @TableField("level")
    @ApiModelProperty(name = "level", value = "敏感等级：1警告 2审核 3禁止", dataType = "Integer")
    private Integer level;


    @TableField("status")
    @ApiModelProperty(name = "status", value = "状态：1启用 0禁用", dataType = "Integer")
    private Integer status;


    @TableField("setTime")
    @ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
    private Date setTime;


    @TableField("gmtModified")
    @ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
    private Date gmtModified;

}