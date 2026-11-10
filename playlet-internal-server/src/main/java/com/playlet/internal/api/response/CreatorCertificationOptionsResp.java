package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * 认证管理：表单字典选项。
 */
@Data
@ApiModel(value = "创作者认证字典选项")
public class CreatorCertificationOptionsResp {

	private List<IdentityOption> personalIdentities;
	private List<IdentityOption> orgIdentities;
	private List<String> genreTypes;
	private List<String> markets;
	private List<String> personalFields;
	private List<String> services;

	@Data
	@ApiModel(value = "认证身份选项")
	public static class IdentityOption {
		private Integer code;
		private String name;
		private String desc;
	}
}
