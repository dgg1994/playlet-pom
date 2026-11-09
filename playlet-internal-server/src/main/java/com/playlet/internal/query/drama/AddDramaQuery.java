package com.playlet.internal.query.drama;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddDramaQuery {
	
	@NotBlank(message = "短剧标题不能为空")
	@ApiModelProperty(name = "dramaTitle",value = "短剧标题",required = true,dataType = "String")
	private String dramaTitle;
	
	@ApiModelProperty(name = "producerFirm",value = "出品方",required = true,dataType = "String")
	private String producerFirm;
	
	@NotNull(message = "总集数不能为空")
	@ApiModelProperty(name = "totalEpisodes",value = "总集数",required = false,dataType = "Integer")
	private Integer totalEpisodes;
	
	@NotNull(message = "是否完结不能为空")
	@ApiModelProperty(name = "finishedState",value = "是否完结（1是0否）",required = false,dataType = "Integer")
	private Integer finishedState;
	
	@NotNull(message = "视频类型不能为空")
	@ApiModelProperty(name = "videoType",value = "视频类型 1横屏 2竖屏",required = false,dataType = "Integer")
	private Integer videoType;
	
	@NotBlank(message = "简介不能为空")
	@ApiModelProperty(name = "descriptionInfo",value = "简介",required = false,dataType = "String")
	private String descriptionInfo;
	
	@NotNull(message = "所属人不能为空")
	@ApiModelProperty(name = "belongUser",value = "所属人",required = false,dataType = "String")
	private Integer belongUser;
	
	@NotNull(message = "是否ai生成不能为空")
	@ApiModelProperty(name = "isAi",value = "是否ai生成 1是0否",required = false,dataType = "Integer")
	private Integer isAi;
	
	@NotNull(message = "标签不能为空")
	@ApiModelProperty(name = "tagGroupIdList",value = "标签语言分组Id类型集合",required = false,dataType = "List<Integer>")
	private List<String> tagGroupIdList;

	@ApiModelProperty(name = "coverUrl",value = "封面七牛对象key（创作者端直传后回传；管理端仍用 file 上传）",required = false,dataType = "String")
	private String coverUrl;

	@ApiModelProperty(name = "filingCategory",value = "备案分类 1一类 2二类 3三类",required = false,dataType = "Integer")
	private Integer filingCategory;

	@ApiModelProperty(name = "investmentAmount",value = "投资额（元）",required = false,dataType = "BigDecimal")
	private java.math.BigDecimal investmentAmount;

	@ApiModelProperty(name = "dramaType",value = "剧类型 1真人短剧 2AI漫剧",required = false,dataType = "Integer")
	private Integer dramaType;

	@ApiModelProperty(name = "specialTheme",value = "特殊题材，逗号分隔：公安/司法/军事/民族/宗教",required = false,dataType = "String")
	private String specialTheme;

	@ApiModelProperty(name = "themePartition",value = "题材分区：女频/男频",required = false,dataType = "String")
	private String themePartition;

	@ApiModelProperty(name = "otherCharacters",value = "其他人物简介",required = false,dataType = "String")
	private String otherCharacters;

	@ApiModelProperty(name = "projectShareRatio",value = "关联项目分账比例（%）",required = false,dataType = "BigDecimal")
	private BigDecimal projectShareRatio;

	@ApiModelProperty(name = "defaultUnlockMethod",value = "默认解锁方式 free/vip/paid",required = false,dataType = "String")
	private String defaultUnlockMethod;

	@ApiModelProperty(name = "unlockPrice",value = "剧集解锁金额（元）",required = false,dataType = "BigDecimal")
	private BigDecimal unlockPrice;

	@ApiModelProperty(name = "storySynopsis",value = "故事梗概",required = false,dataType = "String")
	private String storySynopsis;

	@ApiModelProperty(name = "episodeOutline",value = "分集大纲JSON数组字符串 [{range,text}]",required = false,dataType = "String")
	private String episodeOutline;

	@ApiModelProperty(name = "femaleLead",value = "女主角介绍",required = false,dataType = "String")
	private String femaleLead;

	@ApiModelProperty(name = "maleLead",value = "男主角介绍",required = false,dataType = "String")
	private String maleLead;

	@ApiModelProperty(name = "materials",value = "备案材料列表",required = false,dataType = "List<DramaMaterialItemQuery>")
	private List<DramaMaterialItemQuery> materials;

}
