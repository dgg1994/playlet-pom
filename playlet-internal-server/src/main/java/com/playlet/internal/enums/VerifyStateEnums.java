package com.playlet.internal.enums;



/**
 * @category 审核状态
 * @author Hlin
 *
 */
public enum VerifyStateEnums {
	DRAFT(0, "草稿"),
	PENDING_REVIEW(1, "待审核"),
	AVAILABLE_NOW(2, "已上架"),
	REMOVED_SHELVES(3, "已下架");
	

	private Integer index;

	private String name;

	private VerifyStateEnums(Integer index, String name) {
		this.index = index;
		this.name = name;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
