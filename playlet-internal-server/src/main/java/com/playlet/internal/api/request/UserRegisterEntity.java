package com.onetoken.api.request;

import lombok.Data;

/**
 * @category 用户注册三方入参
 * @author Hlin
 *
 */
@Data
public class UserRegisterEntity {
	
	private String email;
	
	private String mobileNumber;
	
	private String mobilePrefix;
	
	private String local;

}
