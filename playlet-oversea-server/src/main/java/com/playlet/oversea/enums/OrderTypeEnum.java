package com.playlet.oversea.enums;

import java.util.ArrayList;
import java.util.List;

import com.playlet.oversea.entity.system.DicEntity;

/**
 * 订单状态枚举
 * 
 * @author system
 * @date 2026-06-16
 */
public enum OrderTypeEnum {

	COLLECTION(1, "代收"),
	COLLECTION_TEST(2, "代收测试");

    private final Integer code;
    
    private final String name;

    OrderTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
    
    public static String getName(int i) {
    	OrderTypeEnum[] orderStatusEnums = values();
        for (OrderTypeEnum orderStatusEnum : orderStatusEnums) {
            if (orderStatusEnum.getCode().equals(i)) {
                return orderStatusEnum.getName();
            }
        }
        return null;
    }
    
    public static List<DicEntity> getList() {
    	OrderTypeEnum[] typeEnums = values();
		List<DicEntity> list = new ArrayList<>();
		for (OrderTypeEnum typeEnum : typeEnums) {
			DicEntity dicEntity = new DicEntity();
			dicEntity.setId(typeEnum.getCode());
			dicEntity.setName(typeEnum.getName());
			list.add(dicEntity);
		}
		return list;
	}
}