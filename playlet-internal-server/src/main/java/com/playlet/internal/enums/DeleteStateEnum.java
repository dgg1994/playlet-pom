package com.playlet.internal.enums;

public enum DeleteStateEnum {
	DELETE(1, "是 删除"),
	NORMAL(0, "否 正常");

    
    private final Integer index;

    private final String name;

    DeleteStateEnum(Integer index, String name) {
        this.index = index;
        this.name = name;
    }

	public Integer getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}
	


}
