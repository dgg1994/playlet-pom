package com.playlet.internal.entity.drama;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 短剧备案材料（营业执照、身份证、版权承诺书、剧本授权、演员肖像权、BGM授权、发行许可证、备案公示、特殊题材、片头片尾）。
 */
@Data
@TableName("drama_record_material")
@ApiModel(value = "备案材料", description = "短剧备案材料文件")
public class DramaRecordMaterialEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("主键")
	private Long id;

	@TableField("drama_id")
	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@TableField("material_type")
	@ApiModelProperty("材料类型（见 DramaRecordMaterialTypeEnums）")
	private Integer materialType;

	@TableField("material_label")
	@ApiModelProperty("材料标签（冗余，方便展示）")
	private String materialLabel;

	@TableField("file_key")
	@ApiModelProperty("文件对象key（七牛）")
	private String fileKey;

	@TableField("file_name")
	@ApiModelProperty("原始文件名")
	private String fileName;

	@TableField("required")
	@ApiModelProperty("是否必填 1是 0否")
	private Integer required;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
