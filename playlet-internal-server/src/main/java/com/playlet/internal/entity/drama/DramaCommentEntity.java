package com.playlet.internal.entity.drama;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("drama_comment")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "短剧评论",description = "短剧评论")
public class DramaCommentEntity extends PageQueryHelperEntity{
	
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;
	
	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId",value = "剧ID",required = true,dataType = "Integer")
	private Integer dramaId;
	
	@TableField("video_id")
	@ApiModelProperty(name = "videoId",value = "视频id",required = true,dataType = "Integer")
	private Integer videoId;
	
	@TableField("user_id")
	@ApiModelProperty(name = "userId",value = "评论用户ID",required = true,dataType = "Integer")
	private Integer userId;
	
	@TableField("user_name")
	@ApiModelProperty(name = "userName",value = "评论用户名",required = true,dataType = "String")
	private String userName;

	@TableField("comment_info")
	@ApiModelProperty(name = "commentInfo",value = "评论内容",required = true,dataType = "String")
	private String commentInfo;
	
	@TableField("like_count")
	@ApiModelProperty(name = "likeCount",value = "点赞数",required = true,dataType = "Integer")
	private Integer likeCount;
	
	@TableField("reply_count")
	@ApiModelProperty(name = "replyCount",value = "回复总数",required = true,dataType = "Integer")
	private Integer replyCount;
	
	@TableField("parent_id")
	@ApiModelProperty(name = "parentId",value = "父评论ID（0表示一级评论）",required = true,dataType = "Integer")
	private Integer parentId;
	
	@TableField("reply_to_user_id")
	@ApiModelProperty(name = "replyToUserId",value = "回复目标用户ID",required = true,dataType = "Integer")
	private Integer replyToUserId;
	
	@TableField("reply_to_user_name")
	@ApiModelProperty(name = "replyToUserName",value = "回复目标用户昵称",required = true,dataType = "String")
	private String replyToUserName;
	
	@TableField("delete_state")
	@ApiModelProperty(name = "deleteState",value = "删除状态 1是0否",required = true,dataType = "String")
	private Integer deleteState;
	
	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "创建时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;

	
}
