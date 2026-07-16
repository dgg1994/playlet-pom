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
	@ApiModelProperty(name = "dramaId",value = "业务剧ID",required = true,dataType = "String")
	private String dramaId;
	
	@TableField("asset_type")
	@ApiModelProperty(name = "assetType",value = "COVER_V/COVER_H/POSTER/SHARE",required = true,dataType = "String")
	private String assetType;
	
	@TableField("url")
	@ApiModelProperty(name = "url",value = "资源URL",required = true,dataType = "String")
	private String url;
	
	@TableField("width")
	@ApiModelProperty(name = "width",value = "宽",required = false,dataType = "Integer")
	private Integer width;
	
	@TableField("height")
	@ApiModelProperty(name = "height",value = "高",required = false,dataType = "Integer")
	private Integer height;
	
	@TableField("status")
	@ApiModelProperty(name = "status",value = "1可用0禁用",required = false,dataType = "Integer")
	private Integer status;

	@TableField("deleted")
	@ApiModelProperty(name = "deleted",value = "0正常1软删",required = false,dataType = "Integer")
	private Integer deleted;

	@TableField("remark")
	@ApiModelProperty(name = "remark",value = "备注",required = false,dataType = "String")
	private String remark;

	@TableField("create_by")
	@ApiModelProperty(name = "createBy",value = "创建人",required = false,dataType = "String")
	private String createBy;

	@TableField("update_by")
	@ApiModelProperty(name = "updateBy",value = "更新人",required = false,dataType = "String")
	private String updateBy;
	
	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "创建时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;

}
