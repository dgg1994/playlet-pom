package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletTransfetContactsEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端：内部转账通讯录（对齐 onetoken /walletContacts）。
 * 网关：/china/admin/walletContacts/**
 */
@RequestMapping("/walletContacts")
@Api(value = "钱包内部转账通讯录", tags = "钱包内部转账通讯录")
public interface WalletTransferContactsManageService {

	@PostMapping("/add")
	@ApiOperation(value = "新增联系人", notes = "按 wallet_uid 添加")
	ResponseBase add(WalletTransfetContactsEntity entity);

	@GetMapping("/delete")
	@ApiOperation(value = "删除联系人", notes = "按 id 删除")
	ResponseBase delete(Long id);

	@PostMapping("/update")
	@ApiOperation(value = "编辑联系人", notes = "修改标签名")
	ResponseBase update(WalletTransfetContactsEntity entity);

	@PostMapping("/findList")
	@ApiOperation(value = "查询联系人", notes = "分页查询")
	ResponseBase findList(WalletTransfetContactsEntity entity);

	@PostMapping("/recentTransfer")
	@ApiOperation(value = "最近转账", notes = "发送方最近转账记录并附带通讯录标签")
	ResponseBase recentTransfer(WalletTransfetContactsEntity entity);
}
