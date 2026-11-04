package com.playlet.internal.entity.banner;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.playlet.internal.constants.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Calendar;
import java.util.Date;

/**
 * Banner 与开屏管理
 * <p>status 为计算字段（now vs startTime/endTime），不持久化</p>
 */
@Data
@TableName("banner_manage")
@ApiModel(value = "Banner 与开屏")
public class BannerManageEntity {

    /** 手动实现分页字段（不继承 PageQueryHelperEntity 避免 startTime/endTime 类型冲突） */
    @TableField(exist = false)
    @ApiModelProperty(name = "pageNumber", value = "页码", dataType = "Integer")
    private Integer pageNumber;

    @TableField(exist = false)
    @ApiModelProperty(name = "pageSize", value = "每页条数", dataType = "Integer")
    private Integer pageSize;

    public Integer getPageNumber() {
        if (pageNumber == null || pageNumber < 1) {
            return Constants.PAGENUMBER;
        }
        return pageNumber;
    }
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return Constants.PAGESIZE;
        }
        return Math.min(pageSize, Constants.MAX_PAGESIZE);
    }

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
    private Integer id;

    @TableField("title")
    @ApiModelProperty(name = "title", value = "标题", dataType = "String")
    private String title;

    @TableField("preview_text")
    @ApiModelProperty(name = "previewText", value = "列表缩略短文案，最长8", dataType = "String")
    private String previewText;

    @TableField("image_url")
    @ApiModelProperty(name = "imageUrl", value = "图片 URL（/file/upload 返回）", dataType = "String")
    private String imageUrl;

    @TableField("jump_type")
    @ApiModelProperty(name = "jumpType", value = "跳转类型：1=活动H5 2=短剧详情 3=会员开通页", dataType = "Integer")
    private Integer jumpType;

    @TableField("jump_value")
    @ApiModelProperty(name = "jumpValue", value = "jumpType=1 完整URL；=2 短剧ID；=3 空字符串", dataType = "String")
    private String jumpValue;

    @TableField("position")
    @ApiModelProperty(name = "position", value = "投放位置：1=首页顶部 2=首页轮播 3=App开屏", dataType = "Integer")
    private Integer position;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(name = "startTime", value = "生效日期 yyyy-MM-dd", dataType = "Date")
    private Date startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(name = "endTime", value = "结束日期 yyyy-MM-dd", dataType = "Date")
    private Date endTime;

    @TableField("sort_weight")
    @ApiModelProperty(name = "sortWeight", value = "排序权重，越大越靠前", dataType = "Integer")
    private Integer sortWeight;

    @TableField("remark")
    @ApiModelProperty(name = "remark", value = "运营备注", dataType = "String")
    private String remark;

    @TableField("setTime")
    @ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
    private Date setTime;

    @TableField("gmtModified")
    @ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
    private Date gmtModified;

    /** 搜索关键字（不持久化） */
    @TableField(exist = false)
    @ApiModelProperty(name = "keyword", value = "模糊搜 title / previewText", dataType = "String")
    private String keyword;

    /**
     * 动态计算状态：0待上线 1投放中 2已结束
     * <p>规则：startTime 当天 00:00:00 起生效，endTime 当天 23:59:59 结束</p>
     */
    public Integer getStatus() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        Date now = new Date();
        if (now.before(startTime)) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endExclusive = cal.getTime();
        if (!now.before(endExclusive)) {
            return 2;
        }
        return 1;
    }
}
