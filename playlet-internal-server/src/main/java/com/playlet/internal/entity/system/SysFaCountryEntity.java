package com.playlet.internal.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @category 国家省市级联
 * @author Hlin
 *
 */
@Data
@TableName("sys_fa_country")
public class SysFaCountryEntity {
	
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "id", required = true, dataType = "Integer")
	private Integer id;

    @TableField("parentId")
    @ApiModelProperty(name = "parentId", value = "上级id", required = true, dataType = "String")
    private String parentId;
    
    @TableField("cname")
    @ApiModelProperty(name = "cname", value = "中文名", required = true, dataType = "String")
    private String cname;
    
    @TableField("ename")
    @ApiModelProperty(name = "ename", value = "英文名", required = true, dataType = "String")
    private String ename;
    
    @TableField("tname")
    @ApiModelProperty(name = "ename", value = "繁体", required = true, dataType = "String")
    private String tname;
    
    @TableField(exist = false)
    @ApiModelProperty(name = "name", value = "英文名", required = true, dataType = "String")
    private String name;

}
