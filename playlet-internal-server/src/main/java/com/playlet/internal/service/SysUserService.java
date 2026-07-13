package com.playlet.internal.service;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.system.SysUserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/user")
@Tag(name = "管理用户管理",description = "管理用户管理")
public interface SysUserService {

	@PostMapping("/findList")
	@Operation(summary = "用户列表", description = "用户列表分页查询未注销用户")
	ResponseBase findList(SysUserEntity entity);

	@PostMapping("/signUp")
	@Operation(summary = "用户注册", description = "用户注册")
	ResponseBase signUp(String user,@RequestPart(required = false) MultipartFile faceFile);

	@GetMapping("/findToken")
	@Operation(summary = "通过token获取用户信息", description = "通过token获取用户信息")
	ResponseBase findToken(HttpServletRequest request);

    @DeleteMapping("/delUser/{userId}")
	@Operation(summary = "删除用户信息", description = "删除用户信息")
	ResponseBase delUser(@PathVariable Integer userId);

    @PostMapping("/updateState")
	@Operation(summary = "改变用户账号状态", description = "改变用户账号状态")
    ResponseBase updateUserState( Integer userId, Integer newState);

    @PostMapping("/updateUser")
	@Operation(summary = "编辑用户信息", description = "编辑用户信息")
	ResponseBase updateUser( String user,@RequestPart(required = false) MultipartFile file);

	@PostMapping("/resetPwd")
	@Operation(summary = "修改密码", description = "修改密码")
	ResponseBase resetPwd(Integer userId,String password);

	@PostMapping("/resetUserPwd")
	@Operation(summary = "重置密码", description = "重置密码")
	ResponseBase resetUserPwd(Integer userId,String password);

	@PostMapping("/verifyPwd")
	@Operation(summary = "校验账号密码", description = "校验账号密码")
	ResponseBase verifyPwd(Integer userId,String password);
	
	@PostMapping("/updateDeviceModel")
	@Operation(summary = "更新用户设备信息", description = "更新用户设备信息")
    ResponseBase updateDeviceModel(SysUserEntity entity);
	
	@GetMapping("/upGoogleSecretkey")
	@Operation(summary = "修改谷歌验证密钥", description = "修改谷歌验证密钥")
    ResponseBase upGoogleSecretkey(Integer userId,String googleSecretkey);
	
	@GetMapping("/IssueGoogleSecretkey")
	@Operation(summary = "签发谷歌验证密钥", description = "签发谷歌验证密钥")
    ResponseBase IssueGoogleSecretkey(String userName);

	@GetMapping("/getPasswordOrKey")
	@Operation(summary = "签发谷歌验证密钥、默认密码", description = "签发谷歌验证密钥、默认密码")
    ResponseBase getPasswordOrKey();

}

