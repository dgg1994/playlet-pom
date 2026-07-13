package com.playlet.internal.entity.system;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("sys_user_role")
@Schema(description = "用户角色")
public class SysUserRoleEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    @TableField("user_id")
    @Schema(description = "用户id")
    private Integer userId;

    @TableField("role_id")
    @Schema(description = "角色id")
    private Integer roleId;

    @TableField("create_user")
    @Schema(description = "创建人")
    private Integer createUser;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private Date createTime;
}
