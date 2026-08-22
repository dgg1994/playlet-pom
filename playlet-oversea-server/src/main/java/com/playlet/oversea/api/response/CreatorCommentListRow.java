package com.playlet.oversea.api.response;

import lombok.Data;

import java.util.Date;

/**
 * 作家评论列表 Dao 行（分页主查字段，用户/父评由 Service 批量补齐）。
 */
@Data
public class CreatorCommentListRow {

	private Integer id;
	private Integer dramaId;
	private String dramaTitle;
	private Integer videoId;
	private Integer setNum;
	private Integer parentId;
	private Integer userId;
	private Integer fromCreatorId;
	private String commentInfo;
	private Integer likeCount;
	private Integer pinFlag;
	private Date pinTime;
	private Date setTime;
	private Integer commentType;
}
