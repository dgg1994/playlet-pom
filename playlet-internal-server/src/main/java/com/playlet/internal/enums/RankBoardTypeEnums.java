package com.playlet.internal.enums;

/**
 * 榜单类型（rank_board.board_type）。
 * PLAYLIST(3) 仅作热榜跳过遗留数据、以及剧单 Tab 响应 boardType 兼容值；
 * 剧单主体已迁至 drama_playlist。
 */
public enum RankBoardTypeEnums {

	ALGO(1, "算法"),
	MANUAL(2, "人工"),
	/** 兼容响应 boardType；剧单主体已迁 drama_playlist，勿再往 rank_board 写 type=3 */
	PLAYLIST(3, "剧单");

	private final Integer index;
	private final String name;

	RankBoardTypeEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static boolean isPlaylist(Integer boardType) {
		return PLAYLIST.getIndex().equals(boardType);
	}
}
