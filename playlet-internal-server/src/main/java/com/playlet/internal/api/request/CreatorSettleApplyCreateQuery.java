package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端新增入驻申请（同时创建创作者登录账号 + 入驻资料，初始状态待查看）。
 */
@Data
@ApiModel("新增入驻申请")
public class CreatorSettleApplyCreateQuery {

    @NotBlank(message = "登录邮箱不能为空")
    @ApiModelProperty(value = "登录邮箱（唯一）", required = true)
    private String userAccount;

    @NotBlank(message = "登录密码不能为空")
    @ApiModelProperty(value = "登录密码（明文传输，服务端加密存储）", required = true)
    private String userPassword;

    @ApiModelProperty("展示昵称，空则用登录邮箱")
    private String nickname;

    @ApiModelProperty("手机区号，如 +86")
    private String mobilePrefix;

    @ApiModelProperty("手机号")
    private String mobileNumber;

    @NotNull(message = "入驻性质不能为空")
    @ApiModelProperty(value = "入驻性质：1个人 2企业/团队", required = true)
    private Integer identityType;

    @ApiModelProperty("入驻身份：1出品方 2MCN机构 3独立创作者；默认 3")
    private Integer creatorRole;

    @ApiModelProperty("真实姓名/法人姓名；入驻性质=个人时必填")
    private String realName;

    @ApiModelProperty("机构名称；入驻性质=企业/团队时必填")
    private String orgName;

    @ApiModelProperty("证件正面图 key/URL")
    private String idCardFront;

    @ApiModelProperty("证件背面图 key/URL")
    private String idCardBack;

    @ApiModelProperty("营业执照图 key/URL")
    private String orgLicense;

    @ApiModelProperty("账单寄送地址")
    private String billAddress;

    @ApiModelProperty("资历自述")
    private String qualifyDesc;
}
