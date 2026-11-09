package com.playlet.internal.entity.drama;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.api.response.DramaAssetRespEntity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drama")
@ApiModel(value = "短剧",description = "短剧")
public class DramaEntity extends PageQueryHelperEntity {
	
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;

	@TableField("drama_title")
	@ApiModelProperty(name = "dramaTitle",value = "短剧标题",required = true,dataType = "String")
	private String dramaTitle;
	
	@TableField("producer_firm")
	@ApiModelProperty(name = "producerFirm",value = "出品方",required = true,dataType = "String")
	private String producerFirm;
	
	@TableField("cover_url")
	@ApiModelProperty(name = "coverUrl",value = "封面：库内为七牛对象key，接口出参为可访问URL",required = false,dataType = "String")
	private String coverUrl;
	
	@TableField("score_num")
	@ApiModelProperty(name = "scoreNum",value = "评分",required = false,dataType = "Long")
	private Double scoreNum;
	
	@TableField("hot_score")
	@ApiModelProperty(name = "hotScore",value = "热度值",required = false,dataType = "Long")
	private Long hotScore;
	
	@TableField("collect_score")
	@ApiModelProperty(name = "collectScore",value = "收藏量",required = false,dataType = "Long")
	private Long collectScore;
	
	@TableField("share_score")
	@ApiModelProperty(name = "shareScore",value = "分享量",required = false,dataType = "Long")
	private Long shareScore;
	
	@TableField("like_score")
	@ApiModelProperty(name = "likeScore",value = "点赞量",required = false,dataType = "Long")
	private Long likeScore;
	
	@TableField("discuss_score")
	@ApiModelProperty(name = "discussScore",value = "评论量",required = false,dataType = "Long")
	private Long discussScore;
	
	@TableField("hot_score_text")
	@ApiModelProperty(name = "hotScoreText",value = "热度文案",required = false,dataType = "String")
	private String hotScoreText;
	
	@TableField("total_episodes")
	@ApiModelProperty(name = "totalEpisodes",value = "总集数",required = false,dataType = "Integer")
	private Integer totalEpisodes;
	
	@TableField("finished_state")
	@ApiModelProperty(name = "finishedState",value = "是否完结（1是0否）",required = false,dataType = "Integer")
	private Integer finishedState;
	
	@TableField("video_type")
	@ApiModelProperty(name = "videoType",value = "视频类型 1横屏 2竖屏",required = false,dataType = "Integer")
	private Integer videoType;
	
	@TableField("video_width")
	@ApiModelProperty(name = "videoWidth",value = "宽",required = false,dataType = "Integer")
	private Integer videoWidth;
	
	@TableField("video_height")
	@ApiModelProperty(name = "videoHeight",value = "高",required = false,dataType = "Integer")
	private Integer videoHeight;
	
	@TableField("description_info")
	@ApiModelProperty(name = "descriptionInfo",value = "简介",required = false,dataType = "String")
	private String descriptionInfo;
	
	@TableField("verify_status")
	@ApiModelProperty(name = "verifyStatus",value = "是否上架 1是0否 0下架1上架",required = false,dataType = "Integer")
	private Integer verifyStatus;

	@TableField("audit_status")
	@ApiModelProperty(name = "auditStatus", value = "剧评审状态 0待审 1审核中 2通过 3驳回 4申诉中", required = false, dataType = "Integer")
	private Integer auditStatus;

	@TableField("shelf_status")
	@ApiModelProperty(name = "shelfStatus", value = "剧上架状态 0未上架 1已上架", required = false, dataType = "Integer")
	private Integer shelfStatus;

	@TableField("audit_reject_reason")
	@ApiModelProperty(name = "auditRejectReason", value = "剧评审驳回原因", required = false, dataType = "String")
	private String auditRejectReason;

	@TableField("audit_pass_time")
	@ApiModelProperty(name = "auditPassTime", value = "剧评审通过时间", required = false, dataType = "Date")
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
	@ApiModelProperty(name = "shelfTime", value = "剧上架时间", required = false, dataType = "Date")
	private Date shelfTime;

	@TableField("delete_state")
	@ApiModelProperty(name = "deleteState",value = "1是0否",required = false,dataType = "Integer")
	private Integer deleteState;

	@TableField("recommended_carousel")
	@ApiModelProperty(name = "recommendedCarousel",value = "是否推荐轮播 0-否 1-是",required = false,dataType = "Boolean")
	private Integer recommendedCarousel = 0;

	@TableField("offline_reason")
	@ApiModelProperty(name = "offlineReason",value = "下架原因",required = false,dataType = "String")
	private String offlineReason;

	@TableField("remark_info")
	@ApiModelProperty(name = "remarkInfo",value = "运营备注",required = false,dataType = "String")
	private String remarkInfo;

	@TableField("belong_user")
	@ApiModelProperty(name = "belongUser",value = "所属人",required = false,dataType = "String")
	private Integer belongUser;
	
	@TableField("is_ai")
	@ApiModelProperty(name = "isAi",value = "是否ai生成 1是0否",required = false,dataType = "Integer")
	private Integer isAi;

