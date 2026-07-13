package com.playlet.internal.entity.system;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户")
public class SysUserEntity extends PageQueryHelperEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    @TableField("username")
    @Schema(description = "用户名")
    private String username;

    @TableField("tel")
    @Schema(description = "手机号")
    private String tel;

    @TableField("acctive")
    @Schema(description = "账号")
    private String acctive;

    @TableField("head_pic")
    @Schema(description = "头像")
    private String headPic;

    @TableField("user_state")
    @Schema(description = "用户状态，2：离职，1：正常，0：账号注销")
    private Integer userState;

    @TableField("password")
    @Schema(description = "密码")
    private String password;

    @TableField("create_user")
    @Schema(description = "创建人")
    private Integer createUser;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private String createTime;

    @TableField("login_time")
    @Schema(description = "登录时间")
    private Date loginTime;

    @TableField("google_secretkey")
    @Schema(description = "谷歌密钥")
    private String googleSecretkey;

    @TableField(exist = false)
    @Schema(description = "用户状态名称")
    private String userStateNmae;

    @TableField(exist = false)
    @Schema(description = "角色集合")
    private List<Integer> roleId;

    @TableField(exist = false)
    @Schema(description = "权限集合")
    private List<SysUserPowerEntity> power;

    @TableField(exist = false)
    @Schema(description = "角色集合")
    private List<SysRoleEntity> roleList;

    @TableField(exist = false)
    @Schema(description = "角色标识")
    private List<String> roles;

    @TableField(exist = false)
    @Schema(description = "权限标识")
    private List<String> permissions;

    @TableField(exist = false)
    private Integer signCount;

    @TableField(exist = false)
    private Integer adminRole;

    @TableField(exist = false)
    private Integer deleteState;

    @TableField(exist = false)
    private Integer deptId;

    @TableField(exist = false)
    private Integer manageId;

    @TableField(exist = false)
    private Integer googleCode;

    public SysUserEntity() {
        super();
    }
}
