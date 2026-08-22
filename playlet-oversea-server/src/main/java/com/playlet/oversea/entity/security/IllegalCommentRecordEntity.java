package com.playlet.oversea.entity.security;

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
 * 类描述：违规评论记录
 *
 * @author GeminiSun
 * @date 2026/08/12 10:02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("illegal_comment_record")
@ApiModel(value = "违规评论记录",description = "评论内容安全审核记录，包括敏感词命中、人工处理、用户处罚等")
public class IllegalCommentRecordEntity extends PageQueryHelperEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(name = "id",value = "主键ID",dataType = "Long")
    private Long id;

    /**
     * 评论ID
     */
    @TableField("comment_id")
    @ApiModelProperty(name = "commentId",value = "违规评论ID",dataType = "Long")
    private Integer commentId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @ApiModelProperty(name = "userId",value = "发布用户ID",dataType = "Long")
    private Integer userId;

    /**
     * 短剧ID
     */
    @TableField("drama_id")
    @ApiModelProperty(name = "dramaId",value = "短剧ID",dataType = "Long")
    private Integer dramaId;

    /**
     * 剧集ID
     */
    @TableField("episode_id")
    @ApiModelProperty(name = "episodeId",value = "剧集ID",dataType = "Long")
    private Integer episodeId;

    /**
     * 原评论内容
     */
    @TableField("content")
    @ApiModelProperty(name = "content",value = "违规评论内容",dataType = "String")
    private String content;

    /**
     * 命中的敏感词
     */
    @TableField("sensitive_words")
    @ApiModelProperty(name = "sensitiveWords",value = "命中的敏感词，多个逗号分隔",dataType = "String")
    private String sensitiveWords;

    /**
     * 风险等级
     */
    @TableField("risk_level")
    @ApiModelProperty(name = "riskLevel",value = "风险等级：1低风险 2中风险 3高风险",dataType = "Integer")
    private Integer riskLevel;

    /**
     * 处理状态
     */
    @TableField("status")
    @ApiModelProperty(name = "status",value = "状态：0待处理 1已通过 2已删除 3禁言用户 4冻结账户",dataType = "Integer")
    private Integer status;

    /**
     * 处理类型
     */
    @TableField("handle_type")
    @ApiModelProperty(name = "handleType",value = "处理类型：1忽略 2删除评论 3禁言用户 4冻结账户",dataType = "Integer")
    private Integer handleType;

    /**
     * 处理管理员
     */
    @TableField("handler_id")
    @ApiModelProperty(name = "handlerId",value = "处理管理员ID",dataType = "Long")
    private Long handlerId;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    @ApiModelProperty(name = "handleRemark",value = "处理备注",dataType = "String")
    private String handleRemark;

    /**
     * 创建时间
     */
    @TableField("setTime")
    @ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
    private Date setTime;

    /**
     * 更新时间
     */
    @TableField("gmtModified")
    @ApiModelProperty(name = "gmtModified", value = "更新时间",dataType = "Date")
    private Date gmtModified;

    /**
     * 来源类型
     */
    @TableField("source_type")
    @ApiModelProperty(name = "sourceType",value = "来源类型：1敏感词 2AI审核 3用户举报",dataType = "Integer")
    private Integer sourceType;

}