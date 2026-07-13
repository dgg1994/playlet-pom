package com.playlet.internal.service;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.system.SysRoleEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 *  @category 角色接口
 */
@RequestMapping("/role")
@Tag(name = "角色接口",description = "角色接口")
public interface RoleService {

	@PostMapping("/findAll")
	@Operation(summary = "查询所有角色", description = "查询所有角色")
	ResponseBase findAll(SysRoleEntity entity);

	@GetMapping("/findById")
	@Operation(summary = "查询角色", description = "查询角色")
	ResponseBase findById(Integer roleId);
	
	@PostMapping("/add")
	@Operation(summary = "新增角色", description = "新增角色")
	ResponseBase add(SysRoleEntity entity);
	
	@PostMapping("/update")
	@Operation(summary = "编辑角色信息", description = "编辑角色信息")
	ResponseBase update(SysRoleEntity entity);
	
	@GetMapping("/delete")
	@Operation(summary = "删除角色", description = "编辑角色信息")
	ResponseBase delete(Integer roleId);
	
	@PostMapping("/changeStatus")
	@Operation(summary = "角色停用启用", description = "角色停用启用")
	ResponseBase changeStatus(Integer roleId,String status);
	
	@GetMapping("/findNormalRole")
	@Operation(summary = "查询正常状态下角色信息", description = "查询正常状态下角色信息")
	ResponseBase findNormalRole();
	
	
}

