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
@TableName("drama_comment_like")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "短剧评论点赞记录",description = "短剧评论点赞记录")
public class DramaCommentLikeEntity extends PageQueryHelperEntity{

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;
	
	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId",value = "剧ID",required = true,dataType = "Integer")
	private Integer dramaId;
	
	@TableField("video_id")
	@ApiModelProperty(name = "videoId",value = "视频id",required = true,dataType = "Integer")
	private Integer videoId;
	
	@TableField("comment_id")
	@ApiModelProperty(name = "commentId",value = "评论id",required = true,dataType = "Integer")
	private Integer commentId;
	
	@TableField("user_id")
	@ApiModelProperty(name = "userId",value = "点赞用户ID",required = true,dataType = "Integer")
	private Integer userId;
	
	@TableField("like_type")
	@ApiModelProperty(name = "likeType",value = "点赞类型 1评论 2回复",required = true,dataType = "Integer")
	private Integer likeType;
	
	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "创建时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;
	
}
