package com.playlet.internal.service;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.system.SysMenuEntity;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

/**
 * @category 菜单管理接口
 *
 */
@RequestMapping("/menu")
//@Api(value = "菜单管理接口",tags = "菜单管理接口")
public interface MenuService {
	
	@PostMapping("/findByUser")
	@Operation(summary = "获取用户菜单", description = "获取用户菜单")
	ResponseBase findByUser(Integer userId);
	
    @PostMapping("/findAll")
    @Operation(summary = "获取所有菜单列表", description = "获取所有菜单菜单列表")
    ResponseBase findAll(SysMenuEntity entity);
    
    @PostMapping("/treeselect")
	@Operation(summary = "获取树结构菜单", description = "获取树结构菜单")
	ResponseBase treeselect(SysMenuEntity entity);
    
    @GetMapping("/roleMenuTreeselect")
	@Operation(summary = "获取对应角色树结构菜单", description = "获取对应角色树结构菜单")
	ResponseBase roleMenuTreeselect(Integer roleId);
    
    @PostMapping("/add")
	@Operation(summary = "新增菜单", description = "新增菜单")
	ResponseBase add(SysMenuEntity entity);
    
    @GetMapping("/findById")
	@Operation(summary = "获取菜单详情", description = "获取菜单详情")
	ResponseBase findById(Integer menuId);
    
    @PostMapping("/update")
	@Operation(summary = "获取菜单详情", description = "获取菜单详情")
	ResponseBase update(SysMenuEntity entity);
    
    @GetMapping("/delete")
	@Operation(summary = "获取菜单详情", description = "获取菜单详情")
	ResponseBase delete(Integer menuId);
	
}

