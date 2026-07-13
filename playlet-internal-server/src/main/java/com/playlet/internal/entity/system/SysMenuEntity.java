package com.playlet.internal.entity.system;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "菜单权限")
public class SysMenuEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "菜单ID")
    private Integer menuId;

    @TableField("menu_name")
    @Schema(description = "菜单名称")
    private String menuName;

    @TableField(exist = false)
    @Schema(description = "父菜单名称")
    private String parentName;

    @TableField("parent_id")
    @Schema(description = "父菜单ID")
    private Integer parentId;

    @TableField("order_num")
    @Schema(description = "显示顺序")
    private Integer orderNum;

    @TableField("path")
    @Schema(description = "路由地址")
    private String path;

    @TableField("component")
    @Schema(description = "组件路径")
    private String component;

    @TableField("query")
    @Schema(description = "路由参数")
    private String query;

    @TableField("is_frame")
    @Schema(description = "是否为外链（0是 1否）")
    private String isFrame;

    @TableField("is_cache")
    @Schema(description = "是否缓存（0缓存 1不缓存）")
    private String isCache;

    @TableField("menu_type")
    @Schema(description = "类型（M目录 C菜单 F按钮）")
    private String menuType;

    @TableField("visible")
    @Schema(description = "显示状态（0显示 1隐藏）")
    private String visible;

    @TableField("status")
    @Schema(description = "菜单状态（0正常 1停用）")
    private String status;

    @TableField("perms")
    @Schema(description = "权限字符串")
    private String perms;

    @TableField("icon")
    @Schema(description = "菜单图标")
    private String icon;

    @TableField("create_by")
    @Schema(description = "创建者")
    private String createBy;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField("update_by")
    @Schema(description = "更新者")
    private String updateBy;

    @TableField("update_time")
    @Schema(description = "更新时间")
    private Date updateTime;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    @TableField(exist = false)
    @Schema(description = "子菜单")
    private List<SysMenuEntity> children = new ArrayList<SysMenuEntity>();

    public Integer getMenuId() { return menuId; }
    public void setMenuId(Integer menuId) { this.menuId = menuId; }

    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 0, max = 50, message = "菜单名称长度不能超过50个字符")
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    @NotNull(message = "显示顺序不能为空")
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }

    @Size(min = 0, max = 200, message = "路由地址不能超过200个字符")
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    @Size(min = 0, max = 200, message = "组件路径不能超过255个字符")
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getIsFrame() { return isFrame; }
    public void setIsFrame(String isFrame) { this.isFrame = isFrame; }

    public String getIsCache() { return isCache; }
    public void setIsCache(String isCache) { this.isCache = isCache; }

    @NotBlank(message = "菜单类型不能为空")
    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }

    public String getVisible() { return visible; }
    public void setVisible(String visible) { this.visible = visible; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Size(min = 0, max = 100, message = "权限标识长度不能超过100个字符")
    public String getPerms() { return perms; }
    public void setPerms(String perms) { this.perms = perms; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public List<SysMenuEntity> getChildren() { return children; }
    public void setChildren(List<SysMenuEntity> children) { this.children = children; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("menuId", getMenuId())
                .append("menuName", getMenuName()).append("parentId", getParentId()).append("orderNum", getOrderNum())
                .append("path", getPath()).append("component", getComponent()).append("isFrame", getIsFrame())
                .append("IsCache", getIsCache()).append("menuType", getMenuType()).append("visible", getVisible())
                .append("status ", getStatus()).append("perms", getPerms()).append("icon", getIcon())
                .append("createBy", getCreateBy()).append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy()).append("updateTime", getUpdateTime()).append("remark", getRemark())
                .toString();
    }
}
