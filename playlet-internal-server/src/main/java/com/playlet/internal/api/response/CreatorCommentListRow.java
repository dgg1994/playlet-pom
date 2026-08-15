package com.playlet.internal.api.response;

import lombok.Data;

import java.util.Date;

/**
 * 作家评论列表 Dao 行（含父评扁平字段）。
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
	private String userName;
	private String avatar;
	private String commentInfo;
	private Integer likeCount;
	private Integer pinFlag;
	private Date pinTime;
	private Date setTime;

	private Integer parentCommentId;
	private String parentUserName;
	private String parentAvatar;
	private String parentCommentInfo;
	private Integer parentFromCreatorId;
}
