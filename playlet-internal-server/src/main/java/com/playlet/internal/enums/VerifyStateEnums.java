package com.playlet.internal.enums;

import java.util.ArrayList;
import java.util.List;

import com.playlet.internal.entity.system.DicEntity;

/**
 * @category 审核状态
 * @author Hlin
 *
 */
public enum VerifyStateEnums {
//	DRAFT(0, "草稿"),
//	PENDING_REVIEW(1, "待审核"),
	AVAILABLE_NOW(1, "已上架"),
	REMOVED_SHELVES(0, "未上架");
	

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

    public static List<DicEntity> getList() {
    	VerifyStateEnums[] typeEnums = values();
		List<DicEntity> list = new ArrayList<>();
		for (VerifyStateEnums typeEnum : typeEnums) {
			DicEntity dicEntity = new DicEntity();
			dicEntity.setId(typeEnum.getIndex());
			dicEntity.setName(typeEnum.getName());
			list.add(dicEntity);
		}
		return list;
	}

}
