package com.playlet.internal.service.settle;

import com.playlet.internal.api.request.SettleManageBatchConfirmReqEntity;
import com.playlet.internal.api.request.SettleManageConfirmReqEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.settle.SettleManageEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端分账结算：网关 /china/admin/settleManage/**
 */
@RequestMapping("/settleManage")
@Api(value = "分账结算管理", tags = "分账结算管理")
public interface SettleManageService {

    @PostMapping("/findList")
    @ApiOperation(value = "结算单分页列表", notes = "可选 keyword / status 筛选")
    ResponseBase findList(SettleManageEntity entity);

    @PostMapping("/confirm")
    @ApiOperation(value = "确认单笔结算", notes = "只处理 status=0 的单据")
    ResponseBase confirm(SettleManageConfirmReqEntity req);

    @PostMapping("/batchConfirm")
    @ApiOperation(value = "批量确认结算", notes = "已结算/无效单据自动跳过，返回成功条数")
    ResponseBase batchConfirm(SettleManageBatchConfirmReqEntity req);
}
