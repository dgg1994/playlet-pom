package com.playlet.internal.enums;

public enum DeleteStateEnum {
	DELETE(1, "是 删除"),
	NORMAL(0, "否 正常");

    
    private final Integer code;

    private final String name;

    DeleteStateEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

	public Integer getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
	


}
