package com.playlet.internal.enums;

public enum ChannelCodeEnum {
    LONG_PAY("10001", "龙支付"),
    QI_PI_LANG("20001", "七匹狼"),
    BAO_DING("30001", "宝鼎"),
    BAO_MA("40001", "宝马"),
    GG_PAY_ALIPAY("50001", "GG Pay支付宝"),
    GG_PAY_WXPAY("60001", "GG Pay 微信"),
    GG_PAY_ALIPAYV2("70001", "GG Pay支付宝V2"),
    GG_PAY_WXPAYV2("80001", "GG Pay 微信V2"),;

    
    private final String code;

    private final String name;

    ChannelCodeEnum(String code, String name) {
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
