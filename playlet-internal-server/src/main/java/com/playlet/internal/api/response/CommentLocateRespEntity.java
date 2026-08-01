package com.playlet.internal.api.response;

import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("评论定位结果")
public class CommentLocateRespEntity {

	@ApiModelProperty("评论层级：1一级 2二级")
	private Integer commentLevel;

	@ApiModelProperty("所属一级评论ID；一级评论时为0")
	private Integer parentId;

	@ApiModelProperty("目标评论完整数据")
	private DramaVideoCommentEntity target;

	@ApiModelProperty("一级父评论（二级时返回）")
	private DramaVideoCommentEntity parent;

	@ApiModelProperty("一级评论所在页上下文")
	private CommentLocatePageEntity parentPage;

	@ApiModelProperty("二级回复所在页上下文（仅二级）")
	private CommentLocatePageEntity siblings;

	@Data
	@ApiModel("评论定位分页片")
	public static class CommentLocatePageEntity {
		@ApiModelProperty("当页列表")
		private List<DramaVideoCommentEntity> list = new ArrayList<>();
		@ApiModelProperty("页码（从1开始）")
		private Integer pageNumber;
		@ApiModelProperty("页大小")
		private Integer pageSize;
		@ApiModelProperty("同范围总条数")
		private Long total;
		@ApiModelProperty("目标在当页 list 中的下标（从0开始）")
		private Integer targetIndex;
	}
}
