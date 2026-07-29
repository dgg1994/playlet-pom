package com.playlet.internal.enums;


import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @category 消息通知类型
 * @author Hlin
 *
 */
public enum NoticeStateEnums {
	NORMAL(1, "正常"),
	DEACTIVATE(2, "停用");
	
	private Integer index;

	private String name;


	private NoticeStateEnums(Integer index, String name) {
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
		NoticeStateEnums[] typeEnums = values();
		List<DicEntity> list = new ArrayList<>();
		for (NoticeStateEnums typeEnum : typeEnums) {
			DicEntity dicEntity = new DicEntity();
			dicEntity.setId(typeEnum.getIndex());
			dicEntity.setName(typeEnum.getName());
			list.add(dicEntity);
		}
		return list;
	}

	public static String getName(int i) {
		NoticeStateEnums[] typeEnums = values();
		for (NoticeStateEnums typeEnum : typeEnums) {
			if (typeEnum.getIndex().equals(i)) {
				return typeEnum.getName();
			}
		}
		return null;
	}

}
