package com.playlet.internal.entity.drama;

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

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drama_asset")
@ApiModel(value = "剧资源",description = "封面海报等")
public class DramaAssetEntity extends PageQueryHelperEntity {
	
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId",value = "剧ID",required = true,dataType = "String")
	private Integer dramaId;
	
	@TableField("video_name")
	@ApiModelProperty(name = "videoName",value = "视频名",required = true,dataType = "String")
	private String videoName;
	
	@TableField("set_num")
	@ApiModelProperty(name = "setNum",value = "第几集",required = true,dataType = "Integer")
	private Integer setNum;
	
	@TableField("collect_score")
	@ApiModelProperty(name = "collectScore",value = "收藏量",required = false,dataType = "Long")
	private Long collectScore;
	
	@TableField("like_score")
	@ApiModelProperty(name = "likeScore",value = "点赞量",required = false,dataType = "Long")
	private Long likeScore;
	
	@TableField("share_score")
	@ApiModelProperty(name = "shareScore",value = "分享量",required = false,dataType = "Long")
	private Long shareScore;
	
	@TableField("discuss_score")
	@ApiModelProperty(name = "discussScore",value = "评论量",required = false,dataType = "Long")
	private Long discussScore;
	
	@TableField("video_url")
	@ApiModelProperty(name = "videoUrl",value = "视频资源URL",required = true,dataType = "String")
	private String videoUrl;
	
	@TableField("video_type")
	@ApiModelProperty(name = "videoType",value = "视频类型 1横屏 2竖屏",required = false,dataType = "Integer")
	private Integer videoType;
	
	@TableField("video_width")
	@ApiModelProperty(name = "videoWidth",value = "宽",required = false,dataType = "Integer")
	private Integer videoWidth;
	
	@TableField("video_height")
	@ApiModelProperty(name = "videoHeight",value = "高",required = false,dataType = "Integer")
	private Integer videoHeight;
	
	@TableField("video_status")
	@ApiModelProperty(name = "videoStatus",value = "可用状态 1是0否",required = false,dataType = "Integer")
	private Integer videoStatus;

	@TableField("delete_state")
	@ApiModelProperty(name = "deleteState",value = "删除状态 1是0否",required = false,dataType = "Integer")
	private Integer deleteState;

	@TableField("remark_info")
	@ApiModelProperty(name = "remarkInfo",value = "备注",required = false,dataType = "String")
	private String remarkInfo;

	@TableField("belong_user")
	@ApiModelProperty(name = "belongUser",value = "所属人",required = false,dataType = "String")
	private Integer belongUser;
	
	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "创建时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;

}
