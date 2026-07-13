package com.playlet.internal.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_role_menu")
@Schema(description = "角色菜单关联对象")
public class SysRoleMenuEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    @TableField("role_id")
    @Schema(description = "角色id")
    private Integer roleId;

    @TableField("menu_id")
    @Schema(description = "菜单id")
    private Integer menuId;

    @TableField("create_user")
    @Schema(description = "创建人")
    private Integer createUser;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private Date createTime;
}
