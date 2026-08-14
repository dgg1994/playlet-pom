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
	@ApiModelProperty(name = "videoUrl",value = "视频资源：库内为七牛对象key，播放接口出参为签名URL",required = true,dataType = "String")
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

	@TableField("audit_status")
	@ApiModelProperty(name = "auditStatus", value = "审核状态 0待审 1审核中 2通过 3驳回 4申诉中", required = false, dataType = "Integer")
	private Integer auditStatus;

	@TableField("shelf_status")
	@ApiModelProperty(name = "shelfStatus", value = "上架状态 0未上架 1已上架", required = false, dataType = "Integer")
	private Integer shelfStatus;

	@TableField("audit_reject_reason")
	@ApiModelProperty(name = "auditRejectReason", value = "驳回原因", required = false, dataType = "String")
	private String auditRejectReason;

	@TableField("audit_pass_time")
	@ApiModelProperty(name = "auditPassTime", value = "审核通过时间", required = false, dataType = "Date")
	private Date auditPassTime;

	@TableField("appeal_status")
	@ApiModelProperty(name = "appealStatus", value = "申诉状态 0无 1申诉中 2申诉通过 3申诉驳回", required = false, dataType = "Integer")
	private Integer appealStatus;

	@TableField("appeal_reason")
	@ApiModelProperty(name = "appealReason", value = "申诉理由", required = false, dataType = "String")
	private String appealReason;

	@TableField("appeal_time")
	@ApiModelProperty(name = "appealTime", value = "申诉时间", required = false, dataType = "Date")
	private Date appealTime;

	@TableField("shelf_time")
	@ApiModelProperty(name = "shelfTime", value = "上架时间", required = false, dataType = "Date")
	private Date shelfTime;

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

	@TableField(exist = false)
	@ApiModelProperty(name = "isLike",value = "是否点赞 1是0否",required = true,dataType = "String")
	private Integer isLike;

	@TableField(exist = false)
	@ApiModelProperty(name = "isCollect",value = "是否收藏 1是0否",required = true,dataType = "String")
	private Integer isCollect;

}
