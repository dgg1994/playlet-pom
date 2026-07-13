package com.playlet.oversea.query.pub;

import com.baomidou.mybatisplus.annotation.TableField;
import com.playlet.oversea.constants.Constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询条件")
public class PageQueryHelperEntity {

    @TableField(exist = false)
    @Schema(description = "分页页码")
    private Integer pageNumber = Constants.PAGENUMBER;

    @TableField(exist = false)
    @Schema(description = "分页数量")
    private Integer pageSize = Constants.PAGESIZE;

    @TableField(exist = false)
    @Schema(description = "开始时间")
    private String startTime;

    @TableField(exist = false)
    @Schema(description = "结束时间")
    private String endTime;
}
