package com.playlet.oversea.enums;

import java.util.ArrayList;
import java.util.List;

import com.playlet.oversea.entity.system.DicEntity;

public enum TelegramRoleEnums {
	ADMIN(1, "管理员"), 
	MERCHANTS(2, "商户"),;

	private Integer index;

	private String name;

	private TelegramRoleEnums(Integer index, String name) {
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
		TelegramRoleEnums[] typeEnums = values();
		List<DicEntity> list = new ArrayList<>();
		for (TelegramRoleEnums typeEnum : typeEnums) {
			DicEntity dicEntity = new DicEntity();
			dicEntity.setId(typeEnum.getIndex());
			dicEntity.setName(typeEnum.getName());
			list.add(dicEntity);
		}
		return list;
	}

    public static String getName(int i) {
    	TelegramRoleEnums[] orderStatusEnums = values();
        for (TelegramRoleEnums orderStatusEnum : orderStatusEnums) {
            if (orderStatusEnum.getIndex().equals(i)) {
                return orderStatusEnum.getName();
            }
        }
        return null;
    }
}
