package com.playlet.oversea.service;

import com.playlet.oversea.api.request.WalletCardAdminUpdateRequest;
import com.playlet.oversea.api.request.WalletCardShippingRequest;
import com.playlet.oversea.api.request.BankcardRechargeRequest;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.wallet.WalletCardProductEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端卡产品（对齐 onetoken /card/**；关联键统一为产品 id）。
 */
@RequestMapping("/card")
@Api(value = "卡产品管理", tags = "卡产品管理")
public interface CardManageService {

	@PostMapping("/findListPag")
	@ApiOperation(value = "卡产品分页列表")
	ResponseBase findListPag(WalletCardProductEntity entity);

	@GetMapping("/pullList")
	@ApiOperation(value = "拉取三方卡产品")
	ResponseBase pullList();

	@PostMapping("/update")
	@ApiOperation(value = "编辑卡产品配置")
	ResponseBase update(WalletCardAdminUpdateRequest entity);

	@PostMapping("/updateImg")
	@ApiOperation(value = "修改封面图")
	ResponseBase updateImg(@RequestParam("id") Integer id, @RequestParam("file") MultipartFile file);

	@PostMapping("/updateListImg")
	@ApiOperation(value = "修改列表图")
	ResponseBase updateListImg(@RequestParam("id") Integer id, @RequestParam("file") MultipartFile file);

	@GetMapping("/copyCard")
	@ApiOperation(value = "复制卡产品")
	ResponseBase copyCard(@RequestParam("id") Integer id);

	@GetMapping("/upState")
	@ApiOperation(value = "上架/下架")
	ResponseBase upState(@RequestParam("id") Integer id, @RequestParam("stateId") Integer stateId);

	@GetMapping("/delete")
	@ApiOperation(value = "删除卡产品")
	ResponseBase delete(@RequestParam("id") Integer id);

	@PostMapping("/topUp")
	@ApiOperation(value = "管理端卡充值")
	ResponseBase topUp(BankcardRechargeRequest entity);

	@PostMapping("/shipping")
	@ApiOperation(value = "实体卡发货")
	ResponseBase shipping(WalletCardShippingRequest entity, HttpServletRequest request);
}
