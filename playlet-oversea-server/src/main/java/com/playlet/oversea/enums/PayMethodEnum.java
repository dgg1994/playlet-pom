package com.playlet.oversea.enums;

public enum PayMethodEnum {
	FORM_JUMP("formJump", "formJump"),
	CODE_IMG("codeImg", "codeImg"),
	WX_APP("wxApp", "wxApp"),
	ALIPAY_APP("alipayApp", "alipayApp"),
	WX_JSA_PI("wxJSApi", "wxJSApi");

    
    private final String code;

    private final String name;

    PayMethodEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

}
