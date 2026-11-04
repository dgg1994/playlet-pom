package com.playlet.internal.service;

import com.playlet.internal.api.request.CreatorSettleApplyCreateQuery;
import com.playlet.internal.api.request.CreatorSettleApplyHandleQuery;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorSettleApplyQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端入驻申请审核：网关 /china/admin/creatorSettleApply/**
 */
@RequestMapping("/creatorSettleApply")
@Api(value = "入驻审核管理", tags = "入驻审核管理")
public interface CreatorSettleApplyManageService {

    @PostMapping("/findList")
    @ApiOperation(value = "入驻申请分页列表", notes = "keyword 模糊搜入驻名称/联系人/联系方式；identityType 1个人 2企业团队；auditStatus 0待查看 1已查看 2已通过 3已驳回")
    ResponseBase findList(CreatorSettleApplyQuery query);

    @GetMapping("/detail")
    @ApiImplicitParam(name = "applyId", value = "creator_profile.id", required = true, dataType = "Integer", paramType = "query")
    @ApiOperation(value = "入驻申请详情", notes = "打开详情时自动把「待查看」置为「已查看」")
    ResponseBase detail(Integer applyId);

    @PostMapping("/handle")
    @ApiOperation(value = "审核入驻申请", notes = "pass=true 通过；pass=false 驳回并必填 rejectReason")
    ResponseBase handle(CreatorSettleApplyHandleQuery query, HttpServletRequest request);

    @PostMapping("/create")
    @ApiOperation(value = "新增入驻申请", notes = "管理端代录入：同时创建创作者登录账号与入驻资料，审核状态为待查看")
    ResponseBase create(CreatorSettleApplyCreateQuery query, HttpServletRequest request);
}