	@TableField("is_member_drama")
	@ApiModelProperty(name = "isMemberDrama", value = "是否会员短剧 0否 1是", required = false, dataType = "Integer")
	private Integer isMemberDrama;

	@TableField("record_status")
	@ApiModelProperty(name = "recordStatus", value = "备案状态 0待初审 1初审通过待复审 2备案通过 3初审驳回 4复审驳回 5已上架", required = false, dataType = "Integer")
	private Integer recordStatus;

	@TableField("record_no")
	@ApiModelProperty(name = "recordNo", value = "备案号（通过后自动生成）", required = false, dataType = "String")
	private String recordNo;

	@TableField("filing_category")
	@ApiModelProperty(name = "filingCategory", value = "备案分类 1一类 2二类 3三类", required = false, dataType = "Integer")
	private Integer filingCategory;

	@TableField("record_type")
	@ApiModelProperty(name = "recordType", value = "备案类型 官方备案/平台备案", required = false, dataType = "String")
	private String recordType;

	@TableField("investment_amount")
	@ApiModelProperty(name = "investmentAmount", value = "投资额（元）", required = false, dataType = "Long")
	private BigDecimal investmentAmount;

	@TableField("theme_partition")
	@ApiModelProperty(name = "themePartition", value = "题材分区：女频/男频", required = false, dataType = "String")
	private String themePartition;

	@TableField("drama_type")
	@ApiModelProperty(name = "dramaType", value = "剧类型 1真人短剧 2AI漫剧", required = false, dataType = "Integer")
	private Integer dramaType;

	@TableField("special_theme")
	@ApiModelProperty(name = "specialTheme", value = "特殊题材，逗号分隔：公安/司法/军事/民族/宗教", required = false, dataType = "String")
	private String specialTheme;

	@TableField("other_characters")
	@ApiModelProperty(name = "otherCharacters", value = "其他人物简介", required = false, dataType = "String")
	private String otherCharacters;

	@TableField("project_share_ratio")
	@ApiModelProperty(name = "projectShareRatio", value = "关联项目分账比例（%）", required = false, dataType = "BigDecimal")
	private BigDecimal projectShareRatio;

	@TableField("default_unlock_method")
	@ApiModelProperty(name = "defaultUnlockMethod",value = "默认解锁方式 free/vip/paid（创建向导）",required = false,dataType = "String")
	private String defaultUnlockMethod;

	@TableField("unlock_price")
	@ApiModelProperty(name = "unlockPrice",value = "剧集解锁金额（元），付费解锁时按集计费",required = false,dataType = "BigDecimal")
	private BigDecimal unlockPrice;

	@TableField("story_synopsis")
	@ApiModelProperty(name = "storySynopsis",value = "故事梗概",required = false,dataType = "String")
	private String storySynopsis;

	@TableField("episode_outline")
	@ApiModelProperty(name = "episodeOutline",value = "分集大纲JSON数组字符串 [{range,text}]",required = false,dataType = "String")
	private String episodeOutline;

	@TableField("female_lead")
	@ApiModelProperty(name = "femaleLead",value = "女主角介绍",required = false,dataType = "String")
	private String femaleLead;

	@TableField("male_lead")
	@ApiModelProperty(name = "maleLead",value = "男主角介绍",required = false,dataType = "String")
	private String maleLead;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "创建时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;
	
	@TableField(exist = false)
	@ApiModelProperty(name = "materialList",value = "备案材料列表",required = false,dataType = "List<DramaRecordMaterialEntity>")
	private List<DramaRecordMaterialEntity> materialList;

	@TableField(exist = false)
	@ApiModelProperty(name = "tagList",value = "标签类型集合",required = false,dataType = "List<Integer>")
	private List<TagEntity> tagList;
	
	@TableField(exist = false)
	@ApiModelProperty(name = "uploadSetNum",value = "已上传集数",required = false,dataType = "Integer")
	private Integer uploadSetNum;

	@TableField(exist = false)
	@ApiModelProperty(name = "tagId",value = "标签主键id（会解析为 groupId 再检索）",required = false,dataType = "Integer")
	private Integer tagId;

	@TableField(exist = false)
	@ApiModelProperty(name = "tagGroupId",value = "标签分组id（drama_tag_rel.tag_group_id）",required = false,dataType = "String")
	private String tagGroupId;

	@TableField(exist = false)
	@ApiModelProperty(name = "voideList",value = "已上传视频",required = false,dataType = "List<DramaAssetRespEntity>")
	private List<DramaAssetRespEntity> voideList;

	@TableField(exist = false)
	@ApiModelProperty(name = "dramaSteps", value = "剧评审步骤：AI / 初审 / 复审", required = false)
	private List<DramaAuditStepEntity> dramaSteps;

	/** 当前用户是否最近观看过该剧：1是0否（剧场首页轮播等展示用，非表字段） */
	@TableField(exist = false)
	@ApiModelProperty(name = "recentWatched", value = "是否最近观看 1是0否", required = false, dataType = "Integer")
	private Integer recentWatched;

	@TableField(exist = false)
	@ApiModelProperty(name = "isHot", value = "隶属哪个榜单", required = false, dataType = "Integer")
	private String isHot;

}
