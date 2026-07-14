package com.onetoken.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("app_oauth_account")
@ApiModel(value = "第三方登录绑定")
public class AppOauthAccountEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("provider")
    @ApiModelProperty(value = "apple / google")
    private String provider;

    @TableField("provider_sub")
    private String providerSub;

    @TableField("uid")
    private String uid;

    @TableField("email")
    private String email;

    @TableField("setTime")
    private Date setTime;

    @TableField("gmtModified")
    private Date gmtModified;
}
